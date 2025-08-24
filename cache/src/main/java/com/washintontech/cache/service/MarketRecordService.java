package com.washintontech.cache.service;

import lombok.extern.log4j.Log4j2;
import net.openhft.chronicle.queue.ChronicleQueue;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Component
@Log4j2
public class MarketRecordService {

    private final ExecutorService executorService;
    private final TradeTransactionProcessor transactionProcessor;

    public MarketRecordService(final ChronicleQueue chronicleQueue,
                               final OrderRecordService orderRecordService) {

        final var chronicleTailer = chronicleQueue.createTailer("CACHE_CONSUMER");
        this.executorService = createExecutorService();
        this.transactionProcessor = new TradeTransactionProcessor(chronicleTailer, orderRecordService, executorService);
        executorService.submit(transactionProcessor);
    }

    private ExecutorService createExecutorService() {
        return Executors.newFixedThreadPool(1, new ThreadFactory() {
            private int count = 1;

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("Cache-" + count++);
                return t;
            }
        });
    }

    public void shutDown() {
        transactionProcessor.setShutdown(true);
        log.info("Shutting down MarketRecordService executor");
    }
}
