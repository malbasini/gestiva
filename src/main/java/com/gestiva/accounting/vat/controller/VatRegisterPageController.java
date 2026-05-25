package com.gestiva.accounting.vat.controller;

import com.gestiva.accounting.vat.web.*;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/vat-registers")
public class VatRegisterPageController {

    private final TenantContext tenantContext;
    private final VatSalesRegisterWebService vatSalesRegisterWebService;
    private final VatPurchaseRegisterWebService vatPurchaseRegisterWebService;
    private final VatSettlementWebService vatSettlementWebService;
    private final VatSalesRegisterCsvExportService vatSalesRegisterCsvExportService;
    private final VatPurchaseRegisterCsvExportService vatPurchaseRegisterCsvExportService;
    private final VatSettlementCsvExportService vatSettlementCsvExportService;
    private final VatSalesRegisterXlsxExportService vatSalesRegisterXlsxExportService;
    private final VatPurchaseRegisterXlsxExportService vatPurchaseRegisterXlsxExportService;
    private final VatSettlementXlsxExportService vatSettlementXlsxExportService;


    public VatRegisterPageController(TenantContext tenantContext,
                                     VatSalesRegisterWebService vatSalesRegisterWebService,
                                     VatPurchaseRegisterWebService vatPurchaseRegisterWebService,
                                     VatSettlementWebService vatSettlementWebService,
                                     VatSalesRegisterCsvExportService vatSalesRegisterCsvExportService,
                                     VatPurchaseRegisterCsvExportService vatPurchaseRegisterCsvExportService,
                                     VatSettlementCsvExportService vatSettlementCsvExportService,
                                     VatSalesRegisterXlsxExportService vatSalesRegisterXlsxExportService,
                                     VatPurchaseRegisterXlsxExportService vatPurchaseRegisterXlsxExportService,
                                     VatSettlementXlsxExportService vatSettlementXlsxExportService) {

        this.tenantContext = tenantContext;
        this.vatSalesRegisterWebService = vatSalesRegisterWebService;
        this.vatPurchaseRegisterWebService = vatPurchaseRegisterWebService;
        this.vatSettlementWebService = vatSettlementWebService;
        this.vatSalesRegisterCsvExportService = vatSalesRegisterCsvExportService;
        this.vatPurchaseRegisterCsvExportService = vatPurchaseRegisterCsvExportService;
        this.vatSettlementCsvExportService = vatSettlementCsvExportService;
        this.vatSalesRegisterXlsxExportService = vatSalesRegisterXlsxExportService;
        this.vatPurchaseRegisterXlsxExportService = vatPurchaseRegisterXlsxExportService;
        this.vatSettlementXlsxExportService = vatSettlementXlsxExportService;
    }

    @GetMapping("/sales")
    public String salesRegister(
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

        model.addAttribute("rows", vatSalesRegisterWebService.findRows(tenantId, from, to));
        model.addAttribute("dateFrom", from);
        model.addAttribute("dateTo", to);
        model.addAttribute("activeMenu", "accounting");

        return "accounting/vat/vat-sales-register";
    }

    @GetMapping("/purchases")
    public String purchaseRegister(
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

        model.addAttribute("rows", vatPurchaseRegisterWebService.findRows(tenantId, from, to));
        model.addAttribute("dateFrom", from);
        model.addAttribute("dateTo", to);
        model.addAttribute("activeMenu", "accounting");

        return "accounting/vat/vat-purchase-register";
    }

