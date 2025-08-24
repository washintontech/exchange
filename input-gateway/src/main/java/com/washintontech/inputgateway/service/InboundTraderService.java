package com.washintontech.inputgateway.service;

import com.washintontech.cache.service.OrderRecordService;
import com.washintontech.matchingEngine.component.OrderBookEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import quickfix.FieldNotFound;
import quickfix.fix44.ExecutionReport;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;

import static com.washintontech.matchingEngine.util.NumberUtils.generateThreadLocalRandomLong;

@Service
@Log4j2
@RequiredArgsConstructor
public class InboundTraderService {
    private final ValidationService validationService;
    private final ComplianceService complianceService;
    private final RiskService riskService;
    private final OrderBookEngine orderBookEngine;
    private final OrderRecordService orderRecordService;
    private final ExecutionReportService executionReportService;

    public ExecutionReport processNewTradeRequest(final NewOrderSingle clientOrder, final int brokerId)
            throws FieldNotFound {
        log.debug("ClientOrder received from brokerId: {}, {}", brokerId, clientOrder);
        final var orderId = generateThreadLocalRandomLong();

        validationService.validateRequest(clientOrder);
        complianceService.ensureCompliance(clientOrder);
        riskService.riskAssessment(clientOrder);

        orderRecordService.updateOrderRecord(clientOrder, brokerId, orderId);
        orderBookEngine.submitNewOrder(clientOrder, brokerId, orderId);
        final var executionReport = executionReportService.executionReport(clientOrder, orderId);
        orderRecordService.addExecutionReport(executionReport, brokerId);
        return executionReport;
    }

    public ExecutionReport processCancelTradeRequest(final OrderCancelRequest order, final int brokerId)
            throws FieldNotFound {
        log.debug("Cancel Order received from brokerId: {}, {}", brokerId, order);
        final var orderRecord = orderRecordService.userOrderRecord(order.getOrigClOrdID().getValue());

        validationService.validateRequest(order, brokerId, orderRecord.getLatestSingleOrderId());
        orderRecordService.updateOrderRecord(order);

        orderBookEngine.submitCancelOrder(order, orderRecord.getLatestSingleOrderId());
        final var executionReport = executionReportService.executionReport(order, orderRecord.getLatestSingleOrderId());
        orderRecordService.addExecutionReport(executionReport, brokerId);
        return executionReport;
    }


    public ExecutionReport processOrderCancelReplaceRequest(final OrderCancelReplaceRequest order,
                                                            final int brokerId) throws FieldNotFound {
        log.debug("OrderCancelReplace Order received from brokerId: {}, {}", brokerId, order);
        final var newOrderID = generateThreadLocalRandomLong();

        final var orderRecord = orderRecordService.userOrderRecord(order.getOrigClOrdID().getValue());
        validationService.validateRequest(order, brokerId, orderRecord);
        final var oldOrderId = orderRecord.getLatestSingleOrderId();
        orderRecordService.updateOrderRecord(order, newOrderID);

        orderBookEngine.submitOrderCancelReplaceOrder(order, oldOrderId, newOrderID);
        final var executionReport = executionReportService.executionReport(order, newOrderID);
        orderRecordService.addExecutionReport(executionReport, brokerId);
        return executionReport;
    }
}

