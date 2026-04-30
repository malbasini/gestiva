package com.gestiva.sales.quote.controller;

import com.gestiva.sales.quote.web.QuoteDetailWebService;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/quotes")
public class QuoteDetailPageController {

    private final QuoteDetailWebService quoteDetailWebService;
    private final TenantContext tenantContext;

    public QuoteDetailPageController(QuoteDetailWebService quoteDetailWebService,
                                     TenantContext tenantContext) {
        this.quoteDetailWebService = quoteDetailWebService;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         @RequestParam(required = false) Long tenantId,
                         Model model) {

        Long resolvedTenantId = tenantId != null ? tenantId : tenantContext.getCurrentTenantId();

        var quote = quoteDetailWebService.getDetail(resolvedTenantId, id);

        model.addAttribute("quote", quote);
        model.addAttribute("tenantId", resolvedTenantId);
        model.addAttribute("activeMenu", "quotes");
        return "quote/quote-detail";
    }
}