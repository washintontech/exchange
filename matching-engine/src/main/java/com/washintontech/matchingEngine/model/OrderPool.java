package com.washintontech.matchingEngine.model;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class OrderPool implements AutoCloseable {

    private static final int THREAD_CACHE_BATCH_SIZE = 32;
    private static final int ACTIVE_ORDER_CAPACITY = 100_000;

    private final Order[] orderPool; // All updates in LIFO order to avoid cache miss

    private final AtomicInteger[] freeOrderIndexes; // Stores orderPool index in LIFO order. Index of start of thread cache.
    //private final AtomicInteger nextFreeOrderIndex; // Stores next order index in allFreeOrderIndexes
    private final AtomicInteger freeOrderIndexesSize;

    private final ThreadLocal<int[]> localCache; // Stores order's index in orderPool in LIFO order.
    private final ThreadLocal<Integer> localCacheSize; // Stores size of int[] hold by localCache
    private final AtomicBoolean closed;

    public OrderPool() {
        this.freeOrderIndexes = new AtomicInteger[ACTIVE_ORDER_CAPACITY];
        this.freeOrderIndexesSize = new AtomicInteger(ACTIVE_ORDER_CAPACITY);
        this.orderPool = new Order[ACTIVE_ORDER_CAPACITY];
        this.localCache = ThreadLocal.withInitial(() -> new int[THREAD_CACHE_BATCH_SIZE]);
        this.localCacheSize = ThreadLocal.withInitial(() -> 0);
        this.closed = new AtomicBoolean(false);

        for (int orderPoolIndex = 0; orderPoolIndex < orderPool.length; orderPoolIndex++) {
            orderPool[orderPoolIndex] = new Order(orderPoolIndex);
            freeOrderIndexes[orderPoolIndex] = new AtomicInteger(orderPoolIndex);
        }
    }

    public Order borrowOrder() {
        final int[] threadCache = localCache.get();
        int size = localCacheSize.get();
        if (size > 0) {
            final int index = threadCache[--size]; // LIFO
            localCacheSize.set(size);
            return orderPool[index];
        }

        return borrowOrderFromPool(threadCache);
    }

    private Order borrowOrderFromPool(final int[] threadCache) {
        int freeOrderFromEnd;
        do {
            freeOrderFromEnd = freeOrderIndexesSize.get(); // LIFO
            if (freeOrderFromEnd - THREAD_CACHE_BATCH_SIZE < 0) {
                // TODO: Add more Orders in Pool.
                throw new RuntimeException(); // PoolExhaustedException
            }
        } while (!freeOrderIndexesSize.compareAndSet(freeOrderFromEnd,  // TODO: Adaptive Batching if CAS failures exceed 5% of operations
                freeOrderFromEnd - THREAD_CACHE_BATCH_SIZE));

        freeOrderFromEnd = freeOrderIndexesSize.get();
        for (int i = 0; i < THREAD_CACHE_BATCH_SIZE; i++) {
            threadCache[i] = freeOrderIndexes[freeOrderFromEnd + i].get();
        }

        localCacheSize.set(THREAD_CACHE_BATCH_SIZE - 1);
        return orderPool[threadCache[THREAD_CACHE_BATCH_SIZE - 1]];
    }

    public void returnOrder(final Order order) {
        clearOrder(order);
        final int index = order.getPoolIndex();

        int[] local = localCache.get();
        int size = localCacheSize.get();
        if (size < THREAD_CACHE_BATCH_SIZE) {
            local[size++] = index; // LIFO
            localCacheSize.set(size);
            return;
        }

        returnOrderToPool(index);
    }

    private void clearOrder(final Order order) {
        // TODO:
    }

    private void returnOrderToPool(final int index) {
        int freeOrderFromEnd;
        do {
            freeOrderFromEnd = freeOrderIndexesSize.get(); // LIFO
        } while (freeOrderIndexesSize.compareAndSet(freeOrderFromEnd, freeOrderFromEnd + 1));

        freeOrderIndexes[freeOrderFromEnd].set(index);
    }


    @Override
    public void close() throws Exception {
        if (closed.compareAndSet(false, true)) {
            try {
                // TODO: Release resources
                localCache.remove();
            } catch (Exception e) {
                throw new Exception("Failed to close OrderPool", e);
            }
        }
    }
}
