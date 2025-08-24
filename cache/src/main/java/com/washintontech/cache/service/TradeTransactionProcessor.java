package com.washintontech.cache.service;

import com.washintontech.cache.model.ExecutedTradeRecord;
import com.washintontech.common.chronicle.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.wire.DocumentContext;
import quickfix.field.ExecType;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.LockSupport;

@Log4j2
@RequiredArgsConstructor
public class TradeTransactionProcessor implements Runnable {

    private final ExcerptTailer chronicleTailer;
    private final OrderRecordService orderRecordService;
    private final ExecutorService executorService;
    @Setter
    private boolean isShutdown;

    @Override
    public void run() {
        try {
            while (!isShutdown) {
                try (DocumentContext dc = chronicleTailer.readingDocument()) {
                    if (!dc.isPresent()) {
                        LockSupport.parkNanos(1_000_000); // 1ms sleep
                        continue;
                    }
                    log.debug("Processing transaction from Chronicle Queue");
                    Transaction transaction = Objects.requireNonNull(dc.wire())
                            .read("Transaction_v1").object(Transaction.class);
                    assert transaction != null;
                    processTransaction(transaction);
                }
            }
            executorService.shutdown();
        } catch (Exception e) {
            log.error("Error in TradeTransactionProcessor: ", e);
        }
    }

    // Adding executed trades
    private void processTransaction(final Transaction transaction) {
        log.debug("Processing transaction: {}", transaction);
        switch (transaction.getExecutionType()) {
            case ExecType.FILL: {
                final var orderRecords = orderRecordService.tradeOrderRecord(transaction.getOrderId());
                var singleOrderRecord = orderRecords.getSingleOrderRecords().peek();
                singleOrderRecord.getTrades().add(
                        new ExecutedTradeRecord(transaction.getTransactionId(),
                                transaction.getOrderId(),
                                transaction.getTransactionPrice(),
                                transaction.getQuantity(),
                                transaction.getTransactionTime()));
                singleOrderRecord.getRemainingQty().addAndGet(-transaction.getQuantity());
                log.debug("orderRecords: {}", orderRecords);
            }
            case ExecType.CANCELED: {
            }
            case ExecType.REPLACED: {
            }
            case ExecType.REJECTED: {
            }
        }
    }
}
