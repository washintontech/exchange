package com.washintontech.matchingEngine.component;

import com.washintontech.matchingEngine.model.OrderPool;
import com.washintontech.matchingEngine.model.SymbolDictionary;
import net.openhft.chronicle.queue.ExcerptAppender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import quickfix.FieldNotFound;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

@Component
public class OrderBookEngine {
    private static final Logger log = LogManager.getLogger(OrderBookEngine.class);
    private static final int PARTITIONS = Runtime.getRuntime().availableProcessors() * 2;
    private static final int MAX_QUEUE_DEPTH = 200_000;
    private final ConcurrentMap<Integer, OrderBook> orderBooks;
    private final ThreadPoolExecutor[] writers;

    // TODO: Replace with primitive map for low GC
    private final ConcurrentHashMap<Long, Integer> orderToPartitionMap;
    //ConcurrentLongToIntMap orderToPartitionMap;
    //ManyToManyConcurrentArrayQueue

    // Hotspot tracking
    private final LongAdder[] partitionCounters;

    private final AtomicInteger lastSteal;

    private final OrderPool orderPool;

    private final ExcerptAppender appender;

    public OrderBookEngine(final ExcerptAppender appender) {
        this.appender = appender;
        this.orderPool = new OrderPool();
        this.orderBooks = new ConcurrentHashMap<>();
        this.writers = new SafeExecutorService[PARTITIONS];
        this.partitionCounters = new LongAdder[PARTITIONS];
        this.lastSteal = new AtomicInteger(0);
        this.orderToPartitionMap = new ConcurrentHashMap<>();
        for (int partitionId = 0; partitionId < PARTITIONS; partitionId++) {
            partitionCounters[partitionId] = new LongAdder();
            writers[partitionId] = new SafeExecutorService(partitionId, writers);
        }
    }

    public void submitNewOrder(final NewOrderSingle requestOrder, final int brokerId, final long orderId) throws FieldNotFound {
        final var symbolId = SymbolDictionary.symbolToId((requestOrder.getSymbol().getValue()));
        int partition = getPartition(symbolId);
        partitionCounters[partition].increment();

        orderToPartitionMap.put(orderId, partition);
        writers[partition].submit(
                () -> {
                    OrderBook book = orderBooks.computeIfAbsent(symbolId,
                            script -> new OrderBook(script, orderPool, appender));
                    final var order = orderPool.borrowOrder();
                    try {
                        order.enrichOrderWithNewOrderSingle(requestOrder, orderId, brokerId, symbolId);
                    } catch (FieldNotFound e) {
                        log.error("Field not found while enriching order: {}", e.getMessage());
                        // TODO: Ensure no FieldNotFound thrown at Validate phase
                        //throw new RuntimeException(e);
                    }
                    book.processNewOrder(order);
                }
        );
    }

    public void submitCancelOrder(final OrderCancelRequest order, final long orderId) throws FieldNotFound {
        final var partitionID = orderToPartitionMap.get(orderId);
        orderToPartitionMap.remove(orderId);
        final var symbolId = SymbolDictionary.symbolToId((order.getSymbol().getValue()));
        writers[partitionID].submit(
                () -> {
                    OrderBook book = orderBooks.get(symbolId);
                    try {
                        book.processCancelOrder(order, orderId);
                    } catch (FieldNotFound e) {
                        log.error("Field not found while enriching order: {}", e.getMessage());
                        // TODO: Ensure no FieldNotFound thrown at Validate phase
                    }
                }
        );
    }

    public void submitOrderCancelReplaceOrder(final OrderCancelReplaceRequest order, final long oldOrderID,
                                              final long newOrderID) throws FieldNotFound {
        final var partitionID = orderToPartitionMap.get(oldOrderID);
        orderToPartitionMap.remove(oldOrderID);
        final var symbolId = SymbolDictionary.symbolToId((order.getSymbol().getValue()));
        writers[partitionID].submit(
                () -> {
                    OrderBook book = orderBooks.get(symbolId);
                    try {
                        book.processOrderCancelReplaceOrder(order, oldOrderID, newOrderID);
                    } catch (FieldNotFound e) {
                        log.error("Field not found while enriching order: {}", e.getMessage());
                        // TODO: Ensure no FieldNotFound thrown at Validate phase
                    }
                }
        );
    }

    private int getPartition(final Integer symbol) {
        int basePartition = Math.abs(symbol.hashCode()) % PARTITIONS;

        if (writers[basePartition].getQueue().size() > MAX_QUEUE_DEPTH * 0.85) {
            int stealAttempt = lastSteal.updateAndGet(i -> (i + 1) % PARTITIONS);
            if (writers[stealAttempt].getQueue().size() < MAX_QUEUE_DEPTH * 0.7) {
                return stealAttempt;
            }
        }

        return basePartition;
    }
}
