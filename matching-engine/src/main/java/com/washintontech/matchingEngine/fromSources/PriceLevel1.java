//package com.washintontech.app.fromSources;
//
//public final class PriceLevel1 {
//    // Head of the linked list (most recent order)
//    private volatile Order1 head;
//
//    // Tail of the linked list (oldest order)
//    private volatile Order1 tail;
//
//    // Total quantity at this price level (atomic for lock-free reads)
//    private final AtomicLong totalQuantity = new AtomicLong();
//
//    // Price represented as a long (e.g., 100.50 -> 10050)
//    private final long price;
//
//    public PriceLevel1(long price) {
//        this.price = price;
//    }
//
//    // ===== Core Operations =====
//
//    /**
//     * Adds an order to the price level (O(1))
//     */
//    public void add(Order1 order) {
//        order.setNext(null); // Ensure clean state
//
//        if (tail == null) {
//            // First order in empty level
//            head = tail = order;
//        } else {
//            // Append to tail
//            tail.setNext(order);
//            tail = order;
//        }
//
//        totalQuantity.addAndGet(order.getQuantity());
//    }
//
//    /**
//     * Removes an order (O(1) with order reference)
//     */
//    public boolean remove(Order1 order) {
//        if (head == null) return false;
//
//        // Special case: removing head
//        if (head == order) {
//            head = order.getNext();
//            if (head == null) {
//                tail = null; // Level is now empty
//            }
//            totalQuantity.addAndGet(-order.getQuantity());
//            return true;
//        }
//
//        // Find order in the linked list
//        Order1 current = head;
//        while (current != null && current.getNext() != order) {
//            current = current.getNext();
//        }
//
//        if (current != null) {
//            current.setNext(order.getNext());
//            if (order == tail) {
//                tail = current; // Update tail if removing last order
//            }
//            totalQuantity.addAndGet(-order.getQuantity());
//            return true;
//        }
//
//        return false; // Order not found
//    }
//
//    /**
//     * Removes the oldest order (FIFO, O(1))
//     */
//    public Order1 removeOldest() {
//        if (head == null) return null;
//
//        Order1 removed = head;
//        head = removed.getNext();
//        if (head == null) {
//            tail = null; // Level is now empty
//        }
//
//        totalQuantity.addAndGet(-removed.getQuantity());
//        return removed;
//    }
//
//    // ===== Atomic Operations =====
//
//    /**
//     * Executes a trade by reducing quantity at this price level.
//     *
//     * @return Remaining quantity after execution
//     */
//    public long executeQuantity(long quantity) {
//        long remaining = quantity;
//
//        while (remaining > 0 && head != null) {
//            Order1 order = head;
//            long orderQty = order.getQuantity();
//
//            if (orderQty > remaining) {
//                // Partial fill
//                order.reduceQuantity(remaining);
//                totalQuantity.addAndGet(-remaining);
//                remaining = 0;
//            } else {
//                // Full fill
//                removeOldest();
//                remaining -= orderQty;
//            }
//        }
//
//        return remaining;
//    }
//
//    // ===== Getters =====
//    public long getPrice() {
//        return price;
//    }
//
//    public long getTotalQuantity() {
//        return totalQuantity.get();
//    }
//
//    public boolean isEmpty() {
//        return head == null;
//    }
//
//    // ===== Iteration Support =====
//    public Order1 getHead() {
//        return head;
//    }
//}
