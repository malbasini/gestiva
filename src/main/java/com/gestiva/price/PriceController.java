package com.gestiva.price;

import com.gestiva.security.usercontext.TenantContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping()
public class PriceController {

    private final TenantContext tenantContext;

    public PriceController(TenantContext tenantContext) {
        this.tenantContext = tenantContext;
    }

    @GetMapping(value = "/pricing")
    public String page(Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();
        model.addAttribute("activeMenu", "price");
        model.addAttribute("tenantId", tenantId);
        return "public/pricing";
    }
}
