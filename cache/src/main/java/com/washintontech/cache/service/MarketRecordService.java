package com.washintontech.cache.service;

import net.openhft.chronicle.queue.ChronicleQueue;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;

@Component
public class MarketRecordService {
    public MarketRecordService(final ChronicleQueue chronicleQueue,
                               final OrderRecordService orderRecordService) {

        final var chronicleTailer = chronicleQueue.createTailer("CACHE_CONSUMER");
        Executors.newFixedThreadPool(1)
                .submit(new TradeTransactionProcessor(chronicleTailer, orderRecordService));
    }
}
