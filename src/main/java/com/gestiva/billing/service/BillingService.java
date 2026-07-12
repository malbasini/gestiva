package com.gestiva.billing.service;

import com.gestiva.billing.entity.BillingOrder;
import com.gestiva.billing.model.BillingOrderStatus;
import com.gestiva.billing.model.SubscriptionPlan;
import com.gestiva.billing.paypal.PaypalClient;
import com.gestiva.billing.paypal.PaypalCreateOrderResult;
import com.gestiva.billing.repository.BillingOrderRepository;
import com.gestiva.billing.web.CreatePaypalOrderResponse;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.tenant.entity.Tenant;
import com.gestiva.security.tenant.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Service
@Transactional
public class BillingService {

    private final BillingOrderRepository billingOrderRepository;
    private final BillingPlanService billingPlanService;
    private final PaypalClient paypalClient;
    private final TenantRepository tenantRepository;

    public BillingService(BillingOrderRepository billingOrderRepository,
                          BillingPlanService billingPlanService,
                          PaypalClient paypalClient,
                          TenantRepository tenantRepository) {

        this.billingOrderRepository = billingOrderRepository;
        this.billingPlanService = billingPlanService;
        this.paypalClient = paypalClient;
        this.tenantRepository = tenantRepository;
    }

    public void captureAndActivate(String providerOrderId, String rawPayload) {
        BillingOrder order = billingOrderRepository.findByProviderOrderId(providerOrderId)
                .orElseThrow(() -> new BusinessException("Ordine PayPal non trovato."));

        String captureStatus = paypalClient.captureOrder(providerOrderId);

        if (!"COMPLETED".equalsIgnoreCase(captureStatus)) {
            throw new BusinessException("Pagamento PayPal non completato.");
        }

        order.setStatus(BillingOrderStatus.COMPLETED.name());
        order.setCompletedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setRawPayload(rawPayload);
        billingOrderRepository.save(order);

        Tenant tenant = tenantRepository.findById(order.getTenantId())
                .orElseThrow(() -> new BusinessException("Tenant non trovato."));

        tenant.setSubscriptionActive(true);
        tenant.setSubscriptionStatus("ACTIVE");
        tenant.setSubscriptionPlan(order.getPlanCode());
        tenantRepository.save(tenant);
    }
    public CreatePaypalOrderResponse createPaypalOrder(Long tenantId, String planCode) {
        SubscriptionPlan plan = billingPlanService.parsePlan(planCode);
        BigDecimal amount = billingPlanService.resolvePrice(plan);
        String currency = billingPlanService.resolveCurrencyCode();
        //CALCOLO L'IVA
        java.math.BigDecimal taxAmount = amount
                .multiply(BigDecimal.valueOf(22.0))
                .divide(new java.math.BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
        amount = amount.add(taxAmount);
        BillingOrder order = new BillingOrder();
        order.setTenantId(tenantId);
        order.setPlanCode(plan.name());
        order.setProvider("PAYPAL");
        order.setStatus(BillingOrderStatus.PENDING.name());
        order.setAmount(amount);
        order.setCurrencyCode(currency);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        billingOrderRepository.save(order);

        PaypalCreateOrderResult paypalOrder = paypalClient.createOrder(
                tenantId,
                plan.name(),
                amount,
                currency,
                "https://gestiva.org/billing/paypal/success",
                "https://gestiva.org/billing/paypal/cancel"
        );

        order.setProviderOrderId(paypalOrder.getOrderId());
        order.setApprovalUrl(paypalOrder.getApprovalUrl());
        order.setRawPayload(paypalOrder.getRawPayload());
        order.setUpdatedAt(LocalDateTime.now());
        billingOrderRepository.save(order);

        CreatePaypalOrderResponse response = new CreatePaypalOrderResponse();
        response.setBillingOrderId(order.getId());
        response.setProviderOrderId(order.getProviderOrderId());
        response.setApprovalUrl(order.getApprovalUrl());
        return response;
    }

    public void markOrderCompleted(String providerOrderId, String rawPayload) {
        BillingOrder order = billingOrderRepository.findByProviderOrderId(providerOrderId)
                .orElseThrow(() -> new BusinessException("Ordine PayPal non trovato."));

        order.setStatus(BillingOrderStatus.COMPLETED.name());
        order.setCompletedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setRawPayload(rawPayload);
        billingOrderRepository.save(order);

        Tenant tenant = tenantRepository.findById(order.getTenantId())
                .orElseThrow(() -> new BusinessException("Tenant non trovato."));

        tenant.setSubscriptionActive(true);
        tenant.setSubscriptionStatus("ACTIVE");
        tenant.setSubscriptionPlan(order.getPlanCode());
        tenantRepository.save(tenant);
    }

    public void markOrderCancelled(String providerOrderId, String rawPayload) {
        billingOrderRepository.findByProviderOrderId(providerOrderId).ifPresent(order -> {
            order.setStatus(BillingOrderStatus.CANCELLED.name());
            order.setUpdatedAt(LocalDateTime.now());
            order.setRawPayload(rawPayload);
            billingOrderRepository.save(order);
        });
    }
}