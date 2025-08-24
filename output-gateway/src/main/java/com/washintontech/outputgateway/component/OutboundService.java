package com.washintontech.outputgateway.component;

import com.washintontech.cache.service.OrderRecordService;
import com.washintontech.common.component.FixApplication;
import lombok.extern.log4j.Log4j2;
import net.openhft.chronicle.queue.ChronicleQueue;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Component
@Log4j2
public class OutboundService {

    private final ExecutorService executorService;
    private final OutboundTransactionProcessor transactionProcessor;

    public OutboundService(final ChronicleQueue chronicleQueue,
                           @Lazy final FixApplication fixApplication,
                           final OrderRecordService orderRecordService) {
        final var chronicleTailer = chronicleQueue.createTailer("OUTPUT_CONSUMER");
        this.executorService = createExecutorService();
        this.transactionProcessor = new OutboundTransactionProcessor(chronicleTailer, fixApplication,
                orderRecordService, executorService);
        executorService.submit(transactionProcessor);
    }

    private ExecutorService createExecutorService() {
        return Executors.newFixedThreadPool(1, new ThreadFactory() {
            private int count = 1;

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("OutputGw-" + count++);
                return t;
            }
        });
    }

    public void shutDown() {
        transactionProcessor.setShutdown(true);
        log.info("Shutting down OutboundService executor");
    }
}

