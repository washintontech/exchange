package com.washintontech.matchingEngine.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import quickfix.FieldNotFound;
import quickfix.field.OrderQty;
import quickfix.field.Price;
import quickfix.field.Side;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelReplaceRequest;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

@Getter
@ToString
public final class Order implements Poolable {
    private final int poolIndex;
    private int symbolId;
    private long orderId;
    private long price;
    private long quantity;
    private char side;
    private long sequenceNumber;
    private int brokerId;
    @Setter
    private Order previous;
    @Setter
    private Order next;
    @Setter
    private AtomicLong executedQuantity;

    private ThreadPoolExecutor threadPoolExecutor;

    public Order(final int poolIndex) {
        this.poolIndex = poolIndex;
    }

    public void enrichOrderWithNewOrderSingle(final NewOrderSingle requestOrder, final long orderId, final int brokerId,
                                              final int symbolId) throws FieldNotFound {
        this.symbolId = symbolId;
        this.brokerId = brokerId;
        this.orderId = orderId;
        this.price = FixUtils.convertPriceDoubleToLong(requestOrder.getDouble(Price.FIELD));
        this.quantity = FixUtils.convertPriceDoubleToLong(requestOrder.getDouble(OrderQty.FIELD));
        this.side = requestOrder.getChar(Side.FIELD);
    }

    public Order enrichWithOrderCancelReplaceOrder(final OrderCancelReplaceRequest changedOrderRequest, final long orderId) throws FieldNotFound {
        this.orderId = orderId;
        this.price = FixUtils.convertPriceDoubleToLong(changedOrderRequest.getDouble(Price.FIELD));
        this.quantity = FixUtils.convertPriceDoubleToLong(changedOrderRequest.getDouble(OrderQty.FIELD));
        return this;
    }

//    public void enrichOrder(final BrokerTradeRequest brokerTradeRequest, final long orderId) {
//        this.script = brokerTradeRequest.getScript();
//        this.brokerId = brokerTradeRequest.getBrokerId();
//        this.orderId = orderId;
//        this.price = brokerTradeRequest.getPrice();
//        this.quantity = brokerTradeRequest.getQuantity();
//        this.isBid = brokerTradeRequest.isBid();
//        //this.sequenceNumber
//    }

    public long pendingQuantities() {
        return quantity - executedQuantity.get();
    }

    public void reduceQuantity(final long remaining) {

    }

    public static long generateThreadLocalRandomLong() {
        return ThreadLocalRandom.current().nextLong();
    }

    public void updateExecutedQuantity(final long newQty) {
        executedQuantity.addAndGet(newQty);
    }


}
