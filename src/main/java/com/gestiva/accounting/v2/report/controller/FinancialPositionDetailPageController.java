package com.gestiva.accounting.v2.report.controller;

import com.gestiva.accounting.v2.report.web.FinancialPositionDetailCsvExportService;
import com.gestiva.accounting.v2.report.web.FinancialPositionDetailView;
import com.gestiva.accounting.v2.report.web.FinancialPositionDetailWebService;
import com.gestiva.accounting.v2.report.web.FinancialPositionDetailXlsxExportService;
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
    private final FinancialPositionDetailCsvExportService financialPositionDetailCsvExportService;
    private final FinancialPositionDetailXlsxExportService financialPositionDetailXlsxExportService;







    public FinancialPositionDetailPageController(TenantContext tenantContext,
                                                 FinancialPositionDetailWebService financialPositionDetailWebService,
                                                 FinancialPositionDetailXlsxExportService financialPositionDetailXlsxExportService,
                                                 FinancialPositionDetailCsvExportService financialPositionDetailCsvExportService) {


        this.tenantContext = tenantContext;
        this.financialPositionDetailWebService = financialPositionDetailWebService;
        this.financialPositionDetailXlsxExportService = financialPositionDetailXlsxExportService;
        this.financialPositionDetailCsvExportService = financialPositionDetailCsvExportService;
    }
    @GetMapping("/financial-summary-detail/export.xlsx")
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

        FinancialPositionDetailView view = financialPositionDetailWebService.build(tenantId, from, to);
        byte[] content = financialPositionDetailXlsxExportService.export(view);

        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=situazione_contabile_per_classi.xlsx")
                .contentType(org.springframework.http.MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(content);
    }
    @GetMapping("/financial-summary-detail/export.csv")
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

        FinancialPositionDetailView view = financialPositionDetailWebService.build(tenantId, from, to);
        byte[] content = financialPositionDetailCsvExportService.export(view);

        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=situazione_contabile_per_classi.csv")
                .contentType(new org.springframework.http.MediaType("text", "csv"))
                .body(content);
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