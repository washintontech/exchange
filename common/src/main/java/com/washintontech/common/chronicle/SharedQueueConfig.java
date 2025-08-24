package com.washintontech.common.chronicle;

import lombok.extern.log4j.Log4j2;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.RollCycles;
import net.openhft.chronicle.queue.impl.StoreFileListener;
import net.openhft.chronicle.wire.WireType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
@Log4j2
public class SharedQueueConfig {
    private static final String RELATIVE_QUEUE_PATH = "data/transactionQueue";

    @Bean
    public ChronicleQueue createQueue() {
        try {
            File queueDir = new File("../", RELATIVE_QUEUE_PATH);
            if (!queueDir.exists()) {
                log.info("Creating Chronicle Queue directory: {}", queueDir.getAbsolutePath());
                boolean created = queueDir.mkdirs();
                if (!created) {
                    throw new RuntimeException("Could not create queue directory: " + queueDir.getAbsolutePath());
                }
            }

            log.info("Initializing Chronicle Queue at: {}", queueDir.getAbsolutePath());

            return ChronicleQueue.singleBuilder(queueDir)
                    .readOnly(false)  // set to true only if you're just reading the queue
                    .rollCycle(RollCycles.FAST_DAILY)
                    .storeFileListener(new StoreFileListener() {
                        @Override
                        public void onAcquired(int cycle, File file) {
                            log.info("Acquired queue file: {}", file);
                        }

                        @Override
                        public void onReleased(int cycle, File file) {
                            log.info("Released queue file: {}", file);
                        }
                    })
                    .blockSize(64L << 20) // 64MB
                    .wireType(WireType.BINARY)
                    .build();

        } catch (Exception e) {
            log.error("Failed to initialize ChronicleQueue", e);
            throw e; // Let Spring fail fast with full trace
        }
    }

    @Bean
    public ExcerptAppender Appender(ChronicleQueue chronicleQueue) {
        return chronicleQueue.createAppender();
    }
}
