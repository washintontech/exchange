//package com.washintontech.app.model;
//
//import java.util.concurrent.ConcurrentMap;
//
//public class Partition implements Runnable {
//    private final ConcurrentMap<String, OrderBook> orderBooks;
//    public Partition(final ConcurrentMap<String, OrderBook> orderBooks) {
//        this.orderBooks = orderBooks;
//    }
//
//    @Override
//    public void run() {
//        OrderBook book = orderBooks.computeIfAbsent(script, s -> new OrderBook());
//        book.process(order);
//    }
//}
