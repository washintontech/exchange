package com.washintontech.matchingEngine.model;

import com.washintontech.common.quickfix.FixUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import quickfix.FieldNotFound;
import quickfix.field.OrderQty;
import quickfix.field.Price;
import quickfix.field.Side;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelReplaceRequest;

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
    //private long sequenceNumber;
    private int brokerId;
    @Setter
    private Order previous;
    @Setter
    private Order next;
    @Setter
    private AtomicLong executedQuantity;

    public Order(final int poolIndex) {
        this.poolIndex = poolIndex;
    }

    public void enrichOrderWithNewOrderSingle(final NewOrderSingle requestOrder, final long orderId, final int brokerId,
                                              final int symbolId) throws FieldNotFound {
        this.symbolId = symbolId;
        this.brokerId = brokerId;
        this.orderId = orderId;
        this.price = FixUtils.convertPriceDoubleToLong(requestOrder.getOrdType(), requestOrder.getDouble(Price.FIELD));
        this.quantity = FixUtils.convertQtyDoubleToLong(requestOrder.getDouble(OrderQty.FIELD));
        this.side = requestOrder.getChar(Side.FIELD);
        this.executedQuantity = new AtomicLong(0);
    }

    public Order enrichWithOrderCancelReplaceOrder(final OrderCancelReplaceRequest changedOrderRequest,
                                                   final long orderId) throws FieldNotFound {
        this.orderId = orderId;
        this.price = FixUtils.convertPriceDoubleToLong(changedOrderRequest.getOrdType(), changedOrderRequest.getDouble(Price.FIELD));
        this.quantity = FixUtils.convertQtyDoubleToLong(changedOrderRequest.getDouble(OrderQty.FIELD));
        return this;
    }

    public long pendingQuantities() {
        return quantity - executedQuantity.get();
    }

    public void reduceQuantity(final long remaining) {

    }

    public void updateExecutedQuantity(final long newQty) {
        executedQuantity.addAndGet(newQty);
    }


}
