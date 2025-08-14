//package com.washintontech.outputgateway.quickfix;
//
//import com.washintontech.common.chronicle.Script;
//import com.washintontech.common.chronicle.Transaction;
//import quickfix.Message;
//
//public class ExchangeTradeRequest extends Message {
//
//    private final long transactionId;
//    private final long transactionTime;
//    private final long orderId;
//    private final long transactionPrice;
//    private final Script script;
//    private final int brokerId;
//    private final boolean isBid;
//    private final int quantity;
//    private final ExecutionStatus executionStatus;
//
//    public ExchangeTradeRequest(final Transaction transaction) {
//        this.transactionId = transaction.getTransactionId();
//        this.transactionTime = transaction.getTransactionTime();
//        this.orderId = transaction.getOrderId();
//        this.transactionPrice = transaction.getTransactionPrice();
//        this.script = transaction.getSymbolId();
//        this.brokerId = transaction.getBrokerId();
//        this.isBid = transaction.isBid();
//        this.quantity = transaction.getQuantity();
//        this.executionStatus = ExecutionStatus.COMPLETED;
//    }
//}
