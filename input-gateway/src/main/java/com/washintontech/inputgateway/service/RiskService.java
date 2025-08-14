package com.washintontech.inputgateway.service;

import com.washintontech.matchingEngine.model.BrokerTradeRequest;
import org.springframework.stereotype.Service;
import quickfix.fix44.NewOrderSingle;

@Service
public class RiskService {

    public void riskAssessment(final BrokerTradeRequest req) {
    }

    public void riskAssessment(NewOrderSingle newOrderSingle) {


    }
}
