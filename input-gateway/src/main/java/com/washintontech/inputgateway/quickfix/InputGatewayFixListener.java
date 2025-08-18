package com.washintontech.inputgateway.quickfix;

import com.washintontech.cache.service.OrderRecordService;
import com.washintontech.common.quickfix.FixMessageListener;
import com.washintontech.inputgateway.service.ExecutionReportService;
import com.washintontech.inputgateway.service.InboundTraderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import quickfix.FieldNotFound;
import quickfix.SessionID;
import quickfix.fix44.ExecutionReport;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;

@Component
@RequiredArgsConstructor
public class InputGatewayFixListener implements FixMessageListener {

    private final InboundTraderService inboundTraderService;
    private final ExecutionReportService executionReportService;
    private final OrderRecordService orderRecordService;

    @Override
    public ExecutionReport onNewOrderSingle(final NewOrderSingle order, final SessionID sessionID) throws FieldNotFound {
        final var brokerId = sessionID.getTargetCompID();
        try {
            return inboundTraderService.processNewTradeRequest(order, Integer.parseInt(brokerId));
        } catch (Exception exception) {
            final var executionReport = executionReportService.rejectedExecutionReport(order);
            orderRecordService.addExecutionReport(executionReport, Integer.parseInt(brokerId));
            return executionReport;
        }
    }

    @Override
    public ExecutionReport onOrderCancelRequest(final OrderCancelRequest order, final SessionID sessionID) throws FieldNotFound {
        final var brokerId = sessionID.getTargetCompID();
        try {
            return inboundTraderService.processCancelTradeRequest(order, Integer.parseInt(brokerId));
        } catch (Exception exception) {
            final var executionReport = executionReportService.rejectedExecutionReport(order);
            orderRecordService.addExecutionReport(executionReport, Integer.parseInt(brokerId));
            return executionReport;
        }
    }

    @Override
    public ExecutionReport onOrderCancelReplaceRequest(final OrderCancelReplaceRequest order, final SessionID sessionID) throws FieldNotFound {
        final var brokerId = sessionID.getTargetCompID();
        try {
            return inboundTraderService.processOrderCancelReplaceRequest(order, Integer.parseInt(brokerId));
        } catch (Exception exception) {
            final var executionReport = executionReportService.rejectedExecutionReport(order);
            orderRecordService.addExecutionReport(executionReport, Integer.parseInt(brokerId));
            return executionReport;
        }
    }

}
