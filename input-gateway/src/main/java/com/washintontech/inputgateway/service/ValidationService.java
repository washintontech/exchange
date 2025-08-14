package com.washintontech.inputgateway.service;

import com.washintontech.cache.model.OrderRecord;
import com.washintontech.cache.service.OrderRecordService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import quickfix.FieldNotFound;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;

import java.math.BigDecimal;

@Service
public class ValidationService {
    private static final Logger log = LogManager.getLogger(InboundTraderService.class);

    private final OrderRecordService orderRecordService;

    public ValidationService(final OrderRecordService orderRecordService) {
        this.orderRecordService = orderRecordService;
    }

    public void validateRequest(NewOrderSingle newOrderSingle, final int brokerId) throws FieldNotFound {
        StringBuffer errorBuffer = new StringBuffer();
        int errorCount = 0;
        // TODO: Validation
        orderRecordService.updateOrderRecord(newOrderSingle, brokerId);
    }

    public void validateRequest(final OrderCancelRequest orderCancelRequest, final int brokerId) throws FieldNotFound {
        StringBuffer errorBuffer = new StringBuffer();
        int errorCount = 0;
        // TODO: Validation

        final var orderRecord = orderRecordService.tradeOrderRecord(Long.valueOf(orderCancelRequest.getOrderID().getValue()));
        final var userOrderRecord = orderRecordService.userOrderRecord(orderCancelRequest.getClOrdID().getValue());
        if (orderRecord != null || userOrderRecord.getOrderStatus() == OrderRecord.OrderStatus.CANCEL) {
            log.error("Executed order can't be cancelled, orderId: {},", orderRecord.getOrderId());
            throw new RuntimeException("Executed order can't be cancelled, orderId: " + orderRecord.getOrderId());
        }

        final var newOrderSingle = userOrderRecord.getNewOrderSingle();
        if (userOrderRecord.getBrokerId() != brokerId ||
                newOrderSingle.getSymbol() != orderCancelRequest.getSymbol() ||
                newOrderSingle.getSide() != orderCancelRequest.getSide() ||
                userOrderRecord.getAccount() != orderCancelRequest.getAccount()) {

            log.error("Order cancel request does not match the original order, brokerId: {}, clOrdID: {}, symbol: {}, side: {}",
                    brokerId, orderCancelRequest.getClOrdID().getValue(), orderCancelRequest.getSymbol(), orderCancelRequest.getSide());

            throw new RuntimeException("Order cancel request does not match the original order, brokerId: " + brokerId +
                    ", clOrdID: " + orderCancelRequest.getClOrdID().getValue() +
                    ", symbol: " + orderCancelRequest.getSymbol() +
                    ", side: " + orderCancelRequest.getSide());
        }
        orderRecordService.updateOrderRecord(orderCancelRequest);
    }

    public void validateRequest(final OrderCancelReplaceRequest order, final int brokerId) throws FieldNotFound {

        final var orderRecord = orderRecordService.tradeOrderRecord(Long.valueOf(order.getOrderID().getValue()));
        final var userOrderRecord = orderRecordService.userOrderRecord(order.getClOrdID().getValue());
        if (orderRecord != null || userOrderRecord.getOrderStatus() == OrderRecord.OrderStatus.CANCEL) {
            log.error("Executed order can't be cancelled, orderId: {},", orderRecord.getOrderId());
            throw new RuntimeException("Executed order can't be cancelled, orderId: " + orderRecord.getOrderId());
        }

        final var newOrderSingle = userOrderRecord.getNewOrderSingle();
        if (userOrderRecord.getBrokerId() != brokerId ||
                newOrderSingle.getSymbol() != order.getSymbol() ||
                newOrderSingle.getSide() != order.getSide() ||
                userOrderRecord.getAccount() != order.getAccount()) {

            log.error("Order cancel request does not match the original order, brokerId: {}, clOrdID: {}, symbol: {}, side: {}",
                    brokerId, order.getClOrdID().getValue(), order.getSymbol(), order.getSide());

            throw new RuntimeException("Order cancel request does not match the original order, brokerId: " + brokerId +
                    ", clOrdID: " + order.getClOrdID().getValue() +
                    ", symbol: " + order.getSymbol() +
                    ", side: " + order.getSide());
        }
        orderRecordService.updateOrderRecord(order);
    }

    // Only BUY and Sell
    private boolean checkInvalidSide(final char aChar) {
        return aChar != '1' && aChar != '2';
    }

    private boolean checkInvalidDouble(final double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().scale() > 4;
    }

    private boolean checkInvalidString(final String string) {
        return string.isBlank();
    }


}
