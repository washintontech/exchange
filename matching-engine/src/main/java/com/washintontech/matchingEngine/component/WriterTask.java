//package com.washintontech.app.model;
//
//import com.lmax.disruptor.YieldingWaitStrategy;
//import com.lmax.disruptor.dsl.Disruptor;
//import com.lmax.disruptor.dsl.ProducerType;
//
//import java.util.concurrent.ConcurrentMap;
//import java.util.concurrent.ThreadFactory;
//
//public class WriterTask implements Runnable {
//
//    //private static final int MAX_QUEUE_DEPTH = 100_000;
//    private final ConcurrentMap<String, OrderBook> orderBooks;
//    private final Disruptor<OrderEvent> disruptor;
//
//    public WriterTask(final ConcurrentMap<String, OrderBook> orderBooks) {
//        this.orderBooks = orderBooks;
//
//        int bufferSize = 1 << 18; // 262144 slots (power-of-2)
//        ThreadFactory threadFactory = new ThreadFactory() {
//            @Override
//            public Thread newThread(Runnable r) {
//                Thread t = new Thread(r, "Disruptor-Processor");
//                t.setPriority(Thread.MAX_PRIORITY);
//                t.setUncaughtExceptionHandler((thread, ex) -> {
//                    // 🔥 Production-safe handling
//                    System.err.println("Uncaught exception in thread " + thread.getName() + ": " + ex);
//                    ex.printStackTrace();
//
//                    // Custom recovery logic — logging, alerting, restart, etc.
//                    restartDisruptor();
//                });
//                return t;
//            }
//        };
//
//        this.disruptor = new Disruptor<>(
//                new OrderEventFactory(),
//                bufferSize,
//                threadFactory,
//                ProducerType.SINGLE,
//                new YieldingWaitStrategy()
//        );
//    }
//    //private final RingBuffer<Runnable>[] queues;
//
//    @Override
//    public void run() {
//        disruptor.start();
//    }
//
//    public void addOrder(Order order) {
//        disruptor
//    }
//}
