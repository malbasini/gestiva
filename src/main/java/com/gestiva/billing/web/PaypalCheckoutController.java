package com.gestiva.billing.web;

import com.gestiva.billing.service.BillingService;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/billing/paypal")
public class PaypalCheckoutController {

    private final BillingService billingService;
    private final TenantContext tenantContext;

    public PaypalCheckoutController(BillingService billingService,
                                    TenantContext tenantContext) {
        this.billingService = billingService;
        this.tenantContext = tenantContext;
    }

    @PostMapping("/create-order")
    public ResponseEntity<CreatePaypalOrderResponse> createOrder(@RequestParam("plan") String plan) {
        Long tenantId = tenantContext.getCurrentTenantId();
        return ResponseEntity.ok(billingService.createPaypalOrder(tenantId, plan));
    }
}