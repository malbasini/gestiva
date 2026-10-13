package com.gestiva.web.layout;

import com.gestiva.security.auth.AuthenticatedUser;
import com.gestiva.security.tenant.entity.Tenant;
import com.gestiva.security.tenant.repository.TenantRepository;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import com.gestiva.platform.company.entity.Company;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.security.company.service.CompanyAccessService;

@ControllerAdvice
public class GlobalLayoutModelAdvice {

    private final TenantContext tenantContext;
    private final TenantRepository tenantRepository;
    private final CompanyContext companyContext;
    private final CompanyAccessService companyAccessService;

    public GlobalLayoutModelAdvice(TenantContext tenantContext,
                                   TenantRepository tenantRepository,
                                   CompanyContext companyContext,
                                   CompanyAccessService companyAccessService) {

        this.tenantContext = tenantContext;
        this.tenantRepository = tenantRepository;
        this.companyContext=companyContext;
        this.companyAccessService=companyAccessService;

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
    @ModelAttribute
    public void addActiveCompanyAttributes(org.springframework.ui.Model model) {
        if (tenantContext.getCurrentUserOrNull() == null) {
            model.addAttribute("activeCompanyId", null);
            model.addAttribute("activeCompanyName", null);
            return;
        }
        Long companyId = companyContext.resolveCurrentCompanyId();
        if (companyId == null) {
            model.addAttribute("activeCompanyId", null);
            model.addAttribute("activeCompanyName", null);
            return;
        }
        Company company = companyAccessService.requireAccess(companyId);
        model.addAttribute("activeCompanyId", company.getId());
        model.addAttribute("activeCompanyName", company.getLegalName());
    }

}
