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

import static com.washintontech.matchingEngine.util.NumberUtils.generateThreadLocalRandomLong;

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

    public long submitNewOrder(final NewOrderSingle requestOrder, final int brokerId) throws FieldNotFound {
        final var symbolId = SymbolDictionary.symbolToId((requestOrder.getSymbol().getValue()));
        int partition = getPartition(symbolId);
        partitionCounters[partition].increment();
        final var orderId = generateThreadLocalRandomLong();
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
        return orderId;
    }

    public long submitCancelOrder(final OrderCancelRequest order, final int brokerId) throws FieldNotFound {
        final var orderID = Long.valueOf(order.getOrderID().getValue());
        final var partitionID = orderToPartitionMap.get(orderID);
        orderToPartitionMap.remove(orderID);
        final var symbolId = SymbolDictionary.symbolToId((order.getSymbol().getValue()));
        writers[partitionID].submit(
                () -> {
                    OrderBook book = orderBooks.get(symbolId);
                    try {
                        book.processCancelOrder(order);
                    } catch (FieldNotFound e) {
                        log.error("Field not found while enriching order: {}", e.getMessage());
                        // TODO: Ensure no FieldNotFound thrown at Validate phase
                    }
                }
        );
        return Long.parseLong(order.getOrderID().getValue());
    }

    public long submitOrderCancelReplaceOrder(final OrderCancelReplaceRequest order, final int brokerId) throws FieldNotFound {
        final var orderID = Long.valueOf(order.getOrderID().getValue());
        final var partitionID = orderToPartitionMap.get(orderID);
        orderToPartitionMap.remove(orderID);
        final var symbolId = SymbolDictionary.symbolToId((order.getSymbol().getValue()));
        final var orderId = generateThreadLocalRandomLong();
        writers[partitionID].submit(
                () -> {
                    OrderBook book = orderBooks.get(symbolId);
                    try {
                        book.processOrderCancelReplaceOrder(order, orderId);
                    } catch (FieldNotFound e) {
                        log.error("Field not found while enriching order: {}", e.getMessage());
                        // TODO: Ensure no FieldNotFound thrown at Validate phase
                    }
                }
        );
        return Long.parseLong(order.getOrderID().getValue());
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


    ///////

//    public BrokerTradeResponse submitOrder(final BrokerTradeRequest tradeRequest) {
//        int partition = getPartition(tradeRequest.getScript());
//        partitionCounters[partition].increment();
//        final var orderId = generateThreadLocalRandomLong(); // TODO: make it universal across the jvm
//
//        writers[partition].submit(
//                () -> {
//                    OrderBook book = orderBooks.computeIfAbsent(tradeRequest.getScript(),
//                            script -> new OrderBook(script, orderPool, appender));
//                    final var order = orderPool.borrowOrder();
//                    order.enrichOrder(tradeRequest, orderId);
//                    book.process(order);
//                }
//        );
//
//        return new BrokerTradeResponse(tradeRequest, orderId, OrderStatus.ACCEPTED);
//    }

//    private int getPartition(Script script) {
//        int basePartition = Math.abs(script.hashCode()) % PARTITIONS;
//
//        if (writers[basePartition].getQueue().size() > MAX_QUEUE_DEPTH * 0.85) {
//            int stealAttempt = lastSteal.updateAndGet(i -> (i + 1) % PARTITIONS);
//            if (writers[stealAttempt].getQueue().size() < MAX_QUEUE_DEPTH * 0.7) {
//                return stealAttempt;
//            }
//        }
//
//        return basePartition;
//    }

}
