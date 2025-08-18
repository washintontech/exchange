package com.washintontech.cache.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Data
@AllArgsConstructor
public class SingleOrderRecord {
    private final long orderId;
    private final long quantity;
    private final quickfix.field.OrdType OrdType;
    private final long price;
    private final long origOrderId;
    private AtomicLong remainingQty;
    private final List<ExecutedTradeRecord> trades;
    private char execType;
}
