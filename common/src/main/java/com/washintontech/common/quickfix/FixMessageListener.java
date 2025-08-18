package com.washintontech.common.quickfix;


import quickfix.FieldNotFound;
import quickfix.SessionID;
import quickfix.fix44.ExecutionReport;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;

public interface FixMessageListener {

    ExecutionReport onNewOrderSingle(NewOrderSingle order, SessionID sessionID) throws FieldNotFound;

    ExecutionReport onOrderCancelRequest(OrderCancelRequest order, SessionID sessionID) throws FieldNotFound;

    ExecutionReport onOrderCancelReplaceRequest(OrderCancelReplaceRequest order, SessionID sessionID) throws FieldNotFound;

}
