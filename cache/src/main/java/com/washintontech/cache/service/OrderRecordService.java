package com.washintontech.cache.service;

import com.washintontech.cache.model.OrderRecords;
import org.springframework.stereotype.Service;
import quickfix.FieldNotFound;
import quickfix.fix44.ExecutionReport;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class OrderRecordService {

    // TODO: Clear all the map post processing to avoid memory leak.
    private ConcurrentHashMap<String, OrderRecords> userOrderIdRecords = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Long, OrderRecords> tradeOrderIdRecords = new ConcurrentHashMap<>();

    public OrderRecords userOrderRecord(final String userOrderId) {
        return userOrderIdRecords.get(userOrderId);
    }

    public OrderRecords tradeOrderRecord(final Long orderId) {
        return tradeOrderIdRecords.get(orderId);
    }

    public void updateOrderRecord(NewOrderSingle newOrderSingle, final int brokerId, final long orderId) throws FieldNotFound {
        final var orderRecord = new OrderRecords(newOrderSingle, brokerId, orderId);
        userOrderIdRecords.put(newOrderSingle.getClOrdID().getValue(), orderRecord);
        tradeOrderIdRecords.put(orderId, orderRecord);
    }

    public void updateOrderRecord(final OrderCancelRequest orderCancelRequest) throws FieldNotFound {
        final var record = userOrderIdRecords.get(orderCancelRequest.getOrigClOrdID().getValue());
        record.updateOrderRecord(orderCancelRequest);
    }

    public void updateOrderRecord(final OrderCancelReplaceRequest orderCancelReplaceRequest, final long newOrderID) throws FieldNotFound {
        final var record = userOrderIdRecords.get(orderCancelReplaceRequest.getOrigClOrdID().getValue());
        record.updateOrderRecord(orderCancelReplaceRequest, newOrderID);
    }

    public void addExecutionReport(final ExecutionReport executionReport, final int brokerId) throws FieldNotFound {
        var record = userOrderIdRecords.get(executionReport.getClOrdID().getValue());
        if (record == null) { // Case: Failure at Data validation.
            record = new OrderRecords(executionReport, brokerId);
        }
        record.updateOrderRecord(executionReport);
    }
}
