//package com.washintontech.inputgateway.quickfix;
//
//import com.washintontech.common.component.InboundFixApplication;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import quickfix.Application;
//import quickfix.MessageCracker;
//
//@Component
//public class ExampleFixApp extends MessageCracker implements Application {
//
//
//    private static final Logger log = LoggerFactory.getLogger(InboundFixApplication.class);
//
//    private final OrderProcessingService orderService;
//    private final FixMessageValidator validator;
//    private final FixMetricsRecorder metrics;
//
//    @Autowired
//    public ExampleFixApp(OrderProcessingService orderService,
//                         FixMessageValidator validator,
//                         FixMetricsRecorder metrics) {
//        this.orderService = orderService;
//        this.validator = validator;
//        this.metrics = metrics;
//    }
//
//    // ----------- Application Interface Methods -----------
//    @Override
//    public void onCreate(SessionID sessionId) {
//        log.info("FIX Session created: {}", sessionId);
//    }
//
//    @Override
//    public void onLogon(SessionID sessionId) {
//        log.info("FIX Session logged on: {}", sessionId);
//        metrics.recordSessionEvent("logon");
//    }
//
//    @Override
//    public void onLogout(SessionID sessionId) {
//        log.info("FIX Session logged out: {}", sessionId);
//        metrics.recordSessionEvent("logout");
//    }
//
//    @Override
//    public void toAdmin(Message message, SessionID sessionId) {
//        try {
//            message.getHeader().setString(Username.FIELD, System.getenv("FIX_USERNAME"));
//            message.getHeader().setString(Password.FIELD, System.getenv("FIX_PASSWORD"));
//        } catch (FieldNotFound e) {
//            log.error("Error setting admin fields", e);
//        }
//    }
//
//    @Override
//    public void fromAdmin(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
//        validator.validateAdminMessage(message);
//    }
//
//    @Override
//    public void toApp(Message message, SessionID sessionId) throws DoNotSend {
//        validator.validateAppMessage(message);
//        metrics.recordOutboundMessage(message);
//    }
//
//    @Override
//    public void fromApp(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
//        try {
//            crack(message, sessionId);
//            metrics.recordInboundMessage(message);
//        } catch (Exception e) {
//            log.error("Error processing message", e);
//            metrics.recordMessageError();
//            throw e;
//        }
//    }
//
//    // ----------- Message Handlers -----------
//    @Handler
//    public void onMessage(NewOrderSingle order, SessionID sessionId) throws FieldNotFound {
//        ExecutionReport report = orderService.processOrder(order);
//        sendExecutionReport(report, sessionId);
//    }
//
//    @Handler
//    public void onMessage(OrderCancelRequest cancel, SessionID sessionId) throws FieldNotFound {
//        ExecutionReport report = orderService.processCancel(cancel);
//        sendExecutionReport(report, sessionId);
//    }
//
//    private void sendExecutionReport(ExecutionReport report, SessionID sessionId) {
//        try {
//            report.set(new TransactTime(new Date()));
//            Session.sendToTarget(report, sessionId);
//        } catch (SessionNotFound e) {
//            log.error("Failed to send execution report", e);
//        }
//    }
//
//
//}
