package com.washintontech.inputgateway.service;

import com.washintontech.cache.service.OrderRecordService;
import com.washintontech.matchingEngine.component.OrderBookEngine;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

import static com.washintontech.cache.model.OrderRecord.EXEC_STRING;
import static com.washintontech.cache.model.OrderRecord.UTC_STRING;

@Service
@RequiredArgsConstructor
public class InboundTraderService {
    private static final Logger log = LogManager.getLogger(InboundTraderService.class);
    private final ValidationService validationService;
    private final ComplianceService complianceService;
    private final RiskService riskService;
    private final OrderBookEngine orderBookEngine;
    private final OrderRecordService orderRecordService;

    public ExecutionReport processNewTradeRequest(final NewOrderSingle clientOrder, final int brokerId) throws FieldNotFound {
        log.debug("ClientOrder received from brokerId: {}, {}", brokerId, clientOrder);
        validationService.validateRequest(clientOrder, brokerId);
        complianceService.ensureCompliance(clientOrder);
        riskService.riskAssessment(clientOrder);
        final long orderId = orderBookEngine.submitNewOrder(clientOrder, brokerId);
        final var executionReport = executionReportNewOrderSingle(clientOrder, orderId);
        orderRecordService.addExecutionReport(executionReport);
        return executionReport;
    }

    public ExecutionReport processCancelTradeRequest(final OrderCancelRequest order, final int brokerId) throws FieldNotFound {
        log.debug("Cancel Order received from brokerId: {}, {}", brokerId, order);
        validationService.validateRequest(order, brokerId);
        final long orderId = orderBookEngine.submitCancelOrder(order, brokerId);
        final var executionReport = executionReportCancelTradeRequest(order, orderId);
        orderRecordService.addExecutionReport(executionReport);
        return executionReport;
    }


    public ExecutionReport processOrderCancelReplaceRequest(final OrderCancelReplaceRequest order, final int brokerId) throws FieldNotFound {
        validationService.validateRequest(order, brokerId);
        log.debug("OrderCancelReplace Order received from brokerId: {}, {}", brokerId, order);
        final long orderId = orderBookEngine.submitOrderCancelReplaceOrder(order, brokerId);
        final var executionReport = executionReportOrderCancelReplaceRequest(order, orderId);
        orderRecordService.addExecutionReport(executionReport);
        return executionReport;
    }

    private ExecutionReport executionReportOrderCancelReplaceRequest(final OrderCancelReplaceRequest order, final long orderId) throws FieldNotFound {
        final var report = new ExecutionReport();
        report.set(order.getClOrdID());
        report.set(new OrigClOrdID(order.getOrderID().getValue()));

        report.set(new OrderID(String.valueOf(orderId)));
        report.set(new ExecID(EXEC_STRING + UUID.randomUUID()));
        report.set(new ExecType(ExecType.REPLACED));
        report.set(new OrdStatus(OrdStatus.ACCEPTED_FOR_BIDDING));
        report.set(new TransactTime(LocalDateTime.now(ZoneId.of(UTC_STRING))));
        report.set(order.getAccount());
        return report;
    }

    private ExecutionReport executionReportCancelTradeRequest(final OrderCancelRequest order, final long orderId) throws FieldNotFound {
        final var report = new ExecutionReport();
        report.set(order.getClOrdID());
        report.set(new OrderID(String.valueOf(orderId)));
        report.set(new ExecID(EXEC_STRING + UUID.randomUUID()));
        report.set(new ExecType(ExecType.CANCELED));
        report.set(new OrdStatus(OrdStatus.ACCEPTED_FOR_BIDDING));
        report.set(new TransactTime(LocalDateTime.now(ZoneId.of(UTC_STRING))));
        report.set(order.getAccount());
        return report;
    }

    private ExecutionReport executionReportNewOrderSingle(final NewOrderSingle clientOrder, final long orderId) throws FieldNotFound {
        final var report = new ExecutionReport();
        report.set(clientOrder.getClOrdID());
        report.set(new OrderID(String.valueOf(orderId)));
        report.set(new ExecID(EXEC_STRING + UUID.randomUUID()));
        report.set(new ExecType(ExecType.NEW));
        report.set(new OrdStatus(OrdStatus.ACCEPTED_FOR_BIDDING));
        report.set(new TransactTime(LocalDateTime.now(ZoneId.of(UTC_STRING))));
        report.set(clientOrder.getAccount());
        return report;
    }
}

