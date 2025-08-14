package com.washintontech.inputgateway.quickfix;

import com.washintontech.common.quickfix.FixMessageListener;
import com.washintontech.inputgateway.service.InboundTraderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import quickfix.FieldNotFound;
import quickfix.SessionID;
import quickfix.field.AvgPx;
import quickfix.field.CumQty;
import quickfix.field.ExecID;
import quickfix.field.ExecType;
import quickfix.field.LastPx;
import quickfix.field.LeavesQty;
import quickfix.field.OrdStatus;
import quickfix.field.OrderID;
import quickfix.field.Side;
import quickfix.field.Symbol;
import quickfix.fix44.ExecutionReport;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;

@Component
@RequiredArgsConstructor
public class InputGatewayFixListener implements FixMessageListener {

    private final InboundTraderService inboundTraderService;

    @Override
    public ExecutionReport onNewOrderSingle(final NewOrderSingle order, final SessionID sessionID) throws FieldNotFound {
        final var brokerId = sessionID.getTargetCompID();
        try {
            return inboundTraderService.processNewTradeRequest(order, Integer.parseInt(brokerId));
        } catch (Exception exception) {
            return executionReport(order);
        }
    }

    @Override
    public ExecutionReport onOrderCancelRequest(final OrderCancelRequest order, final SessionID sessionID) {
        final var brokerId = sessionID.getTargetCompID();
        try {
            return inboundTraderService.processCancelTradeRequest(order, Integer.parseInt(brokerId));
        } catch (Exception exception) {
            return executionReport(order);
        }
    }

    @Override
    public ExecutionReport onOrderCancelReplaceRequest(final OrderCancelReplaceRequest order, final SessionID sessionID) {
        final var brokerId = sessionID.getTargetCompID();
        try {
            return inboundTraderService.processOrderCancelReplaceRequest(order, Integer.parseInt(brokerId));
        } catch (Exception exception) {
            return executionReport(order);
        }
    }

    private ExecutionReport executionReport(final OrderCancelRequest order) {
        return null;
    }

    private ExecutionReport executionReport(final OrderCancelReplaceRequest order) {
        return null;
    }

    private ExecutionReport executionReport(final NewOrderSingle order) throws FieldNotFound {
        ExecutionReport report = new ExecutionReport(
                new OrderID("4379741157166390015"),
                new ExecID("EXEC123456"),
                new ExecType(ExecType.FILL),
                new OrdStatus(OrdStatus.FILLED),
                new Side(Side.BUY),
                new LeavesQty(0),
                new CumQty(order.getOrderQty().getValue()),
                new AvgPx(order.getPrice().getValue()));

        report.set(order.getClOrdID()); // ClOrdID from client
        report.set(new Symbol(order.getSymbol().getValue()));
        //report.set(new LastShares(order.getOrderQty().getValue()));
        report.set(new LastPx(order.getPrice().getValue()));

        return report;


//        final var executionReport = new ExecutionReport();
//        executionReport.set(order.getClOrdID());
//        executionReport.set(new OrdStatus(OrdStatus.REJECTED));
//        return executionReport;
    }
}
