package com.gestiva.accounting.v2.report.controller;

import com.gestiva.accounting.v2.report.web.FinancialPositionDetailWebService;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/accounting")
public class FinancialPositionDetailPageController {

    private final TenantContext tenantContext;
    private final FinancialPositionDetailWebService financialPositionDetailWebService;

    public FinancialPositionDetailPageController(TenantContext tenantContext,
                                                 FinancialPositionDetailWebService financialPositionDetailWebService) {
        this.tenantContext = tenantContext;
        this.financialPositionDetailWebService = financialPositionDetailWebService;
    }

    @GetMapping("/financial-summary-detail")
    public String page(
            @RequestParam(name = "dateFrom", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dateFrom,
            @RequestParam(name = "dateTo", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dateTo,
            Model model) {

        Long tenantId = tenantContext.getCurrentTenantId();

        LocalDate from = dateFrom != null ? dateFrom : LocalDate.now().withDayOfMonth(1);
        LocalDate to = dateTo != null ? dateTo : LocalDate.now();

        model.addAttribute("financialSummary", financialPositionDetailWebService.build(tenantId, from, to));
        model.addAttribute("dateFrom", from);
        model.addAttribute("dateTo", to);
        model.addAttribute("activeMenu", "accounting");

        return "accounting/report/financial-summary-detail";
    }
}