package com.washintontech.common.chronicle;

import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.RollCycles;
import net.openhft.chronicle.queue.impl.StoreFileListener;
import net.openhft.chronicle.wire.WireType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class SharedQueueConfig {

    private static final Logger log = LogManager.getLogger(SharedQueueConfig.class);
    private static final String RELATIVE_QUEUE_PATH = "data/transactionQueue";

//    @Value("${chronicle.queue.rootDirectory}")
//    private String rootPath;

    @Bean
    public ChronicleQueue createQueue() {
        try {
            // Use project root directory dynamically
            //String rootPath = System.getProperty("user.dir");
            File queueDir = new File("../", RELATIVE_QUEUE_PATH);

            // Make sure directory exists
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

//    @Bean
//    public ChronicleQueue createQueue() {
//        return ChronicleQueue.singleBuilder()
//                .path("/Users/prakash.kumar1/washintontech/Exchange/common/data/transactionQueue")
//                .readOnly(true) // TODO: Queue Cleanup Process
//                .rollCycle(RollCycles.FAST_DAILY)
//                .storeFileListener(new StoreFileListener() { // TODO:
//                    @Override
//                    public void onAcquired(int cycle, File file) {
//                        LoggerFactory.getLogger(getClass())
//                                .info("Acquired queue file: {}", file);
//                    }
//
//                    @Override
//                    public void onReleased(final int cycle, final File file) {
//
//                    }
//                })
//                .blockSize(64L << 20) // 64MB blocks
//                .wireType(WireType.BINARY)
//                .build();
//
//    }

    @Bean
    public ExcerptAppender Appender(ChronicleQueue chronicleQueue) {
        return chronicleQueue.createAppender();
    }
}
