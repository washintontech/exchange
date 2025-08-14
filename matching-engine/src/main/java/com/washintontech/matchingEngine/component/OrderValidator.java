//package com.washintontech.app.model;
//
//import com.lmax.disruptor.EventHandler;
//
//public class OrderValidator implements EventHandler<OrderEvent> {
//
//    @Override
//    public void onEvent(final OrderEvent event, final long sequence, final boolean endOfBatch) throws Exception {
//        if (!validate(event.getOrder())) {
//            throw new IllegalOrderException(...);
//        }
//    }
//}
