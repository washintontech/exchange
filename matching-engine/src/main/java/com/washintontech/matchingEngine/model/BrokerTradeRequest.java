package com.washintontech.matchingEngine.model;

import com.washintontech.common.chronicle.Script;
import lombok.Data;
import lombok.EqualsAndHashCode;
import quickfix.Message;

@EqualsAndHashCode(callSuper = true)
@Data
public class BrokerTradeRequest extends Message {
    private long requestId;
    private int brokerId;
    private Script script;
    private int quantity;
    private long price;
    private boolean isBid;
    private OrderType orderType;

    // Set for response
    private long orderId;
    private OrderStatus orderStatus;
}
