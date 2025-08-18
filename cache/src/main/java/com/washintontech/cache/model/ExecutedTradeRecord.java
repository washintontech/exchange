package com.washintontech.cache.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExecutedTradeRecord {
    @Getter
    private final long transactionId;
    private final long orderId;
    private final long price;
    private final long quantity;
    private final long executedAt;

}
