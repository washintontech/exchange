package com.washintontech.matchingEngine.util;

import java.time.Instant;

public class TimeUtils {
    public static long generateInstantEpochNanoSec() {
        var now = Instant.now();
        return now.getEpochSecond() * 1_000_000_000L + now.getNano();
    }
}
