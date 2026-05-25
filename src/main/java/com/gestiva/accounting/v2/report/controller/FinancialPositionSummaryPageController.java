package com.gestiva.accounting.v2.report.controller;

import com.gestiva.accounting.v2.report.web.FinancialPositionSummaryCsvExportService;
import com.gestiva.accounting.v2.report.web.FinancialPositionSummaryView;
import com.gestiva.accounting.v2.report.web.FinancialPositionSummaryWebService;
import com.gestiva.accounting.v2.report.web.FinancialPositionSummaryXlsxExportService;
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
public class FinancialPositionSummaryPageController {

    private final TenantContext tenantContext;
    private final FinancialPositionSummaryWebService financialPositionSummaryWebService;
    private final FinancialPositionSummaryCsvExportService financialPositionSummaryCsvExportService;
    private final FinancialPositionSummaryXlsxExportService financialPositionSummaryXlsxExportService;

    public FinancialPositionSummaryPageController(TenantContext tenantContext,
                                                  FinancialPositionSummaryWebService financialPositionSummaryWebService,
                                                  FinancialPositionSummaryCsvExportService financialPositionSummaryCsvExportService,
                                                  FinancialPositionSummaryXlsxExportService financialPositionSummaryXlsxExportService) {
        this.tenantContext = tenantContext;
        this.financialPositionSummaryWebService = financialPositionSummaryWebService;
        this.financialPositionSummaryCsvExportService = financialPositionSummaryCsvExportService;
        this.financialPositionSummaryXlsxExportService = financialPositionSummaryXlsxExportService;
    }

    @GetMapping("/financial-summary")
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

        model.addAttribute("financialSummary", financialPositionSummaryWebService.build(tenantId, from, to));
        model.addAttribute("dateFrom", from);
        model.addAttribute("dateTo", to);
        model.addAttribute("activeMenu", "accounting");

        return "accounting/report/financial-summary";
    }

    @GetMapping("/financial-summary/export.csv")
    public org.springframework.http.ResponseEntity<byte[]> exportCsv(
            @RequestParam(name = "dateFrom", required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate dateFrom,
            @RequestParam(name = "dateTo", required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate dateTo) {

        Long tenantId = tenantContext.getCurrentTenantId();

        java.time.LocalDate from = dateFrom != null ? dateFrom : java.time.LocalDate.now().withDayOfMonth(1);
        java.time.LocalDate to = dateTo != null ? dateTo : java.time.LocalDate.now();

        FinancialPositionSummaryView summary = financialPositionSummaryWebService.build(tenantId, from, to);
        byte[] content = financialPositionSummaryCsvExportService.export(summary);

        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=situazione_contabile_sintetica.csv")
                .contentType(new org.springframework.http.MediaType("text", "csv"))
                .body(content);
    }

    @GetMapping("/financial-summary/export.xlsx")
    public org.springframework.http.ResponseEntity<byte[]> exportXlsx(
            @RequestParam(name = "dateFrom", required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate dateFrom,
            @RequestParam(name = "dateTo", required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate dateTo) {

        Long tenantId = tenantContext.getCurrentTenantId();

        java.time.LocalDate from = dateFrom != null ? dateFrom : java.time.LocalDate.now().withDayOfMonth(1);
        java.time.LocalDate to = dateTo != null ? dateTo : java.time.LocalDate.now();

        FinancialPositionSummaryView summary = financialPositionSummaryWebService.build(tenantId, from, to);
        byte[] content = financialPositionSummaryXlsxExportService.export(summary);

        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=situazione_contabile_sintetica.xlsx")
                .contentType(org.springframework.http.MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(content);
    }
}
