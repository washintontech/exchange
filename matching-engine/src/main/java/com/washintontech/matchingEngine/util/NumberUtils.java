package com.washintontech.matchingEngine.util;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

public class NumberUtils {
    private static final long RANDOM_BASE = ThreadLocalRandom.current().nextLong(1_000_000L, Long.MAX_VALUE / 2);
    private static final AtomicLong counter = new AtomicLong(0);

    public static long generateThreadLocalRandomLong() {
        return RANDOM_BASE + counter.getAndIncrement();
    }
}
