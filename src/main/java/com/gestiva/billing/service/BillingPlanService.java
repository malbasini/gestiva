package com.gestiva.billing.service;

import com.gestiva.billing.model.SubscriptionPlan;
import com.gestiva.common.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class BillingPlanService {

    public BigDecimal resolvePrice(SubscriptionPlan plan) {
        return switch (plan) {
            case STARTER -> new BigDecimal("19.00");
            case PROFESSIONAL -> new BigDecimal("39.00");
        };
    }

    public String resolveCurrencyCode() {
        return "EUR";
    }

    public SubscriptionPlan parsePlan(String value) {
        try {
            return SubscriptionPlan.valueOf(value.trim().toUpperCase());
        } catch (Exception ex) {
            throw new BusinessException("Piano non valido: " + value);
        }
    }
}
