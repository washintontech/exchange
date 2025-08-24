package com.washintontech.matchingEngine.component;

import com.washintontech.common.chronicle.ChronicleQueueOperation;
import com.washintontech.common.chronicle.Transaction;
import com.washintontech.common.utils.TimeUtils;
import com.washintontech.matchingEngine.model.Order;
import com.washintontech.matchingEngine.model.OrderPool;
import com.washintontech.matchingEngine.model.PriceLevel;
import com.washintontech.matchingEngine.util.NumberUtils;
import lombok.extern.log4j.Log4j2;
import quickfix.FieldNotFound;
import quickfix.field.ExecType;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.MICROSECONDS;

@Log4j2
public class OrderBook {
    private final int symbolId;
    private final OrderPool orderPool;
    private final ConcurrentNavigableMap<Long, PriceLevel> bids; // TODO: Long and PriceLevel -> GC load
    private final ConcurrentNavigableMap<Long, PriceLevel> asks;

    // TODO: Flat array map + concurrency
    // private final Long2ObjectOpenHashMap<Order> orderCache = new Long2ObjectOpenHashMap<>(1024, 0.75f);
    private final Map<Long, Order> orderCache;
    private final ScheduledExecutorService transactionExecutor;

    private volatile long sequenceNumber;
    private final ChronicleQueueOperation queueOperation;

    public OrderBook(final int symbolId, final OrderPool orderPool, final ChronicleQueueOperation queueOperation) {
        this.symbolId = symbolId;
        this.orderPool = orderPool;
        this.bids = new ConcurrentSkipListMap<>(Comparator.reverseOrder());
        this.asks = new ConcurrentSkipListMap<>();
        this.orderCache = new ConcurrentHashMap<>();
        this.transactionExecutor = createTransactionExecutor();
        transactionExecutor.scheduleAtFixedRate(this::transact, 10, 10, MICROSECONDS);
        this.queueOperation = queueOperation;
    }

    public void processNewOrder(final Order order) {
        log.debug("Processing order: {}", order);
        ConcurrentNavigableMap<Long, PriceLevel> book = getBook(order);
        book.compute(order.getPrice(), (p, level) -> {
            if (level == null) level = new PriceLevel(p);
            level.add(order);
            orderCache.put(order.getOrderId(), order);
            return level;
        });
    }

    public void processCancelOrder(final OrderCancelRequest order, final long orderId) throws FieldNotFound {
        log.debug("Processing CancelOrder: {}", order);
        final var existingOrder = orderCache.get(orderId);
        if (existingOrder != null) {
            existingOrder.getPrevious().setNext(existingOrder.getNext());
            orderPool.returnOrder(existingOrder);
            queueOperation.addTransaction(createTransaction(existingOrder, 0, ExecType.CANCELED));
        }
    }

    public void processOrderCancelReplaceOrder(final OrderCancelReplaceRequest changedOrderRequest,
                                               final long oldOrderId, final long newOrderId) throws FieldNotFound {
        log.debug("Processing OrderCancelReplaceOrder: {}", changedOrderRequest);
        final var existingOrder = orderCache.get(oldOrderId);
        if (existingOrder != null) {
            existingOrder.getPrevious().setNext(existingOrder.getNext());
            queueOperation.addTransaction(createTransaction(existingOrder, 0, ExecType.REPLACED));

            Order newOrder = existingOrder.enrichWithOrderCancelReplaceOrder(changedOrderRequest, newOrderId);
            getBook(newOrder)
                    .compute(newOrder.getPrice(), (p, level) -> {
                        if (level == null) level = new PriceLevel(p);
                        level.add(newOrder);
                        orderCache.put(newOrder.getOrderId(), newOrder);
                        return level;
                    });
        }
    }

    private ConcurrentNavigableMap<Long, PriceLevel> getBook(final Order order) {
        if (order.getSide() == '1') {
            return bids;
        }
        return asks;
    }

    // Level -> Order -> items
    private void transact() { // TODO: Check if previous transaction still going on .. may be Add as Runnable in queue
        try {
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

                final var transactionPrice = NumberUtils.midValue(bidPrice, askPrice);

                while (true) { // Move out of the loop when either bid or ask executable level is empty (No orders)
                    log.debug("In while loop");
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
        } catch (Exception exception) {
            log.error("Error during transaction processing: {}", exception.getMessage(), exception);
        }
    }

    // Orders at executable priceLevels
    private void doTransact(final Order bidOrder, final Order askOrder, final long transactionPrice,
                            final PriceLevel bidLevel, final PriceLevel askLevel) {
        log.debug("Transacting bidOrder: {}, askOrder: {}, transactionPrice: {}", bidOrder, askOrder, transactionPrice);
        final var bidOrderPendingQty = bidOrder.pendingQuantities();
        final var askOrderPendingQty = askOrder.pendingQuantities();
        final Transaction bidTransaction = createTransaction(bidOrder, transactionPrice, ExecType.FILL);
        final Transaction askTransaction = createTransaction(askOrder, transactionPrice, ExecType.FILL);

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

        queueOperation.addTransaction(bidTransaction);
        queueOperation.addTransaction(askTransaction);
    }

    private Transaction createTransaction(final Order order, final long transactionPrice, final char executionType) {
        final var transactionId = NumberUtils.generateThreadLocalRandomLong();
        final var transactionTime = TimeUtils.generateInstantEpochNanoSec();
        return new Transaction(transactionId, transactionTime, order.getOrderId(),
                transactionPrice, order.getSymbolId(), order.getBrokerId(), order.getSide(), executionType);
    }

    private ScheduledExecutorService createTransactionExecutor() {
        return Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setName("Transaction-" + this.symbolId);
            return t;
        });
    }
}