    @GetMapping("/settlement")
    public String settlement(
            @RequestParam(name = "dateFrom", required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate dateFrom,
            @RequestParam(name = "dateTo", required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate dateTo,
            Model model) {

        Long tenantId = tenantContext.getCurrentTenantId();

        java.time.LocalDate from = dateFrom != null ? dateFrom : java.time.LocalDate.now().withDayOfMonth(1);
        java.time.LocalDate to = dateTo != null ? dateTo : java.time.LocalDate.now();

        model.addAttribute("settlement", vatSettlementWebService.calculateSettlement(tenantId, from, to));
        model.addAttribute("dateFrom", from);
        model.addAttribute("dateTo", to);
        model.addAttribute("activeMenu", "accounting");

        return "accounting/vat/vat-monthly-settlement";
    }

    @GetMapping("/sales/export.csv")
    public org.springframework.http.ResponseEntity<byte[]> exportSalesRegisterCsv(
            @RequestParam(name = "dateFrom", required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate dateFrom,
            @RequestParam(name = "dateTo", required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate dateTo) {

        Long tenantId = tenantContext.getCurrentTenantId();

        java.time.LocalDate from = dateFrom != null ? dateFrom : java.time.LocalDate.now().withDayOfMonth(1);
        java.time.LocalDate to = dateTo != null ? dateTo : java.time.LocalDate.now();

        var rows = vatSalesRegisterWebService.findRows(tenantId, from, to);
        byte[] content = vatSalesRegisterCsvExportService.export(rows);

        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=registro_iva_vendite.csv")
                .contentType(new org.springframework.http.MediaType("text", "csv"))
                .body(content);
    }

    @GetMapping("/purchases/export.csv")
    public org.springframework.http.ResponseEntity<byte[]> exportPurchaseRegisterCsv(
            @RequestParam(name = "dateFrom", required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate dateFrom,
            @RequestParam(name = "dateTo", required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate dateTo) {

        Long tenantId = tenantContext.getCurrentTenantId();

        java.time.LocalDate from = dateFrom != null ? dateFrom : java.time.LocalDate.now().withDayOfMonth(1);
        java.time.LocalDate to = dateTo != null ? dateTo : java.time.LocalDate.now();

        var rows = vatPurchaseRegisterWebService.findRows(tenantId, from, to);
        byte[] content = vatPurchaseRegisterCsvExportService.export(rows);

        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=registro_iva_acquisti.csv")
                .contentType(new org.springframework.http.MediaType("text", "csv"))
                .body(content);
    }

    @GetMapping("/settlement/export.csv")
    public org.springframework.http.ResponseEntity<byte[]> exportSettlementCsv(
            @RequestParam(name = "dateFrom", required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate dateFrom,
            @RequestParam(name = "dateTo", required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate dateTo) {

        Long tenantId = tenantContext.getCurrentTenantId();

        java.time.LocalDate from = dateFrom != null ? dateFrom : java.time.LocalDate.now().withDayOfMonth(1);
        java.time.LocalDate to = dateTo != null ? dateTo : java.time.LocalDate.now();

        VatSettlementView settlement = vatSettlementWebService.calculateSettlement(tenantId, from, to);
        byte[] content = vatSettlementCsvExportService.export(settlement);

        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=liquidazione_iva.csv")
                .contentType(new org.springframework.http.MediaType("text", "csv"))
                .body(content);
    }

    @GetMapping("/sales/export.xlsx")
    public org.springframework.http.ResponseEntity<byte[]> exportSalesRegisterXlsx(
            @RequestParam(name = "dateFrom", required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate dateFrom,
            @RequestParam(name = "dateTo", required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate dateTo) {

        Long tenantId = tenantContext.getCurrentTenantId();

        java.time.LocalDate from = dateFrom != null ? dateFrom : java.time.LocalDate.now().withDayOfMonth(1);
        java.time.LocalDate to = dateTo != null ? dateTo : java.time.LocalDate.now();

        var rows = vatSalesRegisterWebService.findRows(tenantId, from, to);
        byte[] content = vatSalesRegisterXlsxExportService.export(rows);

        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=registro_iva_vendite.xlsx")
                .contentType(org.springframework.http.MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(content);
    }

    @GetMapping("/settlement/export.xlsx")
    public org.springframework.http.ResponseEntity<byte[]> exportSettlementXlsx(
            @RequestParam(name = "dateFrom", required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate dateFrom,
            @RequestParam(name = "dateTo", required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate dateTo) {

        Long tenantId = tenantContext.getCurrentTenantId();

        java.time.LocalDate from = dateFrom != null ? dateFrom : java.time.LocalDate.now().withDayOfMonth(1);
        java.time.LocalDate to = dateTo != null ? dateTo : java.time.LocalDate.now();

        VatSettlementView settlement = vatSettlementWebService.calculateSettlement(tenantId, from, to);
        byte[] content = vatSettlementXlsxExportService.export(settlement);

        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=liquidazione_iva.xlsx")
                .contentType(org.springframework.http.MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(content);
    }

    @GetMapping("/purchases/export.xlsx")
    public org.springframework.http.ResponseEntity<byte[]> exportPurchaseRegisterXlsx(
            @RequestParam(name = "dateFrom", required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate dateFrom,
            @RequestParam(name = "dateTo", required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate dateTo) {

        Long tenantId = tenantContext.getCurrentTenantId();

        java.time.LocalDate from = dateFrom != null ? dateFrom : java.time.LocalDate.now().withDayOfMonth(1);
        java.time.LocalDate to = dateTo != null ? dateTo : java.time.LocalDate.now();

        var rows = vatPurchaseRegisterWebService.findRows(tenantId, from, to);
        byte[] content = vatPurchaseRegisterXlsxExportService.export(rows);

        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=registro_iva_acquisti.xlsx")
                .contentType(org.springframework.http.MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(content);
    }
}