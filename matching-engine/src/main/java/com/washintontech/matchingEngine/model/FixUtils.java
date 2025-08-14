package com.washintontech.matchingEngine.model;

public class FixUtils {

//    public static long getPriceLong(final NewOrderSingle newOrderSingle) throws FieldNotFound {
//        return convertPriceDoubleToLong(newOrderSingle.getDouble(Price.FIELD));
//    }

    public static long convertPriceDoubleToLong(final double price) {
        return (long) (price * 10000);
    }

    public static double convertPriceLongToDouble(final long price) {
        return price / 10000.0;
    }

//    public static long getQuantityLong(final NewOrderSingle newOrderSingle) throws FieldNotFound {
//        return convertPriceDoubleToLong(newOrderSingle.getDouble(OrderQty.FIELD));
//    }
}
