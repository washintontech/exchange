package com.washintontech.matchingEngine.util;

import java.util.UUID;

public class StringUtils {
    public static String generateRandomUUID() {
        return UUID.randomUUID().toString();
    }
}
