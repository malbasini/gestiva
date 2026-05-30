package com.gestiva.accounting.v2.report.controller;

import com.gestiva.accounting.v2.account.repository.AccountRepository;
import com.gestiva.accounting.v2.report.web.AccountLedgerCsvExportService;
import com.gestiva.accounting.v2.report.web.AccountLedgerView;
import com.gestiva.accounting.v2.report.web.AccountLedgerWebService;
import com.gestiva.accounting.v2.report.web.AccountLedgerXlsxExportService;
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
public class AccountLedgerPageController {

    private final TenantContext tenantContext;
    private final AccountLedgerWebService accountLedgerWebService;
    private final AccountRepository accountRepository;
    private final AccountLedgerCsvExportService accountLedgerCsvExportService;
    private final AccountLedgerXlsxExportService accountLedgerXlsxExportService;




    public AccountLedgerPageController(TenantContext tenantContext,
                                       AccountLedgerWebService accountLedgerWebService,
                                       AccountRepository accountRepository,
                                       AccountLedgerCsvExportService accountLedgerCsvExportService,
                                       AccountLedgerXlsxExportService accountLedgerXlsxExportService) {
        
        this.tenantContext = tenantContext;
        this.accountLedgerWebService = accountLedgerWebService;
        this.accountRepository = accountRepository;
        this.accountLedgerCsvExportService = accountLedgerCsvExportService;
        this.accountLedgerXlsxExportService = accountLedgerXlsxExportService;
    }
    @GetMapping("/account-ledger/export.csv")
    public org.springframework.http.ResponseEntity<byte[]> exportCsv(
            @RequestParam(name = "accountId") Long accountId,
            @RequestParam(name = "dateFrom", required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate dateFrom,
            @RequestParam(name = "dateTo", required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate dateTo) {

        Long tenantId = tenantContext.getCurrentTenantId();
        java.time.LocalDate from = dateFrom != null ? dateFrom : java.time.LocalDate.now().withDayOfMonth(1);
        java.time.LocalDate to = dateTo != null ? dateTo : java.time.LocalDate.now();

        AccountLedgerView ledger = accountLedgerWebService.build(tenantId, accountId, from, to);
        byte[] content = accountLedgerCsvExportService.export(ledger);

        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=mastrino_conto.csv")
                .contentType(new org.springframework.http.MediaType("text", "csv"))
                .body(content);
    }
    @GetMapping("/account-ledger/export.xlsx")
    public org.springframework.http.ResponseEntity<byte[]> exportXlsx(
            @RequestParam(name = "accountId") Long accountId,
            @RequestParam(name = "dateFrom", required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate dateFrom,
            @RequestParam(name = "dateTo", required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate dateTo) {

        Long tenantId = tenantContext.getCurrentTenantId();
        java.time.LocalDate from = dateFrom != null ? dateFrom : java.time.LocalDate.now().withDayOfMonth(1);
        java.time.LocalDate to = dateTo != null ? dateTo : java.time.LocalDate.now();

        AccountLedgerView ledger = accountLedgerWebService.build(tenantId, accountId, from, to);
        byte[] content = accountLedgerXlsxExportService.export(ledger);

        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=mastrino_conto.xlsx")
                .contentType(org.springframework.http.MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(content);
    }
    @GetMapping("/account-ledger")
    public String page(
            @RequestParam(name = "accountId") Long accountId,
            @RequestParam(name = "dateFrom", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(name = "dateTo", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            Model model) {

        Long tenantId = tenantContext.getCurrentTenantId();
        LocalDate from = dateFrom != null ? dateFrom : LocalDate.now().withDayOfMonth(1);
        LocalDate to = dateTo != null ? dateTo : LocalDate.now();

        model.addAttribute("ledger", accountLedgerWebService.build(tenantId, accountId, from, to));
        model.addAttribute("accounts", accountRepository.findByTenantIdAndLeafAccountTrueAndActiveTrueOrderByCodeAsc(tenantId));
        model.addAttribute("accountId", accountId);
        model.addAttribute("dateFrom", from);
        model.addAttribute("dateTo", to);
        model.addAttribute("activeMenu", "accounting");

        return "accounting/report/account-ledger";
    }
}
