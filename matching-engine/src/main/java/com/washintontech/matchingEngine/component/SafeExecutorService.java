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
}
