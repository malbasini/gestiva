package com.gestiva.accounting.v2.report.controller;

import com.gestiva.accounting.v2.report.web.IncomeStatementCsvExportService;
import com.gestiva.accounting.v2.report.web.IncomeStatementView;
import com.gestiva.accounting.v2.report.web.IncomeStatementWebService;
import com.gestiva.accounting.v2.report.web.IncomeStatementXlsxExportService;
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
public class IncomeStatementPageController {

    private final TenantContext tenantContext;
    private final IncomeStatementWebService incomeStatementWebService;
    private final IncomeStatementCsvExportService incomeStatementCsvExportService;
    private final IncomeStatementXlsxExportService incomeStatementXlsxExportService;

    public IncomeStatementPageController(TenantContext tenantContext,
                                         IncomeStatementWebService incomeStatementWebService,
                                         IncomeStatementCsvExportService incomeStatementCsvExportService,
                                         IncomeStatementXlsxExportService incomeStatementXlsxExportService) {

        this.tenantContext = tenantContext;
        this.incomeStatementWebService = incomeStatementWebService;
        this.incomeStatementCsvExportService = incomeStatementCsvExportService;
        this.incomeStatementXlsxExportService = incomeStatementXlsxExportService;
    }

    @GetMapping("/income-statement/export.csv")
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

        IncomeStatementView view = incomeStatementWebService.build(tenantId, from, to);
        byte[] content = incomeStatementCsvExportService.export(view);

        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=conto_economico.csv")
                .contentType(new org.springframework.http.MediaType("text", "csv"))
                .body(content);
    }
    @GetMapping("/income-statement/export.xlsx")
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

        IncomeStatementView view = incomeStatementWebService.build(tenantId, from, to);
        byte[] content = incomeStatementXlsxExportService.export(view);

        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=conto_economico.xlsx")
                .contentType(org.springframework.http.MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(content);
    }
    @GetMapping("/income-statement")
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

        model.addAttribute("incomeStatement", incomeStatementWebService.build(tenantId, from, to));
        model.addAttribute("dateFrom", from);
        model.addAttribute("dateTo", to);
        model.addAttribute("activeMenu", "accounting");

        return "accounting/report/income-statement";
    }
}
