package com.gestiva.sales.quote.controller;

import com.gestiva.sales.quote.dto.QuoteSearchRequest;
import com.gestiva.sales.quote.web.QuoteWebService;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/quotes")
public class QuotePageController {

    private final QuoteWebService quoteWebService;
    private final TenantContext tenantContext;

    public QuotePageController(QuoteWebService quoteWebService,
                               TenantContext tenantContext) {
        this.quoteWebService = quoteWebService;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    public String listQuotes(@RequestParam(required = false) String status,
                             @RequestParam(required = false) Long customerId,
                             @RequestParam(required = false) String search,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size,
                             @RequestParam(defaultValue = "quoteDate") String sortBy,
                             @RequestParam(defaultValue = "desc") String sortDir,
                             @RequestParam(required = false) Long tenantId,
                             Model model) {

        Long resolvedTenantId = tenantId != null ? tenantId : tenantContext.getCurrentTenantId();

        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        var pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        QuoteSearchRequest request = new QuoteSearchRequest();
        request.setStatus(status);
        request.setCustomerId(customerId);
        request.setSearch(search);

        var result = quoteWebService.search(resolvedTenantId, request, pageable);

        model.addAttribute("quotesPage", result);
        model.addAttribute("status", status);
        model.addAttribute("customerId", customerId);
        model.addAttribute("search", search);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("tenantId", resolvedTenantId);

        return "quote/quote-list";
    }
}