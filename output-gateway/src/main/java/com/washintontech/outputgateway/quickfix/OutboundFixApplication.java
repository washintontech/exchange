//package com.washintontech.outputgateway.quickfix;
//
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.springframework.stereotype.Component;
//import quickfix.Application;
//import quickfix.DoNotSend;
//import quickfix.FieldNotFound;
//import quickfix.IncorrectDataFormat;
//import quickfix.IncorrectTagValue;
//import quickfix.Message;
//import quickfix.MessageCracker;
//import quickfix.RejectLogon;
//import quickfix.Session;
//import quickfix.SessionID;
//import quickfix.SessionNotFound;
//import quickfix.UnsupportedMessageType;
//
//@Component
//public class OutboundFixApplication extends MessageCracker implements Application {
//
//    private static final Logger log = LogManager.getLogger(OutboundFixApplication.class);
//
//    private SessionID sessionId;
//
//    public void submitOrder(final ExchangeTradeRequest exchangeTradeRequest) throws SessionNotFound {
//
//        //final var orderRes = new OrderRes("", "", "", 0, 0.0f, TradeDirection.BUY, Status.UNKNOWN_STATUS);
//        Session.sendToTarget(new Employee(), sessionId);
//
////        OrderState state = new OrderState(order);
////        orderStates.put(order.getClOrdID().getValue(), state);
////
////        return order.getClOrdID().getValue();
//    }
//
//    @Override
//    public void fromApp(final Message message, final SessionID sessionId) throws FieldNotFound, IncorrectDataFormat,
//            IncorrectTagValue, UnsupportedMessageType {
//        crack(message, sessionId);
//    }
//
//    @Handler
//    public void onMessage(Employee order, SessionID sessionID) throws FieldNotFound {
//
//    }
//
//    @Override
//    public void onCreate(final SessionID sessionId) {
//        this.sessionId = sessionId;
//        log.info("Session created: {}", sessionId);
//    }
//
//    @Override
//    public void onLogout(final SessionID sessionId) {
//        this.sessionId = null;
//        log.info("Session terminated: {}", sessionId);
//    }
//
//    @Override
//    public void onLogon(final SessionID sessionId) {
//    }
//
//    @Override
//    public void toAdmin(final Message message, final SessionID sessionId) {
//    }
//
//    @Override
//    public void fromAdmin(final Message message, final SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
//    }
//
//    @Override
//    public void toApp(final Message message, final SessionID sessionId) throws DoNotSend {
//    }
//
//    private static class Employee extends Message {
//    }
//}
