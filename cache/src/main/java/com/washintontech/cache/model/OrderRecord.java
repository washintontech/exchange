package com.washintontech.cache.model;

import lombok.Data;
import quickfix.FieldNotFound;
import quickfix.field.Account;
import quickfix.fix44.ExecutionReport;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;

import java.util.Stack;

@Data
public class OrderRecord {

    public static final String UTC_STRING = "UTC";
    public static final String EXEC_STRING = "EXEC";

    private long brokerId;
    private Account account;
    private String latestClientOrderId;
    private String latestExecutionReportId;
    private long orderId;
    private OrderStatus orderStatus;
    private NewOrderSingle newOrderSingle;
    private OrderCancelRequest orderCancelRequest;
    private Stack<OrderCancelReplaceRequest> orderCancelReplaceRequests;
    private Stack<ExecutionReport> executionReports;

    public OrderRecord(NewOrderSingle newOrderSingle, final int brokerId) throws FieldNotFound {
        this.brokerId = brokerId;
        this.latestClientOrderId = newOrderSingle.getClOrdID().getValue();
        this.orderStatus = OrderStatus.NEW;
        this.newOrderSingle = newOrderSingle;
        this.account = newOrderSingle.getAccount();
    }

    public void updateOrderRecord(final OrderCancelRequest orderCancelRequest) throws FieldNotFound {
        this.latestClientOrderId = orderCancelRequest.getClOrdID().getValue();
        this.orderStatus = OrderStatus.CANCEL;
        this.orderCancelRequest = orderCancelRequest;
    }

    public void updateOrderRecord(final OrderCancelReplaceRequest orderCancelReplaceRequest) throws FieldNotFound {
        this.latestClientOrderId = orderCancelReplaceRequest.getClOrdID().getValue();
        this.orderStatus = OrderStatus.MODIFY;
        this.orderCancelReplaceRequests.push(orderCancelReplaceRequest);
    }

    public void updateOrderRecord(final ExecutionReport executionReport) {
        this.executionReports.push(executionReport);
    }

    public enum OrderStatus {
        NEW,  // NewOrderSingle
        MODIFY, // OrderCancelReplaceRequest
        CANCEL // OrderCancelRequest
    }

}
