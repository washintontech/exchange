package com.washintontech.cache.model;

import com.washintontech.common.quickfix.FixUtils;
import lombok.Data;
import quickfix.FieldNotFound;
import quickfix.field.Account;
import quickfix.field.ExecType;
import quickfix.field.OrderQty;
import quickfix.field.Price;
import quickfix.fix44.ExecutionReport;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;

import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicLong;

@Data
public class OrderRecords {

    public static final String UTC_STRING = "UTC";
    public static final String EXEC_STRING = "EXEC";

    private int brokerId;
    private Account account;
    private String latestClientOrderId;
    private long latestSingleOrderId;
    private OrderStatus orderStatus;
    private Stack<SingleOrderRecord> singleOrderRecords = new Stack<>(); // Trade Orders
    private NewOrderSingle newOrderSingle;
    private OrderCancelRequest orderCancelRequest;
    private Stack<OrderCancelReplaceRequest> orderCancelReplaceRequests = new Stack<>();
    private Stack<ExecutionReport> executionReports = new Stack<>();

    public OrderRecords(final NewOrderSingle newOrderSingle, final int brokerId, final long orderId) throws FieldNotFound {
        this.brokerId = brokerId;
        this.latestClientOrderId = newOrderSingle.getClOrdID().getValue();
        this.orderStatus = OrderStatus.NEW;
        this.newOrderSingle = newOrderSingle;
        this.account = newOrderSingle.getAccount();
        this.latestSingleOrderId = orderId;

        final var qty = FixUtils.convertQtyDoubleToLong(newOrderSingle.getDouble(OrderQty.FIELD));
        final var price = FixUtils.convertPriceDoubleToLong(newOrderSingle.getOrdType(), newOrderSingle.getDouble(Price.FIELD));
        singleOrderRecords.push(
                new SingleOrderRecord(orderId, qty, newOrderSingle.getOrdType(), price, 0,
                        new AtomicLong(qty), List.of(), ExecType.NEW));
    }

    public OrderRecords(final ExecutionReport executionReport, final int brokerId) throws FieldNotFound {
        this.brokerId = brokerId;
        this.latestClientOrderId = executionReport.getClOrdID().getValue();
        this.orderStatus = OrderStatus.REJECTED;
        this.account = executionReport.getAccount();
    }

    public void updateOrderRecord(final OrderCancelRequest orderCancelRequest) throws FieldNotFound {
        this.latestClientOrderId = orderCancelRequest.getClOrdID().getValue();
        this.orderStatus = OrderStatus.CANCEL;
        this.orderCancelRequest = orderCancelRequest;
        this.singleOrderRecords.peek()
                .setExecType(ExecType.CANCELED);
    }

    public void updateOrderRecord(final OrderCancelReplaceRequest orderCancelReplaceRequest, final long newOrderID) throws FieldNotFound {
        this.latestClientOrderId = orderCancelReplaceRequest.getClOrdID().getValue();
        this.orderStatus = OrderStatus.REPLACED;
        this.orderCancelReplaceRequests.push(orderCancelReplaceRequest);

        final long originalOrderId = this.latestSingleOrderId;
        this.latestSingleOrderId = newOrderID;
        this.singleOrderRecords.peek()
                .setExecType(ExecType.REPLACED);

        final var qty = FixUtils.convertQtyDoubleToLong(orderCancelReplaceRequest.getDouble(OrderQty.FIELD));
        final var price = FixUtils.convertPriceDoubleToLong(orderCancelReplaceRequest.getOrdType(),
                orderCancelReplaceRequest.getDouble(Price.FIELD));
        singleOrderRecords.push(
                new SingleOrderRecord(newOrderID, qty, orderCancelReplaceRequest.getOrdType(), price, originalOrderId,
                        new AtomicLong(qty), List.of(), ExecType.NEW));
    }

    public void updateOrderRecord(final ExecutionReport executionReport) {
        this.executionReports.push(executionReport);
    }

    public enum OrderStatus {
        NEW,  // NewOrderSingle
        REPLACED, // OrderCancelReplaceRequest
        CANCEL, // OrderCancelRequest
        REJECTED, // Failed at Validation Stage
        EXECUTED // FILL order
    }

}
