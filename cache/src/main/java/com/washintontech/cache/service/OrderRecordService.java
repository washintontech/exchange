package com.washintontech.cache.service;

import com.washintontech.cache.model.OrderRecord;
import org.springframework.stereotype.Service;
import quickfix.FieldNotFound;
import quickfix.fix44.ExecutionReport;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class OrderRecordService {

    // TODO: Clear all the map post processing.
    private ConcurrentHashMap<String, OrderRecord> userOrderIdRecords = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Long, OrderRecord> tradeOrderIdRecords = new ConcurrentHashMap<>();

    public void updateOrderRecord(NewOrderSingle newOrderSingle, final int brokerId) throws FieldNotFound {
        final var orderRecord = new OrderRecord(newOrderSingle, brokerId);
        userOrderIdRecords.put(newOrderSingle.getClOrdID().getValue(), orderRecord);
    }


    public void updateOrderRecord(final OrderCancelRequest orderCancelRequest) throws FieldNotFound {
        final var record = userOrderIdRecords.get(orderCancelRequest.getOrigClOrdID().getValue());
        record.updateOrderRecord(orderCancelRequest);
    }

    public void updateOrderRecord(final OrderCancelReplaceRequest orderCancelReplaceRequest) throws FieldNotFound {
        final var record = userOrderIdRecords.get(orderCancelReplaceRequest.getOrigClOrdID().getValue());
        record.updateOrderRecord(orderCancelReplaceRequest);
    }

    public OrderRecord userOrderRecord(final String userOrderId) {
        return userOrderIdRecords.get(userOrderId);
    }

    public OrderRecord tradeOrderRecord(final Long orderId) {
        return tradeOrderIdRecords.get(orderId);
    }

    public void addExecutionReport(final ExecutionReport executionReport) throws FieldNotFound {
        final var record = userOrderIdRecords.get(executionReport.getClOrdID().getValue());
        record.updateOrderRecord(executionReport);
    }
}
