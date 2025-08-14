package com.washintontech.outputgateway.component;

import com.washintontech.cache.service.OrderRecordService;
import com.washintontech.common.component.FixApplication;
import net.openhft.chronicle.queue.ChronicleQueue;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;

@Component
//@RequiredArgsConstructor
public class OutboundService {

//    private final ChronicleQueue chronicleQueue;
//    //@Lazy
//    private final FixApplication fixApplication;

    public OutboundService(final ChronicleQueue chronicleQueue,
                           @Lazy final FixApplication fixApplication,
                           final OrderRecordService orderRecordService) {
        final var chronicleTailer = chronicleQueue.createTailer("OUTPUT_CONSUMER");
        Executors.newFixedThreadPool(1)
                .submit(new OutboundTransactionProcessor(chronicleTailer, fixApplication, orderRecordService));
    }
}

