package com.washintontech.common.component;

import com.washintontech.common.quickfix.FixMessageListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import quickfix.Application;
import quickfix.DoNotSend;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.Message;
import quickfix.MessageCracker;
import quickfix.RejectLogon;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.UnsupportedMessageType;
import quickfix.fix44.ExecutionReport;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@RequiredArgsConstructor
public class FixApplication extends MessageCracker implements Application {

    private final FixMessageListener fixMessageListener;

    private final Map<SessionID, Session> activeSessions = new ConcurrentHashMap<>();
    private final Map<Integer, SessionID> brokerSessions = new ConcurrentHashMap<>();

    @Override
    public void onLogon(final SessionID sessionId) {
        String brokerId = sessionId.getTargetCompID();
        brokerSessions.put(Integer.parseInt(brokerId), sessionId);
        log.debug("Broker [{}] logged in with session {}", brokerId, sessionId);
    }

    public void sendToBroker(int brokerId, Message message) {
        log.debug("Sending message to broker: {}, message: {}", brokerId, message);
        SessionID sessionId = brokerSessions.get(brokerId);
        if (sessionId != null && Session.doesSessionExist(sessionId)) {
            try {
                Session.sendToTarget(message, sessionId);
            } catch (SessionNotFound e) {
                throw new RuntimeException(e);
            }
        } else {
            log.warn("No active session for broker: {}", brokerId);
        }
    }

    @Override
    public void onCreate(final SessionID sessionId) {
        activeSessions.put(sessionId, Session.lookupSession(sessionId));
        log.debug("Session created: {}", sessionId);
    }

    @Override
    public void onLogout(final SessionID sessionId) {
        activeSessions.remove(sessionId);
        log.debug("Session terminated: {}", sessionId);
    }

    @Override
    public void fromApp(final Message message, final SessionID sessionId) throws FieldNotFound,
            IncorrectTagValue, UnsupportedMessageType {
        log.debug("Received message from app: {}", message);
        crack(message, sessionId);
    }

    @Handler
    public void onMessage(NewOrderSingle order, SessionID sessionID) throws FieldNotFound {
        log.debug("Received NewOrderSingle from app: {}", order);
        ExecutionReport executionReport = fixMessageListener.onNewOrderSingle(order, sessionID);
        log.debug("Response ExecutionReport : {}", executionReport);
        sendExecutionReport(executionReport, sessionID);
    }

    @Handler
    public void onMessage(OrderCancelRequest order, SessionID sessionID) throws FieldNotFound {
        log.debug("Received OrderCancelRequest from app: {}", order);
        ExecutionReport executionReport = fixMessageListener.onOrderCancelRequest(order, sessionID);
        log.debug("Response ExecutionReport : {}", executionReport);
        sendExecutionReport(executionReport, sessionID);
    }

    @Handler
    public void onMessage(OrderCancelReplaceRequest order, SessionID sessionID) throws FieldNotFound {
        log.debug("Received OrderCancelReplaceRequest from app: {}", order);
        ExecutionReport executionReport = fixMessageListener.onOrderCancelReplaceRequest(order, sessionID);
        log.debug("Response ExecutionReport : {}", executionReport);
        sendExecutionReport(executionReport, sessionID);
    }


    private void sendExecutionReport(final ExecutionReport executionReport, final SessionID sessionID) {
        try {
            Session.sendToTarget(executionReport, sessionID);
        } catch (SessionNotFound e) {
            throw new RuntimeException(e); // TODO
        }
    }

    @Override
    public void toAdmin(final Message message, final SessionID sessionId) {
    }

    @Override
    public void fromAdmin(final Message message, final SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
    }

    @Override
    public void toApp(final Message message, final SessionID sessionId) throws DoNotSend {
    }
}
