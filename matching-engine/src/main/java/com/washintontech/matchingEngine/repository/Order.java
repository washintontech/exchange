//package com.washintontech.app.repository;
//
//import com.washintontech.common.Script;
//import jakarta.validation.constraints.DecimalMin;
//import jakarta.validation.constraints.Min;
//import jakarta.validation.constraints.NotBlank;
//import jakarta.validation.constraints.NotNull;
//import lombok.Getter;
//
//import java.util.concurrent.atomic.AtomicInteger;
//
//@Getter
//public class Order {
//    @NotBlank
//    private final String orderId;
//    @Min(1)
//    private final int totalQuantity;
//    @DecimalMin(value = "0.05", inclusive = false)
//    private final float price;
//    @NotNull
//    private final Script script;
//    @Min(1)
//    private final long instantEpochNanoSec;
//    @NotNull
//    private AtomicInteger executedQuantity;
//
//    public Order(final String orderId, final int totalQuantity, final float price, final Script script,
//                 final long instantEpochNanoSec) {
//        this.orderId = orderId;
//        this.totalQuantity = totalQuantity;
//        this.price = price;
//        this.script = script;
//        this.instantEpochNanoSec = instantEpochNanoSec;
//    }
//
//    public void updateExecutedQuantity(int newQty) {
//        executedQuantity.addAndGet(newQty);
//    }
//
//    public int pendingQty() {
//        return totalQuantity - executedQuantity.get();
//    }
//}
