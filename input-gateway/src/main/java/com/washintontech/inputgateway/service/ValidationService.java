package com.washintontech.inputgateway.service;

import com.washintontech.cache.model.OrderRecords;
import com.washintontech.cache.service.OrderRecordService;
import com.washintontech.inputgateway.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import quickfix.FieldNotFound;
import quickfix.field.Account;
import quickfix.field.ClOrdID;
import quickfix.field.OrdType;
import quickfix.field.OrderQty;
import quickfix.field.Price;
import quickfix.field.Side;
import quickfix.field.Symbol;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;

import java.math.BigDecimal;

@Service
@Log4j2
@RequiredArgsConstructor
public class ValidationService {

    private final OrderRecordService orderRecordService;

    public void validateRequest(NewOrderSingle newOrderSingle) throws FieldNotFound {
        validateOrder(newOrderSingle.getPrice(), newOrderSingle.getOrdType(), newOrderSingle.getOrderQty());
    }

    public void validateRequest(final OrderCancelRequest orderCancelRequest, final int brokerId, final long orderId)
            throws FieldNotFound {
        validateExistingOrder(orderCancelRequest.getClOrdID(), orderCancelRequest.getSymbol(),
                orderCancelRequest.getSide(), orderCancelRequest.getAccount(), brokerId, orderId);

    }

    public void validateRequest(final OrderCancelReplaceRequest order, final int brokerId,
                                final OrderRecords orderRecords) throws FieldNotFound {
        validateOrder(order.getPrice(), order.getOrdType(), order.getOrderQty());
        validateExistingOrder(order.getClOrdID(), order.getSymbol(),
                order.getSide(), order.getAccount(), brokerId, orderRecords.getLatestSingleOrderId());

        if (orderRecords.getSingleOrderRecords().peek().getRemainingQty().get() !=
                (long) order.getOrderQty().getValue()) {
            throw new BadRequestException("Partial modification of quantity is not supported. " +
                    "Please cancel the order and place a new one.");
        }
    }

    private void validateOrder(final Price price, final OrdType ordType, final OrderQty orderQty) {
        int errorCount = 0;
        StringBuilder errorBuffer = new StringBuilder();
        if (!validatePrice(ordType, price)) {
            errorCount++;
            errorBuffer.append("Invalid price: ").append(price.getValue()).append("\n");
        }

        if (!validateQty(orderQty)) {
            errorCount++;
            errorBuffer.append("Invalid Order Qty: ").append(orderQty.getValue()).append("\n");
        }

        if (errorCount > 0) {
            throw new BadRequestException(errorBuffer.toString());
        }
    }

    private boolean validateQty(final OrderQty orderQty) {
        BigDecimal bd = BigDecimal.valueOf(orderQty.getValue()).stripTrailingZeros();
        return bd.scale() <= 0;
    }

    private boolean validatePrice(final OrdType ordType, final Price price) {
        if (ordType.getValue() == OrdType.MARKET) {
            return true;
        }
        BigDecimal bd = BigDecimal.valueOf(price.getValue()).stripTrailingZeros();
        return bd.scale() <= 4;
    }

    private void validateExistingOrder(final ClOrdID clOrderId, final Symbol symbol, final Side side,
                                       final Account account,
                                       final int brokerId, final long orderId) throws FieldNotFound {

        final var userOrderRecord = orderRecordService.userOrderRecord(clOrderId.getValue());
        if (userOrderRecord.getOrderStatus() == OrderRecords.OrderStatus.CANCEL) {
            log.error("Executed order can't be cancelled, orderId: {},", orderId);
            throw new RuntimeException("Executed order can't be cancelled, orderId: " + orderId);
        }

        final var newOrderSingle = userOrderRecord.getNewOrderSingle();
        if (userOrderRecord.getBrokerId() != brokerId ||
                newOrderSingle.getSymbol() != symbol ||
                newOrderSingle.getSide() != side ||
                userOrderRecord.getAccount() != account) {

            log.error("Order cancel request does not match the original order, brokerId: {}, clOrdID: {}, " +
                            "symbol: {}, side: {}",
                    brokerId, clOrderId.getValue(), symbol, side);

            throw new BadRequestException(
                    "Order cancel request does not match the original order, brokerId: " + brokerId +
                            ", clOrdID: " + clOrderId.getValue() +
                            ", symbol: " + symbol +
                            ", side: " + side);
        }
    }
}
