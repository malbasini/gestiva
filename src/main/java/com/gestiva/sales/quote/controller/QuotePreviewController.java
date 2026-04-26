package com.gestiva.sales.quote.controller;

import com.gestiva.sales.quote.pdf.QuotePdfDataService;
import com.gestiva.sales.quote.pdf.QuotePdfView;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/quotes")
public class QuotePreviewController {

    private final QuotePdfDataService quotePdfDataService;
    private final TenantContext tenantContext;

    public QuotePreviewController(QuotePdfDataService quotePdfDataService,
                                  TenantContext tenantContext) {
        this.quotePdfDataService = quotePdfDataService;
        this.tenantContext = tenantContext;
    }

    public Long getTenantId() {
        return tenantContext.getCurrentTenantId();
    }

    @GetMapping("/{id}/preview")
    public String previewQuote(@PathVariable Long id, Model model) {
        QuotePdfView quote = quotePdfDataService.buildView(getTenantId(), id);
        model.addAttribute("quote", quote);
        model.addAttribute("previewMode", true);
        return "quote/quote-preview";
    }
}