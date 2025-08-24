package com.washintontech.inputgateway;

import com.washintontech.cache.service.MarketRecordService;
import com.washintontech.common.chronicle.ChronicleQueueOperation;
import com.washintontech.outputgateway.component.OutboundService;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;


@Log4j2
public class ShutDownListener implements ApplicationListener<ContextClosedEvent> {
    @Override
    public void onApplicationEvent(final ContextClosedEvent event) {
        event.getApplicationContext().getBean(OutboundService.class).shutDown();
        event.getApplicationContext().getBean(MarketRecordService.class).shutDown();
        event.getApplicationContext().getBean(ChronicleQueueOperation.class).shutDown();
        log.info("Matching Engine is shutting down. Bye!");
    }
}
