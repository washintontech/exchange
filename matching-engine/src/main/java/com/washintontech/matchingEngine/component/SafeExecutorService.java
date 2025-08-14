package com.washintontech.matchingEngine.component;

import org.jctools.queues.MpscBlockingConsumerArrayQueue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SafeExecutorService extends ThreadPoolExecutor {
    private static final int corePoolSize = 0;
    private static final int maxPoolSize = 100_000;
    private static final int queueCapacity = 100_000;
    private static final RejectedExecutionHandler rejectionHandler = new AbortPolicy();

    public SafeExecutorService(final int partitionId, final ThreadPoolExecutor[] writers) {
        super(corePoolSize, maxPoolSize, 0L, TimeUnit.MILLISECONDS,
                new MpscBlockingConsumerArrayQueue<>(queueCapacity),
                new SafeThreadFactory(partitionId, writers),
                rejectionHandler);
    }

    public SafeExecutorService(final int partitionId, final ThreadPoolExecutor[] writers, final BlockingQueue<Runnable> blockingQueue) {
        super(corePoolSize, maxPoolSize, 0L, TimeUnit.MILLISECONDS,
                blockingQueue,
                new SafeThreadFactory(partitionId, writers),
                rejectionHandler);
    }

//    @Override
//    protected void afterExecute(Runnable r, Throwable t) {
//        super.afterExecute(r, t);
//
//        if (t == null && r instanceof Future<?>) {
//            try {
//                ((Future<?>) r).get(); // this throws the actual task exception
//            } catch (CancellationException | InterruptedException e) {
//                t = e;
//                Thread.currentThread().interrupt(); // preserve interrupt status
//            } catch (ExecutionException e) {
//                t = e.getCause();
//            }
//        }
//
//        if (t != null) {
//            System.err.println("❌ Task failed with exception: " + t.getMessage());
//            t.printStackTrace();
//            // Optional: metrics, alerting, requeueing, etc.
//        }
//    }


//    private record SafeThreadFactory(int partitionId, ThreadPoolExecutor[] writers) implements ThreadFactory {
//        private static final String THREAD_NAME_PREFIX = "OrderBookWriter-";
//
//        @Override
//        public Thread newThread(Runnable r) {
//            Thread t = new Thread(r, THREAD_NAME_PREFIX + partitionId);
//
//            t.setUncaughtExceptionHandler((thread, ex) -> {
//                //logger.error("Fatal error in partition {}: {}", i, ex.getMessage(), ex);
//                restartPartition();
//            });
//
//            t.setPriority(Thread.MAX_PRIORITY);
//            return t;
//        }
//
//        private void restartPartition() {
//            if (!writers[partitionId].isShutdown()) {
//                return;
//            }
//            synchronized (writers) {
//                if (!writers[partitionId].isShutdown() || !writers[partitionId].isTerminated()) {
//                    return;
//                }
//                //logger.warn("Restarting partition {}", partitionId);
//                writers[partitionId] = new SafeExecutorService(partitionId, writers, writers[partitionId].getQueue());
//            }
//        }
//    }
}
