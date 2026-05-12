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
    public String list(@RequestParam(name = "page", defaultValue = "0") int page,
                       @RequestParam(name = "size", defaultValue = "10") int size,
                       @RequestParam(name = "q", required = false) String q,
                       @RequestParam(name = "status", required = false) String status,
                       @RequestParam(name = "dateFrom", required = false)
                       @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
                       java.time.LocalDate dateFrom,
                       @RequestParam(name = "dateTo", required = false)
                       @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
                       java.time.LocalDate dateTo,
                       Model model) {

        Long tenantId = tenantContext.getCurrentTenantId();

        var resultPage = quoteWebService.findPage(tenantId, page, size, q, status, dateFrom, dateTo);
        model.addAttribute("quotesPage", resultPage);
        model.addAttribute("page", resultPage);
        model.addAttribute("q", q);
        model.addAttribute("status", status);
        model.addAttribute("dateFrom", dateFrom);
        model.addAttribute("dateTo", dateTo);
        model.addAttribute("size", size);
        model.addAttribute("activeMenu", "quotes");

        return "quote/quote-list";
    }











}