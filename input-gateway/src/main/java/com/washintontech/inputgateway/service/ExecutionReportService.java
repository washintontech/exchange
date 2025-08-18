package com.washintontech.inputgateway.service;

import org.springframework.stereotype.Service;
import quickfix.FieldNotFound;
import quickfix.field.ExecID;
import quickfix.field.ExecType;
import quickfix.field.OrdStatus;
import quickfix.field.OrderID;
import quickfix.field.OrigClOrdID;
import quickfix.field.TransactTime;
import quickfix.fix44.ExecutionReport;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import static com.washintontech.cache.model.OrderRecords.EXEC_STRING;
import static com.washintontech.cache.model.OrderRecords.UTC_STRING;

@Service
public class ExecutionReportService {

    public ExecutionReport executionReport(final NewOrderSingle clientOrder, final long orderId) throws FieldNotFound {
        final var report = new ExecutionReport();
        report.set(clientOrder.getClOrdID());
        report.set(clientOrder.getAccount());
        report.set(clientOrder.getSymbol());
        report.set(new OrderID(String.valueOf(orderId)));
        report.set(new ExecType(ExecType.NEW));
        report.set(new OrdStatus(OrdStatus.ACCEPTED_FOR_BIDDING));
        return executionReport(report);
    }

    public ExecutionReport executionReport(final OrderCancelRequest order, final long orderId) throws FieldNotFound {
        final var report = new ExecutionReport();
        report.set(order.getClOrdID());
        report.set(new OrderID(String.valueOf(orderId)));
        report.set(order.getAccount());
        report.set(new ExecType(ExecType.CANCELED));
        report.set(new OrdStatus(OrdStatus.ACCEPTED_FOR_BIDDING));
        return executionReport(report);
    }

    public ExecutionReport executionReport(final OrderCancelReplaceRequest order, final long orderId) throws FieldNotFound {
        final var report = new ExecutionReport();
        report.set(order.getClOrdID());
        report.set(new OrigClOrdID(order.getOrderID().getValue()));
        report.set(order.getAccount());
        report.set(new OrderID(String.valueOf(orderId)));
        report.set(new ExecType(ExecType.REPLACED));
        report.set(new OrdStatus(OrdStatus.ACCEPTED_FOR_BIDDING));
        return executionReport(report);
    }

    public ExecutionReport rejectedExecutionReport(final NewOrderSingle order) throws FieldNotFound {
        final var report = new ExecutionReport();
        report.set(order.getClOrdID());
        report.set(order.getAccount());
        report.set(order.getSymbol());
        return rejectedExecutionReport(report);
    }

    public ExecutionReport rejectedExecutionReport(final OrderCancelRequest order) throws FieldNotFound {
        final var report = new ExecutionReport();
        report.set(order.getClOrdID());
        report.set(order.getAccount());
        return rejectedExecutionReport(report);
    }

    public ExecutionReport rejectedExecutionReport(final OrderCancelReplaceRequest order) throws FieldNotFound {
        final var report = new ExecutionReport();
        report.set(order.getClOrdID());
        report.set(order.getAccount());
        return rejectedExecutionReport(report);
    }

    private ExecutionReport rejectedExecutionReport(final ExecutionReport report) {
        report.set(new ExecType(ExecType.REJECTED));
        report.set(new OrdStatus(OrdStatus.REJECTED));
        return executionReport(report);
    }

    private ExecutionReport executionReport(final ExecutionReport report) {
        report.set(new ExecID(EXEC_STRING + UUID.randomUUID()));
        report.set(new TransactTime(LocalDateTime.now(ZoneId.of(UTC_STRING))));
        return report;
    }
}
