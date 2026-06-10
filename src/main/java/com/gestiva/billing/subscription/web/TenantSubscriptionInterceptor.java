package com.gestiva.billing.subscription.web;

import com.gestiva.security.tenant.entity.Tenant;
import com.gestiva.security.tenant.repository.TenantRepository;
import com.gestiva.security.usercontext.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TenantSubscriptionInterceptor implements HandlerInterceptor {

    private final TenantContext tenantContext;
    private final TenantRepository tenantRepository;

    public TenantSubscriptionInterceptor(TenantContext tenantContext,
                                         TenantRepository tenantRepository) {
        this.tenantContext = tenantContext;
        this.tenantRepository = tenantRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String uri = request.getRequestURI();

        if (isPublicOrAlwaysAllowed(uri)) {
            return true;
        }
        Long tenantId = tenantContext.getCurrentTenantId();
        if (tenantId == null) {
            return true;
        }

        boolean active = tenantRepository.findById(tenantId)
                .map(t -> t.isSubscriptionActive())
                .orElse(false);

        if (!active) {
            response.sendRedirect("/pricing");
            return false;
        }
        Tenant t = tenantRepository.findById(tenantId).orElse(null);
        if ("STARTER".equalsIgnoreCase(t.getSubscriptionPlan())) {
            if (uri.startsWith("/inventory")
                    || uri.startsWith("/inventory-adjustments")
                    || uri.startsWith("/inventory-valuations")
                    || uri.startsWith("/tenant-settings/inventory-valuation")
                    || uri.startsWith("/payments")
                    || uri.startsWith("/payment-dues")
                    || uri.startsWith("/accounting")
                    || uri.startsWith("/accounting-entries")
                    || uri.startsWith("/accounting-dashboard")
                    || uri.startsWith("/vat-registers")
                    || uri.startsWith("/v2/accounts")
                    || uri.startsWith("/v2/journal-entries")) {
                response.sendRedirect("/pricing");
            }
            return false;
        }
        return true;
    }
    private boolean isPublicOrAlwaysAllowed(String uri) {
        return uri.equals("/login")
                || uri.equals("/logout")
                || uri.equals("/register")
                || uri.equals("/pricing")
                || uri.startsWith("/paypal")
                || uri.startsWith("/billing")
                || uri.startsWith("/403")
                || uri.startsWith("/css/")
                || uri.startsWith("/js/")
                || uri.startsWith("/images/")
                || uri.startsWith("/webjars/")
                || uri.equals("/error");
    }
}