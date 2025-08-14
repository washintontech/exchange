//package com.washintontech.app.fromSources;
//
//public class LockFreeOrderBook1 {
//    // Price levels using ConcurrentNavigableMap
//    private final ConcurrentNavigableMap<Long, PriceLevel1> bids = new ConcurrentSkipListMap<>(Comparator.reverseOrder());
//    private final ConcurrentNavigableMap<Long, PriceLevel1> asks = new ConcurrentSkipListMap<>();
//
//    // Fast order lookup (pad to prevent false sharing)
//    @Contended
//    private final Long2ObjectOpenHashMap<Order1> orderCache;
//
//    // Memory management
//    private final OrderPool orderPool = new OrderPool(10_000);
//    private final PriceLevelPool priceLevelPool = new PriceLevelPool(1000);
//
//    // Atomic state tracking
//    private final AtomicLong sequenceNumber = new AtomicLong();
//    private volatile OrderBookSnapshot lastSnapshot;
//
//    // Add order with lock-free guarantees
//    public void addOrder(Order1 order) {
//        PriceLevel1 level = getOrCreateLevel(order);
//        level.add(order);
//
//        // Publish changes (RCU pattern)
//        publishUpdate(order, level);
//    }
//
//    private PriceLevel1 getOrCreateLevel(Order1 order) {
//        ConcurrentNavigableMap<Long, PriceLevel1> book = order.isBid() ? bids : asks;
//        return book.computeIfAbsent(order.getPrice(), p -> priceLevelPool.borrow(p));
//    }
//
//    private void publishUpdate(Order1 order, PriceLevel1 level) {
//        orderCache.put(order.getId(), order);
//        sequenceNumber.lazySet(System.nanoTime()); // Memory fence
//    }
//}
