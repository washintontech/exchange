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
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
public class OrderBook {
    private final int symbolId;
    private final OrderPool orderPool;
    private final TreeMap<Long, PriceLevel> bids;
    private final TreeMap<Long, PriceLevel> asks;
    private final Map<Long, Order> orderCache;
    private final ChronicleQueueOperation queueOperation;

    public OrderBook(final int symbolId, final OrderPool orderPool, final ChronicleQueueOperation queueOperation) {
        this.symbolId = symbolId;
        this.orderPool = orderPool;
        this.bids = new TreeMap<>(Comparator.reverseOrder());
        this.asks = new TreeMap<>();
        this.orderCache = new ConcurrentHashMap<>();
        this.queueOperation = queueOperation;
    }

    public void processNewOrder(final Order order) {
        log.debug("Processing order: {}", order);
        if (order.getSide() == 'B') {
            if (asks.isEmpty() || asks.firstKey() > order.getPrice()) {
                processNoOrderMatches(bids, order);
            } else {
                processOrderAtMatchPriceLevel(order, asks);
            }
        } else {
            if (bids.isEmpty() || bids.firstKey() < order.getPrice()) {
                processNoOrderMatches(asks, order);
            } else {
                processOrderAtMatchPriceLevel(order, bids);
            }
        }
    }

    private void processNoOrderMatches(TreeMap<Long, PriceLevel> priceLevelMap, final Order order) {
        final var level = priceLevelMap.compute(order.getPrice(), (price, priceLevel) -> {
            if (priceLevel == null) {
                priceLevel = new PriceLevel(order.getPrice());
            }
            priceLevel.add(order);
            orderCache.put(order.getOrderId(), order);
            return priceLevel;
        });
        priceLevelMap.putIfAbsent(order.getPrice(), level);
    }

    private void processOrderAtMatchPriceLevel(final Order order,
                                               final TreeMap<Long, PriceLevel> oppositePriceLevel) {
        final var oppPriceLevel = oppositePriceLevel.firstEntry().getValue();
        var oldestOppositOrder = oppPriceLevel.getOldestOrder();
        while (oldestOppositOrder != null && order.pendingQuantities() > 0) {
            oldestOppositOrder = tradeOrderAgainstOppOrder(order, oldestOppositOrder, oppPriceLevel);
        }

        if (oldestOppositOrder == null) { // All orders at PriceLevel exhausted
            oppositePriceLevel.pollFirstEntry();
        }

        if (order.pendingQuantities() > 0) {
            processNewOrder(order); // All orders at PriceLevel exhausted, Look match in next PriceLevel
        } else {
            orderPool.returnOrder(order);
        }
    }

    private Order tradeOrderAgainstOppOrder(final Order order, Order oldestOppositeOrder,
                                            final PriceLevel oppositePriceLevel) {

        if (order.pendingQuantities() >= oldestOppositeOrder.pendingQuantities()) {
            orderCache.remove(oldestOppositeOrder.getOrderId());
            oppositePriceLevel.removeOldest();
            order.reduceQuantity(oldestOppositeOrder.pendingQuantities());

            // Execution price is oldest order's price
            final Transaction transaction1 = createTransaction(order, oldestOppositeOrder.getPrice(), ExecType.FILL, oldestOppositeOrder.pendingQuantities());
            final Transaction transaction2 = createTransaction(oldestOppositeOrder, oldestOppositeOrder.getPrice(), ExecType.FILL, oldestOppositeOrder.pendingQuantities());
            queueOperation.addTransaction(transaction1);
            queueOperation.addTransaction(transaction2);

            orderPool.returnOrder(oldestOppositeOrder);
            oldestOppositeOrder = oppositePriceLevel.getOldestOrder(); // Next Order in the PriceLevel
        } else {
            final Transaction transaction1 = createTransaction(order, oldestOppositeOrder.getPrice(), ExecType.FILL, order.pendingQuantities());
            final Transaction transaction2 = createTransaction(oldestOppositeOrder, oldestOppositeOrder.getPrice(), ExecType.FILL, order.pendingQuantities());
            queueOperation.addTransaction(transaction1);
            queueOperation.addTransaction(transaction2);

            oldestOppositeOrder.reduceQuantity(order.pendingQuantities());
            order.reduceQuantity(order.pendingQuantities());
        }
        return oldestOppositeOrder;
    }

    public void processCancelOrder(final OrderCancelRequest order, final long orderId) throws FieldNotFound {
        log.debug("Processing CancelOrder: {}", order);
        final var existingOrder = orderCache.get(orderId);
        if (existingOrder != null) {
            existingOrder.getPrevious().setNext(existingOrder.getNext());
            queueOperation.addTransaction(createTransaction(existingOrder, 0, ExecType.CANCELED, existingOrder.pendingQuantities()));
            orderCache.remove(existingOrder.getOrderId());
            orderPool.returnOrder(existingOrder);
        } else {
            log.debug("No order found with orderId : {}, Canceling OrderCancelRequest: {}", orderId, order);
            sendRejectedResponse();
        }
    }

    public void processOrderCancelReplaceOrder(final OrderCancelReplaceRequest changedOrderRequest,
                                               final long oldOrderId, final long newOrderId) throws FieldNotFound {
        log.debug("Processing OrderCancelReplaceOrder: {}", changedOrderRequest);
        final var existingOrder = orderCache.get(oldOrderId);
        if (existingOrder != null) {
            existingOrder.getPrevious().setNext(existingOrder.getNext());
            queueOperation.addTransaction(createTransaction(existingOrder, 0, ExecType.REPLACED, existingOrder.pendingQuantities()));
            final Order newOrder = existingOrder.enrichWithOrderCancelReplaceOrder(changedOrderRequest, newOrderId);
            orderCache.remove(oldOrderId);
            processNewOrder(newOrder);
        } else {
            log.debug("No order found with orderId : {}, Canceling OrderCancelReplaceRequest: {}", oldOrderId, newOrderId);
            sendRejectedResponse();
        }
    }

    private Transaction createTransaction(final Order order, final long transactionPrice, final char executionType,
                                          final long quantity) {
        final var transactionId = NumberUtils.generateThreadLocalRandomLong();
        final var transactionTime = TimeUtils.generateInstantEpochNanoSec();
        return new Transaction(transactionId, transactionTime, order.getOrderId(),
                transactionPrice, order.getSymbolId(), order.getBrokerId(), order.getSide(), executionType, quantity);
    }

    private void sendRejectedResponse() {
        // TODO: Send Rejected response to the client
    }
}
