//package com.washintontech.app.fromSources;
//
//public final class Order1 implements Poolable1 {
//    private long id;
//    private long price;
//    private long quantity;
//    private boolean isBid;
//    private Order1 next; // Linked list pointer
//
//    // Getters/setters
//    public Order1 getNext() {
//        return next;
//    }
//
//    public void setNext(Order1 next) {
//        this.next = next;
//    }
//
//    public long getQuantity() {
//        return quantity;
//    }
//
//    public void reduceQuantity(long delta) {
//        this.quantity -= delta;
//    }
//
//    // Reset for object pooling
//    @Override
//    public void reset() {
//        this.next = null;
//        this.id = -1;
//    }
//
//
//    // Packed with timestamp + sequence
//    long generateId() {
//        long timestamp = System.currentTimeMillis() << 20; // 44 bits
//        long sequence = orderCounter.getAndIncrement() & 0xFFFFF; // 20 bits
//        return timestamp | sequence;
//    }
//// Example: 0x18F30_2A3B4 = 2024-06-05 10:00:00 + order #176,564
//
//    // Convert string UUIDs to long on ingestion
//    long uuidToLong(String uuid) {
//        return UUID.fromString(uuid).getMostSignificantBits() & Long.MAX_VALUE;
//    }
//
//
//}
