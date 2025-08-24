package com.washintontech.common.chronicle;

import lombok.extern.log4j.Log4j2;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.wire.DocumentContext;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;

@Component
@Log4j2
public class ChronicleQueueOperation {
    private final ExcerptAppender appender;
    private final BlockingQueue<Transaction> transactionQueue;
    private final ExecutorService executorService;
    private boolean isShutdown;

    public ChronicleQueueOperation(final ExcerptAppender appender) {
        this.appender = appender;
        this.transactionQueue = new LinkedBlockingQueue<>();
        this.executorService = createExecutorService();
        executorService.submit(this::addToChronicleQueue);
    }

    public void addTransaction(Transaction transaction) {
        final var offer = transactionQueue.offer(transaction);
        if (!offer) {
            log.error("Failed to add transaction to queue: {}", transaction);
            throw new RuntimeException("Transaction queue is full"); // TODO: Handle this more gracefully
        }
    }

    private void addToChronicleQueue() {
        try {
            while (!isShutdown) {
                final var transaction = transactionQueue.take(); // Blocking call, waits until an item is available
                try (DocumentContext dc = appender.writingDocument()) {
                    Objects.requireNonNull(dc.wire())
                            .write("Transaction_v1")
                            .object(transaction);
                }
            }
            executorService.shutdown();
        } catch (Exception e) {
            log.error("Error in QueueOperation executor: ", e);
        }
    }

    private ExecutorService createExecutorService() {
        return Executors.newFixedThreadPool(1, new ThreadFactory() {
            private int count = 1;

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("ChronicleQ-" + count++);
                return t;
            }
        });
    }

    public void shutDown() {
        isShutdown = true;
        log.info("Shutting down ChronicleQueueOperation executor");
    }
}
