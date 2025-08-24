package com.washintontech.matchingEngine.util;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

public class NumberUtils {
    private static final long RANDOM_BASE = ThreadLocalRandom.current().nextLong(1_000_000L, Long.MAX_VALUE / 2);
    private static final AtomicLong counter = new AtomicLong(0);


    public static long midValue(final long bidPrice, final long askPrice) {
        return (bidPrice + askPrice + 1) / 2; // "+1" to bias toward rounding up when odd
    }

    public static long generateThreadLocalRandomLong() {
        return RANDOM_BASE + counter.getAndIncrement();
    }
}
