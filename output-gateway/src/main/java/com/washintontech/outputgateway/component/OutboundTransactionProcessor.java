package com.washintontech.outputgateway.component;

import com.washintontech.cache.service.OrderRecordService;
import com.washintontech.common.chronicle.Transaction;
import com.washintontech.common.component.FixApplication;
import com.washintontech.common.quickfix.FixUtils;
import com.washintontech.common.utils.SymbolDictionary;
import com.washintontech.common.utils.TimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.wire.DocumentContext;
import quickfix.FieldNotFound;
import quickfix.field.Account;
import quickfix.field.AvgPx;
import quickfix.field.ClOrdID;
import quickfix.field.CumQty;
import quickfix.field.ExecID;
import quickfix.field.ExecType;
import quickfix.field.LeavesQty;
import quickfix.field.OrdStatus;
import quickfix.field.OrderID;
import quickfix.field.OrderQty;
import quickfix.field.Price;
import quickfix.field.Side;
import quickfix.field.Symbol;
import quickfix.field.TransactTime;
import quickfix.fix44.ExecutionReport;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.LockSupport;

import static com.washintontech.cache.model.OrderRecords.EXEC_STRING;

@Log4j2
@RequiredArgsConstructor
public class OutboundTransactionProcessor implements Runnable {

    private final ExcerptTailer chronicleTailer;
    private final FixApplication fixApplication;
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
            log.error("Error in OutboundTransactionProcessor: ", e);
        }
    }

    private void processTransaction(final Transaction transaction) {
        log.debug("Processing transaction: {}", transaction);
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
        executionReport.set(new Symbol(SymbolDictionary.idToSymbol(transaction.getSymbolId())));
        executionReport.set(new OrderQty(FixUtils.convertQtyLongToDouble(transaction.getQuantity())));
        executionReport.set(new Price(FixUtils.convertPriceLongToDouble(transaction.getTransactionPrice())));
        executionReport.set(new Side(transaction.getSide()));
        executionReport.set(account);
        executionReport.set(new ExecID(EXEC_STRING + UUID.randomUUID()));
        executionReport.set(new TransactTime(
                TimeUtils.convertEpochNanoSecToLocalDateTime(transaction.getTransactionTime())));

// TODO: Check need for these fields
        executionReport.set(new LeavesQty(00.0));   // Tag 151
        executionReport.set(new CumQty(00.0));            // Tag 14
        executionReport.set(new AvgPx(00.0));              // Tag 6
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
}
