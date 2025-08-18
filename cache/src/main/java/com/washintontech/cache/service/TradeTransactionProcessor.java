package com.washintontech.cache.service;

import com.washintontech.cache.model.ExecutedTradeRecord;
import com.washintontech.common.chronicle.Transaction;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.wire.DocumentContext;
import quickfix.field.ExecType;

import java.util.Objects;

public class TradeTransactionProcessor implements Runnable {

    private final ExcerptTailer chronicleTailer;
    private final OrderRecordService orderRecordService;

    public TradeTransactionProcessor(final ExcerptTailer chronicleTailer,
                                     final OrderRecordService orderRecordService) {
        this.chronicleTailer = chronicleTailer;
        this.orderRecordService = orderRecordService;
    }

    @Override
    public void run() {
        try (DocumentContext dc = chronicleTailer.readingDocument()) {
            if (dc.isPresent()) {
                Transaction transaction = Objects.requireNonNull(dc.wire())
                        .read("Transaction_v1").object(Transaction.class);
                assert transaction != null;
                processTransaction(transaction);
            }
        }

    }

    // Adding executed trades
    private void processTransaction(final Transaction transaction) {
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
