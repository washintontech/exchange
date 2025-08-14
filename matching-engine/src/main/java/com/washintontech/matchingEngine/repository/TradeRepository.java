package com.washintontech.matchingEngine.repository;

import com.washintontech.common.chronicle.Transaction;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class TradeRepository {
    private final List<Transaction> transactions;
    private final Map<String, List<Transaction>> orderIdTransactions;

    public TradeRepository() {
        this.transactions = new ArrayList<>();
        this.orderIdTransactions = new HashMap<>();
    }
}
