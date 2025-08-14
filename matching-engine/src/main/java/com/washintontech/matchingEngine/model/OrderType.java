package com.washintontech.matchingEngine.model;

public enum OrderType {
    NEW_MARKET,
    NEW_LIMIT,
    MODIFY, // Only Limit order can be modified.
    CANCEL

}
