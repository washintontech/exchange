package com.washintontech.matchingEngine.component;

import com.washintontech.common.chronicle.Transaction;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TradeBook {
    private final int symbolId;
    private final Queue<Transaction> bidTransactions;
    private final Queue<Transaction> askTransactions;

    public TradeBook(final int symbolId) {
        this.symbolId = symbolId;
        this.bidTransactions = new ConcurrentLinkedQueue<>(); // TODO: Check for better DS
        this.askTransactions = new ConcurrentLinkedQueue<>();
    }

    public void process(final Transaction bidTransaction, final Transaction askTransaction) {
        bidTransactions.offer(bidTransaction);
        askTransactions.offer(askTransaction);
    }
}
