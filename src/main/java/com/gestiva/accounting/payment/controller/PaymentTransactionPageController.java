package com.gestiva.accounting.payment.controller;

import com.gestiva.accounting.payment.web.PaymentTransactionCsvExportService;
import com.gestiva.accounting.payment.web.PaymentTransactionListWebService;
import com.gestiva.accounting.payment.web.PaymentTransactionXlsxExportService;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/payments")
public class PaymentTransactionPageController {

    private final TenantContext tenantContext;
    private final PaymentTransactionListWebService paymentTransactionListWebService;
    private final PaymentTransactionCsvExportService paymentTransactionCsvExportService;
    private final PaymentTransactionXlsxExportService paymentTransactionXlsxExportService;







    public PaymentTransactionPageController(TenantContext tenantContext,
                                            PaymentTransactionListWebService paymentTransactionListWebService,
                                            PaymentTransactionCsvExportService paymentTransactionCsvExportService,
                                            PaymentTransactionXlsxExportService paymentTransactionXlsxExportService) {
        this.tenantContext = tenantContext;
        this.paymentTransactionListWebService = paymentTransactionListWebService;
        this.paymentTransactionCsvExportService = paymentTransactionCsvExportService;
        this.paymentTransactionXlsxExportService = paymentTransactionXlsxExportService;
    }
    @GetMapping("/export.csv")
    public org.springframework.http.ResponseEntity<byte[]> exportCsv(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "direction", required = false) String direction) {

        Long tenantId = tenantContext.getCurrentTenantId();

        var resultPage = paymentTransactionListWebService.findPage(tenantId, page, size, direction);
        byte[] content = paymentTransactionCsvExportService.export(resultPage);

        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=incassi_pagamenti.csv")
                .contentType(new org.springframework.http.MediaType("text", "csv"))
                .body(content);
    }

    @GetMapping("/export.xlsx")
    public org.springframework.http.ResponseEntity<byte[]> exportXlsx(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "direction", required = false) String direction) {

        Long tenantId = tenantContext.getCurrentTenantId();

        var resultPage = paymentTransactionListWebService.findPage(tenantId, page, size, direction);
        byte[] content = paymentTransactionXlsxExportService.export(resultPage);

        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=incassi_pagamenti.xlsx")
                .contentType(org.springframework.http.MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(content);
    }
    @GetMapping
    public String list(@RequestParam(name = "page", defaultValue = "0") int page,
                       @RequestParam(name = "size", defaultValue = "10") int size,
                       @RequestParam(name = "direction", required = false) String direction,
                       Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();

        var resultPage = paymentTransactionListWebService.findPage(tenantId, page, size, direction);

        model.addAttribute("page", resultPage);
        model.addAttribute("direction", direction);
        model.addAttribute("size", size);
        model.addAttribute("activeMenu", "accounting");

        return "accounting/payment/payment-transaction-list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();

        var detail = paymentTransactionListWebService.findDetail(tenantId, id);

        model.addAttribute("payment", detail);
        model.addAttribute("activeMenu", "accounting");

        return "accounting/payment/payment-transaction-detail";
    }
}