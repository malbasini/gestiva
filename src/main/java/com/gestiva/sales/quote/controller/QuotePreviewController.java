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

    @GetMapping("/{id}/preview")
    public String previewQuote(@PathVariable Long id, Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();
        QuotePdfView quote = quotePdfDataService.buildView(tenantId, id);

        model.addAttribute("quote", quote);
        model.addAttribute("previewMode", true);
        model.addAttribute("quoteId", id);
        model.addAttribute("tenantId", tenantId);

        return "quote/quote-preview";
    }
}