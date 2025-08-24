package com.washintontech.common.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class TimeUtils {

    public static final String UTC_STRING = "UTC";

    public static long generateInstantEpochNanoSec() {
        var now = Instant.now();
        return now.getEpochSecond() * 1_000_000_000L + now.getNano();
    }

    public static LocalDateTime convertEpochNanoSecToLocalDateTime(long epochNanoSec) {
        Instant instant = Instant.ofEpochSecond(
                epochNanoSec / 1_000_000_000L,
                epochNanoSec % 1_000_000_000L
        );
        return LocalDateTime.ofInstant(instant, ZoneId.of(UTC_STRING));
    }
}
