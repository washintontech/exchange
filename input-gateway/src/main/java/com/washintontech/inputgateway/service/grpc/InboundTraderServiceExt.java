//package com.washintontech.inputgateway.service.grpc;
//
//import com.washintontech.broker.trade.BrokerTradeRequest;
//import com.washintontech.broker.trade.BrokerTradeResponse;
//import com.washintontech.broker.trade.BrokerTradeServiceGrpc;
//import com.washintontech.cache.annotation.ServerService;
//import com.washintontech.inputgateway.service.InboundTraderService;
//import io.grpc.stub.StreamObserver;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.springframework.stereotype.Service;
//
//@ServerService
//@Service
//public class InboundTraderServiceExt extends BrokerTradeServiceGrpc.BrokerTradeServiceImplBase {
//
//    private static final Logger log = LogManager.getLogger(InboundTraderServiceExt.class);
//
//    private final InboundTraderService brokerTraderService;
//
//    public InboundTraderServiceExt(final InboundTraderService inboundTraderService) {
//        this.brokerTraderService = inboundTraderService;
//    }
//
//    @Override
//    public void trade(BrokerTradeRequest request, StreamObserver<BrokerTradeResponse> responseObserver) {
////        Mono.just(request)
////                .doOnEach(logOnNext(req -> log.debug("Request received: {}", req)))
////                .flatMap(brokerTraderService::processTradeRequest)
////                .doOnNext(responseObserver::onNext)
////                .subscribe();
//
////        var tradeResponse = clientTraderService.processTradeRequest(request);
////        responseObserver.onNext(tradeResponse);
//    }
//}
//
//
////rpc trade(ClientTradeRequest) returns (TradeResponse);
