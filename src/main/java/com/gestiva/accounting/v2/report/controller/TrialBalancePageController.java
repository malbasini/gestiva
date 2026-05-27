package com.gestiva.accounting.v2.report.controller;

import com.gestiva.accounting.v2.report.web.TrialBalanceCsvExportService;
import com.gestiva.accounting.v2.report.web.TrialBalanceView;
import com.gestiva.accounting.v2.report.web.TrialBalanceWebService;
import com.gestiva.accounting.v2.report.web.TrialBalanceXlsxExportService;
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
public class TrialBalancePageController {

    private final TenantContext tenantContext;
    private final TrialBalanceWebService trialBalanceWebService;
    private final TrialBalanceCsvExportService trialBalanceCsvExportService;
    private final TrialBalanceXlsxExportService trialBalanceXlsxExportService;

    public TrialBalancePageController(TenantContext tenantContext,
                                      TrialBalanceWebService trialBalanceWebService,
                                      TrialBalanceCsvExportService trialBalanceCsvExportService,
                                      TrialBalanceXlsxExportService trialBalanceXlsxExportService) {

        this.tenantContext = tenantContext;
        this.trialBalanceWebService = trialBalanceWebService;
        this.trialBalanceCsvExportService = trialBalanceCsvExportService;
        this.trialBalanceXlsxExportService = trialBalanceXlsxExportService;
    }
    @GetMapping("/trial-balance/export.csv")
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

        TrialBalanceView trialBalance = trialBalanceWebService.build(tenantId, from, to);
        byte[] content = trialBalanceCsvExportService.export(trialBalance);

        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=bilancino_di_verifica.csv")
                .contentType(new org.springframework.http.MediaType("text", "csv"))
                .body(content);
    }
    @GetMapping("/trial-balance/export.xlsx")
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

        TrialBalanceView trialBalance = trialBalanceWebService.build(tenantId, from, to);
        byte[] content = trialBalanceXlsxExportService.export(trialBalance);

        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=bilancino_di_verifica.xlsx")
                .contentType(org.springframework.http.MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(content);
    }
    @GetMapping("/trial-balance")
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

        model.addAttribute("trialBalance", trialBalanceWebService.build(tenantId, from, to));
        model.addAttribute("dateFrom", from);
        model.addAttribute("dateTo", to);
        model.addAttribute("activeMenu", "accounting");

        return "accounting/report/trial-balance";
    }
}