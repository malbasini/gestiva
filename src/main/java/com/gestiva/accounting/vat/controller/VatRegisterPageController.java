package com.gestiva.accounting.vat.controller;

import com.gestiva.accounting.vat.web.VatPurchaseRegisterWebService;
import com.gestiva.accounting.vat.web.VatSalesRegisterWebService;
import com.gestiva.accounting.vat.web.VatSettlementWebService;
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



    public VatRegisterPageController(TenantContext tenantContext,
                                     VatSalesRegisterWebService vatSalesRegisterWebService,
                                     VatPurchaseRegisterWebService vatPurchaseRegisterWebService,
                                     VatSettlementWebService vatSettlementWebService) {

        this.tenantContext = tenantContext;
        this.vatSalesRegisterWebService = vatSalesRegisterWebService;
        this.vatPurchaseRegisterWebService = vatPurchaseRegisterWebService;
        this.vatSettlementWebService = vatSettlementWebService;
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
    public String monthlySettlement(
            @RequestParam(name = "year", required = false) Integer year,
            @RequestParam(name = "month", required = false) Integer month,
            Model model) {

        Long tenantId = tenantContext.getCurrentTenantId();

        LocalDate now = LocalDate.now();
        int selectedYear = year != null ? year : now.getYear();
        int selectedMonth = month != null ? month : now.getMonthValue();

        model.addAttribute("settlement",
                vatSettlementWebService.calculateMonthlySettlement(tenantId, selectedYear, selectedMonth));
        model.addAttribute("selectedYear", selectedYear);
        model.addAttribute("selectedMonth", selectedMonth);
        model.addAttribute("activeMenu", "accounting");

        return "accounting/vat/vat-monthly-settlement";
    }
}