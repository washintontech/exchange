package com.washintontech.matchingEngine.component;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

@Log4j2
@RequiredArgsConstructor
public class SafeThreadFactory implements ThreadFactory {
    private static final String THREAD_NAME_PREFIX = "OrderBook-";
    private final int partitionId;
    private final ThreadPoolExecutor[] writers;

    @Override
    public Thread newThread(final @NonNull Runnable r) {
        Thread t = new Thread(r, THREAD_NAME_PREFIX + partitionId);

        t.setUncaughtExceptionHandler((thread, ex) -> {
            log.error("Fatal error in partition: {}. Restarting partition: {}", partitionId, ex.getMessage(), ex);
            restartPartition();
        });

        t.setPriority(Thread.MAX_PRIORITY);
        return t;
    }

    private void restartPartition() {
        if (!writers[partitionId].isShutdown()) return;
        synchronized (writers) {
            if (!writers[partitionId].isShutdown() || !writers[partitionId].isTerminated()) {
                return;
            }
            log.warn("Restarting partition {}", partitionId);
            writers[partitionId] = new SafeExecutorService(partitionId, writers, writers[partitionId].getQueue());
        }
    }
}
