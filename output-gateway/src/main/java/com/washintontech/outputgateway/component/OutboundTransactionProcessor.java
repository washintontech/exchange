package com.washintontech.outputgateway.component;

import com.washintontech.cache.service.OrderRecordService;
import com.washintontech.common.chronicle.Transaction;
import com.washintontech.common.component.FixApplication;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.wire.DocumentContext;
import quickfix.FieldNotFound;
import quickfix.field.Account;
import quickfix.field.ClOrdID;
import quickfix.field.ExecID;
import quickfix.field.ExecType;
import quickfix.field.OrdStatus;
import quickfix.field.OrderID;
import quickfix.field.TransactTime;
import quickfix.fix44.ExecutionReport;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.UUID;

import static com.washintontech.cache.model.OrderRecords.EXEC_STRING;
import static com.washintontech.cache.model.OrderRecords.UTC_STRING;

public class OutboundTransactionProcessor implements Runnable {

    private final ExcerptTailer chronicleTailer;
    private final FixApplication fixApplication;
    private final OrderRecordService orderRecordService;

    public OutboundTransactionProcessor(final ExcerptTailer chronicleTailer,
                                        final FixApplication fixApplication,
                                        final OrderRecordService orderRecordService) {
        this.chronicleTailer = chronicleTailer;
        this.fixApplication = fixApplication;
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

    private void processTransaction(final Transaction transaction) {
        final var orderRecord = orderRecordService.tradeOrderRecord(transaction.getOrderId());
        final var executionReport = executionReport(transaction, orderRecord.getAccount(),
                orderRecord.getLatestClientOrderId());
        try {
            orderRecordService.addExecutionReport(executionReport, transaction.getBrokerId());
        } catch (FieldNotFound e) {
            throw new RuntimeException(e); // Not possible, already validated
        }
        fixApplication.sendToBroker(transaction.getBrokerId(), executionReport);
    }

    private ExecutionReport executionReport(final Transaction transaction, final Account account,
                                            final String latestClientOrderId) {
        final var executionReport = new ExecutionReport();
        executionReport.set(new ExecType(transaction.getExecutionType()));
        executionReport.set(ordStatus(transaction.getExecutionType()));
        executionReport.set(new OrderID(String.valueOf(transaction.getOrderId())));
        executionReport.set(new ClOrdID(latestClientOrderId));
        executionReport.set(account);
        executionReport.set(new ExecID(EXEC_STRING + UUID.randomUUID()));
        executionReport.set(new TransactTime(LocalDateTime.now(ZoneId.of(UTC_STRING))));
        return executionReport;
    }

    private OrdStatus ordStatus(final char executionType) {
        switch (executionType) {
            case ExecType.FILL:
                return new OrdStatus(OrdStatus.FILLED);
            case ExecType.CALCULATED:
                return new OrdStatus(OrdStatus.CANCELED);
            case ExecType.REPLACED:
                return new OrdStatus(OrdStatus.REPLACED);
            default:
                return new OrdStatus(OrdStatus.EXPIRED);
        }
    }

//    private ExecutionReport cancelledExecutionReport(final Transaction transaction, final Account account,
//                                                     final String latestClientOrderId) {
//        final var executionReport = new ExecutionReport();
//        executionReport.set(new ExecType(ExecType.CANCELED));
//        executionReport.set(new OrdStatus(OrdStatus.CANCELED));
//        return getExecutionReport(transaction, account, latestClientOrderId, executionReport);
//    }
//
//    private ExecutionReport replacedExecutionReport(final Transaction transaction, final Account account,
//                                                    final String latestClientOrderId) {
//        final var executionReport = new ExecutionReport();
//        executionReport.set(new ExecType(ExecType.REPLACED));
//        executionReport.set(new OrdStatus(OrdStatus.REPLACED));
//        return getExecutionReport(transaction, account, latestClientOrderId, executionReport);
//    }
//
//    private static ExecutionReport getExecutionReport(final Transaction transaction, final Account account,
//                                                      final String latestClientOrderId, final ExecutionReport executionReport) {
//        executionReport.set(new OrderID(String.valueOf(transaction.getOrderId())));
//        executionReport.set(new ClOrdID(latestClientOrderId));
//        executionReport.set(account);
//        executionReport.set(new ExecID(EXEC_STRING + UUID.randomUUID()));
//        executionReport.set(new TransactTime(LocalDateTime.now(ZoneId.of(UTC_STRING))));
//        return executionReport;
//    }
}
