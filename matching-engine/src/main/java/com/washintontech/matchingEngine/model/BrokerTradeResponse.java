package com.washintontech.matchingEngine.model;

import com.washintontech.common.chronicle.Script;
import com.washintontech.matchingEngine.util.TimeUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import quickfix.Message;

@EqualsAndHashCode(callSuper = true)
@Data
public class BrokerTradeResponse extends Message {
    private long requestId;
    private long brokerId;
    private long orderId;
    private Script script;
    private int quantity;
    private OrderStatus orderStatus;
    private long orderPlacedPrice;
    private boolean isBid;
    private long epochNanoSec;

//    public BrokerTradeResponse(final BrokerTradeRequest tradeRequest) {
//        this.requestId = tradeRequest.getRequestId();
//        this.brokerId = tradeRequest.getBrokerId();
//        this.orderId = tradeRequest.getOrderId();
//        this.script = tradeRequest.getScript();
//        this.quantity = tradeRequest.getQuantity();
//        this.orderStatus = tradeRequest.getOrderStatus();
//        this.orderPlacedPrice = tradeRequest.getPrice(); // TODO: for market order
//        this.isBid = tradeRequest.isBid();
//        this.epochNanoSec = TimeUtils.generateInstantEpochNanoSec();
//    }

    public BrokerTradeResponse(final BrokerTradeRequest tradeRequest, final long orderId, final OrderStatus orderStatus) {
        this.requestId = tradeRequest.getRequestId();
        this.brokerId = tradeRequest.getBrokerId();
        this.orderId = orderId;
        this.script = tradeRequest.getScript();
        this.quantity = tradeRequest.getQuantity();
        this.orderStatus = orderStatus;
        this.orderPlacedPrice = tradeRequest.getPrice(); // TODO: for market order
        this.isBid = tradeRequest.isBid();
        this.epochNanoSec = TimeUtils.generateInstantEpochNanoSec();
    }
}
