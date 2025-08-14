package com.washintontech.matchingEngine.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

public class NumberUtils {
    private static final long RANDOM_BASE = ThreadLocalRandom.current().nextLong(1_000_000L, Long.MAX_VALUE / 2);
    private static final AtomicLong counter = new AtomicLong(0);


    public static long midValue(final float price, final float price1, final int scale, final float multiple) {
        BigDecimal bd1 = BigDecimal.valueOf(price);
        BigDecimal bd2 = BigDecimal.valueOf(price1);
        BigDecimal multipleDecimal = BigDecimal.valueOf(multiple);
        final var floatValue = bd1.add(bd2)
                .divide(BigDecimal.valueOf(2), 10, RoundingMode.HALF_UP)
                .divide(multipleDecimal, 0, RoundingMode.HALF_UP).multiply(multipleDecimal)
                .setScale(scale, RoundingMode.HALF_UP).floatValue();
        return Long.parseLong(String.valueOf(floatValue)); // TODO: Check out of bound
    }

    public static long generateThreadLocalRandomLong() {
        return RANDOM_BASE + counter.getAndIncrement();
    }
}
