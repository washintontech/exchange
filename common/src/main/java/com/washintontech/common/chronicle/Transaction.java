package com.washintontech.common.chronicle;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.openhft.chronicle.wire.SelfDescribingMarshallable;


@EqualsAndHashCode(callSuper = true)
@Data
public class Transaction extends SelfDescribingMarshallable {

    private final long transactionId;
    private final long transactionTime;
    private final long orderId;
    private final long transactionPrice;
    private final int symbolId;
    private final int brokerId;
    private final char side;
    private final char executionType;
    //private final int version = 1;

    private long quantity;

    public Transaction(long transactionId, long transactionTime, long orderId, long transactionPrice, int symbolId,
                       int brokerId, char side, final char executionType) {
        this.transactionId = transactionId;
        this.transactionTime = transactionTime;
        this.orderId = orderId;
        this.transactionPrice = transactionPrice;
        this.brokerId = brokerId;
        this.symbolId = symbolId;
        this.side = side;
        this.executionType = executionType;
    }

}
