package com.gestiva.web.layout;

import com.gestiva.security.auth.AuthenticatedUser;
import com.gestiva.security.tenant.entity.Tenant;
import com.gestiva.security.tenant.repository.TenantRepository;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;


@ControllerAdvice
public class GlobalLayoutModelAdvice {

    private final TenantContext tenantContext;
    private final TenantRepository tenantRepository;

    public GlobalLayoutModelAdvice(TenantContext tenantContext,
                                   TenantRepository tenantRepository) {
        this.tenantContext = tenantContext;
        this.tenantRepository = tenantRepository;
    }

    @ModelAttribute("tenantSubscriptionActive")
    public boolean tenantSubscriptionActive() {
        Long tenantId = tenantContext.getCurrentTenantIdOrNull();
        if (tenantId == null) {
            return false;
        }

        return tenantRepository.findById(tenantId)
                .map(Tenant::isSubscriptionActive)
                .orElse(false);
    }

    @ModelAttribute("currentUser")
    public AuthenticatedUser currentUser() {
        return tenantContext.getCurrentUserOrNull();
    }

    @ModelAttribute("tenantSubscriptionStatus")
    public String tenantSubscriptionStatus() {
        Long tenantId = tenantContext.getCurrentTenantIdOrNull();
        if (tenantId == null) {
            return "PENDING";
        }

        return tenantRepository.findById(tenantId)
                .map(t -> t.getSubscriptionStatus() != null ? t.getSubscriptionStatus() : "PENDING")
                .orElse("PENDING");
    }

    @ModelAttribute("tenantSubscriptionPlan")
    public String tenantSubscriptionPlan() {
        Long tenantId = tenantContext.getCurrentTenantIdOrNull();
        if (tenantId == null) {
            return null;
        }

        return tenantRepository.findById(tenantId)
                .map(t -> t.getSubscriptionPlan())
                .orElse(null);
    }
}
