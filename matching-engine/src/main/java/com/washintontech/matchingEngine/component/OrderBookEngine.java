package com.washintontech.matchingEngine.component;

import com.washintontech.common.chronicle.ChronicleQueueOperation;
import com.washintontech.common.utils.SymbolDictionary;
import com.washintontech.matchingEngine.model.OrderPool;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import quickfix.FieldNotFound;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadPoolExecutor;

@Component
@Log4j2
public class OrderBookEngine {
    private static final int PARTITIONS = Runtime.getRuntime().availableProcessors() * 2;
    private final ConcurrentMap<Integer, OrderBook> orderBooks;
    private final ThreadPoolExecutor[] writers;
    private final OrderPool orderPool;
    private final ChronicleQueueOperation queueOperation;

    public OrderBookEngine(final ChronicleQueueOperation queueOperation) {
        this.queueOperation = queueOperation;
        this.orderPool = new OrderPool();
        this.orderBooks = new ConcurrentHashMap<>();
        this.writers = new SafeExecutorService[PARTITIONS];
        for (int partitionId = 0; partitionId < PARTITIONS; partitionId++) {
            writers[partitionId] = getSafeExecutorService(partitionId);
        }
    }

    public void submitNewOrder(final NewOrderSingle requestOrder, final int brokerId, final long orderId) throws FieldNotFound {
        final var symbolId = SymbolDictionary.symbolToId((requestOrder.getSymbol().getValue()));
        final int partition = getPartition(symbolId);
        writers[partition].submit(
                () -> {
                    OrderBook book = orderBooks.computeIfAbsent(symbolId,
                            script -> new OrderBook(script, orderPool, queueOperation));
                    final var order = orderPool.borrowOrder();
                    try {
                        order.enrichOrderWithNewOrderSingle(requestOrder, orderId, brokerId, symbolId);
                    } catch (FieldNotFound e) {
                        // Unexpected case: Fields are already validated
                        log.error("Field not found while enriching order: {}", e.getMessage());
                        return;
                    }
                    book.processNewOrder(order);
                }
        );
    }

    public void submitCancelOrder(final OrderCancelRequest order, final long orderId) throws FieldNotFound {
        final var symbolId = SymbolDictionary.symbolToId((order.getSymbol().getValue()));
        final int partition = getPartition(symbolId);
        writers[partition].submit(
                () -> {
                    OrderBook book = orderBooks.get(symbolId);
                    try {
                        book.processCancelOrder(order, orderId);
                    } catch (FieldNotFound e) {
                        // Unexpected case: Fields are already validated
                        log.error("Field not found while enriching order: {}", e.getMessage());
                    }
                }
        );
    }

    public void submitOrderCancelReplaceOrder(final OrderCancelReplaceRequest order, final long oldOrderID,
                                              final long newOrderID) throws FieldNotFound {
        final var symbolId = SymbolDictionary.symbolToId((order.getSymbol().getValue()));
        final int partition = getPartition(symbolId);
        writers[partition].submit(
                () -> {
                    OrderBook book = orderBooks.get(symbolId);
                    try {
                        book.processOrderCancelReplaceOrder(order, oldOrderID, newOrderID);
                    } catch (FieldNotFound e) {
                        // Unexpected case: Fields are already validated
                        log.error("Field not found while enriching order: {}", e.getMessage());
                    }
                }
        );
    }

    private int getPartition(final Integer symbol) {
        return Math.abs(symbol.hashCode()) % PARTITIONS;
    }

    private @NonNull SafeExecutorService getSafeExecutorService(final int partitionId) {
        return new SafeExecutorService(partitionId, writers);
    }
}
