//package com.washintontech.outputgateway.service;
//
//import com.washintontech.common.OrderType;
//import com.washintontech.common.Script;
//import com.washintontech.exchange.trade.ExchangeTradeRequest;
//import com.washintontech.exchange.trade.ExchangeTradeServiceGrpc;
//import com.washintontech.exchange.trade.ExecutionStatus;
//import com.washintontech.exchange.trade.OrderOutboundRequest;
//import com.washintontech.outputgateway.responseObserver.ExchangeTraderStreamObserverResponse;
//import io.grpc.ManagedChannel;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.springframework.stereotype.Service;
//
//@Service
//public class ExchangeTradeService {
//
//    private static final Logger log = LogManager.getLogger(ExchangeTradeService.class);
//    private final ExchangeTradeServiceGrpc.ExchangeTradeServiceStub exchangeTradeServiceStub;
//
//    public ExchangeTradeService(final ManagedChannel managedChannel) {
//        this.exchangeTradeServiceStub = ExchangeTradeServiceGrpc.newStub(managedChannel);
//    }
//
//    public void tradeResponse() {
//        final var exchangeTradeRequest = ExchangeTradeRequest.newBuilder()
//                .setBrokerId("brokerID")
//                .setOrderOutboundRequest(
//                        OrderOutboundRequest.newBuilder()
//                                .setOrderId("orderId")
//                                .setScript(Script.AMAZON)
//                                .setQuantity(10)
//                                .setExecutionStatus(ExecutionStatus.COMPLETED)
//                                .setOrderType(OrderType.NEW_MARKET)
//                                .setOrderExecutionPrice(100.05f)
//                                .build())
//                .build();
//
//        exchangeTradeServiceStub.trade(exchangeTradeRequest, new ExchangeTraderStreamObserverResponse());
//    }
//}
