package com.washintontech.common.quickfix;

import quickfix.field.OrdType;

public class FixUtils {

    public static long convertPriceDoubleToLong(final OrdType ordType, final double price) {
        if (ordType.getValue() == OrdType.MARKET) {
            return 0;
        }
        return (long) (price * 10000);
    }

    public static long convertQtyDoubleToLong(final double qty) {
        return (long) qty;
    }
}
