//package com.washintontech.app.fromSources;
//
//import java.util.concurrent.BlockingQueue;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ConcurrentMap;
//import java.util.concurrent.ConcurrentNavigableMap;
//import java.util.concurrent.ConcurrentSkipListMap;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.LinkedBlockingQueue;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.concurrent.atomic.LongAdder;
//
//public class ScalableOrderBookEngine {
//    // Configuration
//    private static final int PARTITIONS = Runtime.getRuntime().availableProcessors() * 2;
//    private static final int MAX_QUEUE_DEPTH = 100_000;
//
//    // Core data structures
//    private final ConcurrentMap<String, OrderBook> orderBooks = new ConcurrentHashMap<>();
//    private final ExecutorService[] writers;
//    private final BlockingQueue<Runnable>[] queues;
//
//    // Hotspot tracking
//    private final LongAdder[] partitionCounters;
//    private final AtomicInteger lastSteal = new AtomicInteger(0);
//
//    public ScalableOrderBookEngine() {
//        this.writers = new ExecutorService[PARTITIONS];
//        this.queues = new BlockingQueue[PARTITIONS];
//        this.partitionCounters = new LongAdder[PARTITIONS];
//
//        for (int i = 0; i < PARTITIONS; i++) {
//            queues[i] = new LinkedBlockingQueue<>(MAX_QUEUE_DEPTH);
//            partitionCounters[i] = new LongAdder();
//            writers[i] = Executors.newSingleThreadExecutor(r -> {
//                Thread t = new Thread(r, "OrderBookWriter-" + i);
//                t.setPriority(Thread.MAX_PRIORITY);
//                return t;
//            });
//        }
//    }
//
//
//    writers[i] = Executors.newSingleThreadExecutor(r -> {
//        Thread t = new Thread(r, "OrderBookWriter-" + i);
//        t.setPriority(Thread.MAX_PRIORITY);
//
//        // Critical addition for production
//        t.setUncaughtExceptionHandler((thread, ex) -> {
//            logger.error("Fatal error in partition {}: {}", i, ex.getMessage(), ex);
//            restartPartition(i); // See Solution 2
//        });
//
//        return t;
//    });
//
//    private void restartPartition(int partitionId) {
//        synchronized(writers) {
//            if (writers[partitionId].isShutdown()) {
//                logger.warn("Restarting partition {}", partitionId);
//
//                // Recreate executor
//                writers[partitionId] = createPartitionExecutor(partitionId);
//
//                // Re-process any queued orders
//                drainQueueToNewExecutor(partitionId);
//            }
//        }
//    }
//
//    private void drainQueueToNewExecutor(int partitionId) {
//        List<Runnable> pending = new ArrayList<>();
//        queues[partitionId].drainTo(pending);
//
//        pending.forEach(task -> {
//            try {
//                writers[partitionId].submit(task);
//            } catch (RejectedExecutionException e) {
//                logger.error("Failed to resubmit task", e);
//            }
//        });
//    }
//
//
//    // Hash-based partitioning with work stealing
//    private int getPartition(String script) {
//        int basePartition = Math.abs(script.hashCode()) % PARTITIONS;
//
//        // Check for hotspot (queue depth)
//        if (queues[basePartition].size() > MAX_QUEUE_DEPTH / 2) {
//            int stealAttempt = lastSteal.updateAndGet(i -> (i + 1) % PARTITIONS);
//            if (queues[stealAttempt].size() < MAX_QUEUE_DEPTH / 4) {
//                return stealAttempt;
//            }
//        }
//
//        return basePartition;
//    }
//
//    public void submitOrder(String script, Order1 order) {
//        int partition = getPartition(script);
//        partitionCounters[partition].increment();
//
//        queues[partition].offer(() -> {
//            OrderBook book = orderBooks.computeIfAbsent(script, s -> new OrderBook());
//            book.process(order);
//        });
//    }
//
//    // Graceful shutdown
//    public void shutdown() {
//        for (ExecutorService writer : writers) {
//            writer.shutdown();
//        }
//    }
//
//    // Monitoring
//    public void printStats() {
//        for (int i = 0; i < PARTITIONS; i++) {
//            System.out.printf("Partition %d: %d tasks, queue %d%n",
//                    i, partitionCounters[i].sum(), queues[i].size());
//        }
//    }
//}
//
//// Lock-free order book implementation
//class OrderBook {
//    private final ConcurrentNavigableMap<Long, PriceLevel1> bids =
//            new ConcurrentSkipListMap<>(Comparator.reverseOrder());
//    private final ConcurrentNavigableMap<Long, PriceLevel1> asks =
//            new ConcurrentSkipListMap<>();
//
//    // Single-threaded processing guaranteed by partition
//    public void process(Order1 order) {
//        ConcurrentNavigableMap<Long, PriceLevel1> book = order.isBid() ? bids : asks;
//        book.compute(order.getPrice(), (p, level) -> {
//            if (level == null) level = new PriceLevel1(p);
//            level.add(order);
//            return level;
//        });
//
//        matchEngine();
//    }
//
//    private void matchEngine() {
//        // Implementation of matching logic
//    }
//}
//
//        /*
//        Disruptor<OrderEvent> disruptor = new Disruptor<>(
//                OrderEvent::new,
//                1024,
//                executor,
//                ProducerType.MULTI,
//                new BusySpinWaitStrategy() // For <100ns latency
//        );
//        */
//
////        int bufferSize = 1 << 18; // 262144 slots (power-of-2)
////        ThreadFactory threadFactory = new ThreadFactory() {
////            @Override
////            public Thread newThread(Runnable r) {
////                Thread t = new Thread(r, "Disruptor-Processor");
////                t.setPriority(Thread.MAX_PRIORITY);
////                return t;
////            }
////        };
////
////        Disruptor<OrderEvent> disruptor = new Disruptor<>(
////                new OrderEventFactory(),
////                bufferSize,
////                threadFactory,
////                ProducerType.SINGLE, // Multiple publishers
////                new YieldingWaitStrategy() // Balance latency/CPU
////        );
////
////        RingBuffer<OrderEvent> ringBuffer = disruptor.start();
////
