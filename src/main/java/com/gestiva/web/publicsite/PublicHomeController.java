package com.gestiva.web.publicsite;

import com.gestiva.security.usercontext.TenantContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PublicHomeController {

    private final TenantContext tenantContext;

    public PublicHomeController(TenantContext tenantContext) {
        this.tenantContext = tenantContext;
    }

    @GetMapping("/")
    public String home() {
        if (tenantContext.getCurrentTenantIdOrNull() != null) {
            return "public/home";
        }
        return "redirect:/login";
    }
}