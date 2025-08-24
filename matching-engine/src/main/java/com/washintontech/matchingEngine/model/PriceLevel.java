package com.washintontech.matchingEngine.model;

import lombok.Getter;

import java.util.concurrent.atomic.AtomicLong;

@Getter
public final class PriceLevel {
    private final long price;
    private final AtomicLong totalQuantity;
    private volatile Order head;
    private volatile Order tail;

    public PriceLevel(long price) {
        this.price = price;
        this.totalQuantity = new AtomicLong();
    }

    // TODO: Check for concurrency
    public void add(Order order) {
        order.setNext(null);
        order.setPrevious(null);

        if (tail == null) {
            head = tail = order;
        } else {
            tail.setNext(order);
            order.setPrevious(tail);
            tail = order;
        }

        totalQuantity.addAndGet(order.getQuantity());
    }

    public boolean remove(Order order) {
        if (head == null) return false;

        if (head == order) {
            head = order.getNext();
            if (head == null) {
                tail = null;
            }
            totalQuantity.addAndGet(-order.getQuantity());
            return true;
        }

        Order current = head;
        while (current != null && current.getNext() != order) {
            current = current.getNext();
        }

        if (current != null) {
            final var nextOrder = order.getNext();
            current.setNext(nextOrder);
            nextOrder.setPrevious(current);
            if (order == tail) {
                tail = current;
            }
            totalQuantity.addAndGet(-order.getQuantity());
            return true;
        }

        return false;
    }

    public Order removeOldest() {
        if (head == null) return null;

        Order removed = head;
        head = removed.getNext();
        head.setPrevious(null);
        if (head == null) {
            tail = null;
        }

        totalQuantity.addAndGet(-removed.getQuantity());
        return removed;
    }

    public long executeQuantity(long quantity) {
        long remaining = quantity;

        while (remaining > 0 && head != null) {
            Order order = head;
            long orderQty = order.getQuantity();

            if (orderQty > remaining) {
                order.reduceQuantity(remaining);
                totalQuantity.addAndGet(-remaining);
                remaining = 0;
            } else {
                removeOldest();
                remaining -= orderQty;
            }
        }

        return remaining;
    }
}
