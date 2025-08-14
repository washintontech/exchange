package com.washintontech.outputgateway.component;

import com.washintontech.cache.service.OrderRecordService;
import com.washintontech.common.chronicle.Transaction;
import com.washintontech.common.component.FixApplication;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.wire.DocumentContext;
import quickfix.FieldNotFound;
import quickfix.field.Account;
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

import static com.washintontech.cache.model.OrderRecord.EXEC_STRING;
import static com.washintontech.cache.model.OrderRecord.UTC_STRING;

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
                processTransaction(transaction);
            }
        }
    }

    private void processTransaction(final Transaction transaction) {
        final var orderRecord = orderRecordService.tradeOrderRecord(transaction.getOrderId());
        final var executionReport = executionReport(transaction, orderRecord.getAccount());
        try {
            orderRecordService.addExecutionReport(executionReport);
        } catch (FieldNotFound e) {
            throw new RuntimeException(e); // TODO
        }
        fixApplication.sendToBroker(transaction.getBrokerId(), executionReport);
    }

    private ExecutionReport executionReport(final Transaction transaction, final Account account) {
        final var executionReport = new ExecutionReport();
        executionReport.set(new OrderID(String.valueOf(transaction.getOrderId())));
        executionReport.set(new ExecID(EXEC_STRING + UUID.randomUUID()));
        executionReport.set(new ExecType(ExecType.FILL));
        executionReport.set(new OrdStatus(OrdStatus.FILLED));
        executionReport.set(new TransactTime(LocalDateTime.now(ZoneId.of(UTC_STRING))));
        executionReport.set(account);
        return executionReport;
    }
}
