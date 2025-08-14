//package com.washintontech.app.fromSources;
//
//import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
//
//public class PriceLevelThreadSafe1 {
//    private static final AtomicReferenceFieldUpdater<PriceLevel1, Order1> HEAD_UPDATER =
//            AtomicReferenceFieldUpdater.newUpdater(PriceLevel1.class, Order1.class, "head");
//
//    private volatile Order1 head;
//    private volatile Order1 tail;
//
//    public void add(Order1 order) {
//        order.setNext(null);
//        Order1 currentTail;
//        do {
//            currentTail = tail;
//            if (currentTail == null) {
//                if (HEAD_UPDATER.compareAndSet(this, null, order)) {
//                    tail = order;
//                    return;
//                }
//            } else {
//                order.setNext(currentTail.getNext());
//                if (compareAndSetTail(currentTail, order)) {
//                    currentTail.setNext(order);
//                    return;
//                }
//            }
//        } while (true);
//    }
//}
