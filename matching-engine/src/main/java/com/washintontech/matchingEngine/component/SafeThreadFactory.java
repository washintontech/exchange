package com.washintontech.matchingEngine.component;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

public class SafeThreadFactory implements ThreadFactory {
    private static final String THREAD_NAME_PREFIX = "OrderBookWriter-";
    private int partitionId;
    private ThreadPoolExecutor[] writers;

    public SafeThreadFactory(final int partitionId, final ThreadPoolExecutor[] writers) {
        this.partitionId = partitionId;
        this.writers = writers;
    }

    @Override
    public Thread newThread(final Runnable r) {
        Thread t = new Thread(r, THREAD_NAME_PREFIX + partitionId);

        t.setUncaughtExceptionHandler((thread, ex) -> {
            //logger.error("Fatal error in partition {}: {}", i, ex.getMessage(), ex);
            restartPartition();
        });

        t.setPriority(Thread.MAX_PRIORITY);
        return t;
    }

    private void restartPartition() {
        if (!writers[partitionId].isShutdown()) {
            return;
        }
        synchronized (writers) {
            if (!writers[partitionId].isShutdown() || !writers[partitionId].isTerminated()) {
                return;
            }
            //logger.warn("Restarting partition {}", partitionId);
            writers[partitionId] = new SafeExecutorService(partitionId, writers, writers[partitionId].getQueue());
        }
    }
}
