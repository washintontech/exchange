package com.washintontech.matchingEngine.component;

import com.washintontech.common.chronicle.Transaction;
import com.washintontech.matchingEngine.model.Order;
import com.washintontech.matchingEngine.model.OrderPool;
import com.washintontech.matchingEngine.model.PriceLevel;
import com.washintontech.matchingEngine.util.NumberUtils;
import com.washintontech.matchingEngine.util.TimeUtils;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.wire.DocumentContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import quickfix.FieldNotFound;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.MICROSECONDS;

public class OrderBook {
    private static final Logger log = LogManager.getLogger(OrderBook.class);
    private final int symbolId;
    private final OrderPool orderPool;
    private final ConcurrentNavigableMap<Long, PriceLevel> bids; // TODO: Long and PriceLevel -> GC load
    private final ConcurrentNavigableMap<Long, PriceLevel> asks;
    private final Map<Long, Order> orderCache; // TODO: check the need
    private final ScheduledExecutorService transactionExecutor;
//    private final ExecutorService postTransactionExecutor;
//
//    private final TradeBook tradeBook;

    // TODO: Flat array map + concurrency
    // private final Long2ObjectOpenHashMap<Order> orderCache = new Long2ObjectOpenHashMap<>(1024, 0.75f);

    private volatile long sequenceNumber;
    private final ExcerptAppender appender;

    public OrderBook(final int symbolId, final OrderPool orderPool, final ExcerptAppender appender) {
        this.symbolId = symbolId;
        this.orderPool = orderPool;
        this.bids = new ConcurrentSkipListMap<>(Comparator.reverseOrder());
        this.asks = new ConcurrentSkipListMap<>();
        this.orderCache = new ConcurrentHashMap<>();
        this.transactionExecutor = Executors.newSingleThreadScheduledExecutor();
        transactionExecutor.scheduleAtFixedRate(this::transact, 10, 10, MICROSECONDS);
        // private final ExecutorService matchingPool = Executors.newFixedThreadPool(4, new AffinityThreadFactory());

//        this.postTransactionExecutor = Executors.newFixedThreadPool(1);
//        this.tradeBook = new TradeBook(symbolId);
        this.appender = appender;
    }

    public void processNewOrder(final Order order) {
        log.info("Processing order: {}", order);
        ConcurrentNavigableMap<Long, PriceLevel> book = getBook(order);
        book.compute(order.getPrice(), (p, level) -> {
            if (level == null) level = new PriceLevel(p);
            level.add(order);
            orderCache.put(order.getOrderId(), order);
            return level;
        });
    }

    public void processCancelOrder(final OrderCancelRequest order) throws FieldNotFound {
        log.info("Processing CancelOrder: {}", order);
        final var existingOrder = orderCache.get(Long.valueOf(order.getOrderID().getValue()));
        if (existingOrder != null) {
            existingOrder.getPrevious().setNext(existingOrder.getNext());
            orderPool.returnOrder(existingOrder);
        }
        // TODO: Send Error report.
        // Ensure validation at Broker before sending Cancel Order.

    }

    public void processOrderCancelReplaceOrder(final OrderCancelReplaceRequest changedOrderRequest, final long newOrderId) throws FieldNotFound {
        log.info("Processing OrderCancelReplaceOrder: {}", changedOrderRequest);
        final var existingOrder = orderCache.get(Long.valueOf(changedOrderRequest.getOrderID().getValue()));
        if (existingOrder != null) {
            existingOrder.getPrevious().setNext(existingOrder.getNext());

            Order order = existingOrder.enrichWithOrderCancelReplaceOrder(changedOrderRequest, newOrderId);
            getBook(order)
                    .compute(order.getPrice(), (p, level) -> {
                        if (level == null) level = new PriceLevel(p);
                        level.add(order);
                        orderCache.put(order.getOrderId(), order);
                        return level;
                    });
        }
        // TODO: Send Error report.
        // Ensure validation at Broker before sending Cancel Order.
    }

    private ConcurrentNavigableMap<Long, PriceLevel> getBook(final Order order) {
        if (order.getSide() == '1') {
            return bids;
        }
        return asks;
    }

    // Level -> Order -> items
    private void transact() { // TODO: Check if previous transaction still going on .. may be Add as Runnable in queue
        //log.info("Transact: bids.size(): {},  asks.size(): {}", bids.size(), asks.size());
        while (!bids.isEmpty() && !asks.isEmpty()) {
            final var priceLevelBidEntry = bids.firstEntry();
            final var bidPrice = priceLevelBidEntry.getKey();
            final var bidLevel = priceLevelBidEntry.getValue();

            final var priceLevelAskEntry = asks.firstEntry();
            final var askPrice = priceLevelAskEntry.getKey();
            final var askLevel = priceLevelAskEntry.getValue();

            if (bidPrice < askPrice) {
                return;
            }

            final var transactionPrice = NumberUtils.midValue( // TODO: Rectify for long
                    bidPrice, askPrice, 4, 0.0005f);

            while (true) {
                final var bidOrder = bidLevel.getHead();
                if (bidOrder == null) {
                    bids.remove(bidPrice);
                    break;
                }
                final var askOrder = askLevel.getHead();
                if (askOrder == null) {
                    asks.remove(askPrice);
                    break;
                }
                doTransact(bidOrder, askOrder, transactionPrice, bidLevel, askLevel);
            }
        }
    }

    private void doTransact(final Order bidOrder, final Order askOrder, final long transactionPrice,
                            final PriceLevel bidLevel, final PriceLevel askLevel) {
        final var bidOrderPendingQty = bidOrder.pendingQuantities();
        final var askOrderPendingQty = askOrder.pendingQuantities();
        final var transactionId = NumberUtils.generateThreadLocalRandomLong();
        final var transactionTime = TimeUtils.generateInstantEpochNanoSec();

        final Transaction bidTransaction = new Transaction(transactionId, transactionTime, bidOrder.getOrderId(),
                transactionPrice, bidOrder.getSymbolId(), bidOrder.getBrokerId(), bidOrder.getSide());
        final Transaction askTransaction = new Transaction(transactionId, transactionTime, askOrder.getOrderId(),
                transactionPrice, askOrder.getSymbolId(), askOrder.getBrokerId(), askOrder.getSide());

        if (bidOrderPendingQty <= askOrderPendingQty) {
            bidTransaction.setQuantity(bidOrderPendingQty);
            askTransaction.setQuantity(bidOrderPendingQty);

            bidLevel.remove(bidOrder);
            orderPool.returnOrder(bidOrder);
            if (bidOrderPendingQty == askOrderPendingQty) {
                askLevel.remove(askOrder);
                orderPool.returnOrder(askOrder);
            } else {
                askOrder.updateExecutedQuantity(bidOrderPendingQty);
            }

        } else {
            bidTransaction.setQuantity(askOrderPendingQty);
            askTransaction.setQuantity(askOrderPendingQty);

            askLevel.remove(askOrder);
            orderPool.returnOrder(askOrder);
            bidOrder.updateExecutedQuantity(askOrderPendingQty);
        }

        saveTransaction(bidTransaction);
        saveTransaction(askTransaction);
    }

    private void saveTransaction(final Transaction transaction) {
        try (DocumentContext dc = appender.writingDocument()) {
            Objects.requireNonNull(dc.wire())
                    .write("Transaction_v1")
                    .object(transaction);
        }
    }
}
