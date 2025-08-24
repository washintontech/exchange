package com.washintontech.common.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class SymbolDictionary {

    // TODO: Load as Static config at startup time
    private static final ConcurrentHashMap<String, Integer> symbolToIdMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Integer, String> idToSymbolMap = new ConcurrentHashMap<>();
    private static final AtomicInteger counter = new AtomicInteger(1);

    public static Integer symbolToId(String symbol) {
        return symbolToIdMap.computeIfAbsent(symbol, s -> {
            final var symbolId = counter.getAndIncrement();
            idToSymbolMap.put(symbolId, s);
            return symbolId;
        });
    }

    public static String idToSymbol(Integer id) {
        return idToSymbolMap.get(id);
    }

}
