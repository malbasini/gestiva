package com.gestiva.inventory.valuation.controller;

import com.gestiva.inventory.item.web.ItemWebService;
import com.gestiva.inventory.valuation.repository.InventoryMovementRepository;
import com.gestiva.inventory.valuation.web.*;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/inventory-valuations")
public class InventoryValuationPageController {

    private final InventoryConsumptionWebService inventoryConsumptionWebService;
    private final TenantContext tenantContext;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final OutboundValuationListWebService outboundValuationListWebService;
    private final CostOfGoodsSoldSummaryWebService costOfGoodsSoldSummaryWebService;
    private final ItemWebService itemWebService;
    private final InventoryStockValuationListWebService inventoryStockValuationListWebService;
    private final InventoryConsistencyCheckWebService inventoryConsistencyCheckWebService;
    private final OutboundValuationCsvExportService outboundValuationCsvExportService;
    private final CostOfGoodsSoldSummaryCsvExportService costOfGoodsSoldSummaryCsvExportService;
    private final InventoryStockValuationCsvExportService inventoryStockValuationCsvExportService;
    private final InventoryConsistencyCheckCsvExportService inventoryConsistencyCheckCsvExportService;


    public InventoryValuationPageController(InventoryConsumptionWebService inventoryConsumptionWebService,
                                            TenantContext tenantContext,
                                            InventoryMovementRepository inventoryMovementRepository,
                                            OutboundValuationListWebService outboundValuationListWebService,
                                            CostOfGoodsSoldSummaryWebService costOfGoodsSoldSummaryWebService,
                                            ItemWebService itemWebService,
                                            InventoryStockValuationListWebService inventoryStockValuationListWebService,
                                            InventoryConsistencyCheckWebService inventoryConsistencyCheckWebService,
                                            OutboundValuationCsvExportService outboundValuationCsvExportService,
                                            CostOfGoodsSoldSummaryCsvExportService costOfGoodsSoldSummaryCsvExportService,
                                            InventoryStockValuationCsvExportService inventoryStockValuationCsvExportService,
                                            InventoryConsistencyCheckCsvExportService inventoryConsistencyCheckCsvExportService) {

        this.inventoryConsumptionWebService = inventoryConsumptionWebService;
        this.tenantContext = tenantContext;
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.outboundValuationListWebService = outboundValuationListWebService;
        this.costOfGoodsSoldSummaryWebService = costOfGoodsSoldSummaryWebService;
        this.itemWebService = itemWebService;
        this.inventoryStockValuationListWebService=inventoryStockValuationListWebService;
        this.inventoryConsistencyCheckWebService=inventoryConsistencyCheckWebService;
        this.outboundValuationCsvExportService=outboundValuationCsvExportService;
        this.costOfGoodsSoldSummaryCsvExportService=costOfGoodsSoldSummaryCsvExportService;
        this.inventoryStockValuationCsvExportService=inventoryStockValuationCsvExportService;
        this.inventoryConsistencyCheckCsvExportService=inventoryConsistencyCheckCsvExportService;
    }

    @GetMapping("/movements/{movementId}")
    public String outboundDetail(@PathVariable Long movementId, Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();

        model.addAttribute("valuationDetail",
                inventoryConsumptionWebService.getOutboundValuationDetail(tenantId, movementId));
        model.addAttribute("activeMenu", "items");

        return "warehouse/inventory/inventory-outbound-valuation-detail";
    }

    @GetMapping("/outbound")
    public String outboundValuations(@RequestParam(name = "page", defaultValue = "0") int page,
                                     @RequestParam(name = "size", defaultValue = "10") int size,
                                     @RequestParam(name = "q", required = false) String q,
                                     @RequestParam(name = "causalCode", required = false) String causalCode,
                                     @RequestParam(name = "dateFrom", required = false)
                                     @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
                                     java.time.LocalDate dateFrom,
                                     @RequestParam(name = "dateTo", required = false)
                                     @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
                                     java.time.LocalDate dateTo,
                                     Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();

        var resultPage = outboundValuationListWebService.findPage(
                tenantId, page, size, q, causalCode, dateFrom, dateTo
        );

        model.addAttribute("page", resultPage);
        model.addAttribute("q", q);
        model.addAttribute("causalCode", causalCode);
        model.addAttribute("dateFrom", dateFrom);
        model.addAttribute("dateTo", dateTo);
        model.addAttribute("size", size);
        model.addAttribute("causalOptions",
                inventoryMovementRepository.findDistinctOutboundCausalCodesByTenantId(tenantId));
        model.addAttribute("activeMenu", "items");

        return "warehouse/inventory/inventory-outbound-valuations";
    }
    @GetMapping("/stock-summary/export.csv")
    public org.springframework.http.ResponseEntity<byte[]> exportStockSummaryCsv(
            @RequestParam(name = "q", required = false) String q) {

        Long tenantId = tenantContext.getCurrentTenantId();

        var rows = inventoryStockValuationListWebService.findAll(tenantId, q);
        byte[] content = inventoryStockValuationCsvExportService.export(rows);

        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=giacenza_valorizzata.csv")
                .contentType(new org.springframework.http.MediaType("text", "csv"))
                .body(content);
    }
    @GetMapping("/cogs-summary/export.csv")
    public org.springframework.http.ResponseEntity<byte[]> exportCostOfGoodsSoldSummaryCsv(
            @RequestParam(name = "itemId", required = false) Long itemId,
            @RequestParam(name = "dateFrom", required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate dateFrom,
            @RequestParam(name = "dateTo", required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate dateTo) {

        Long tenantId = tenantContext.getCurrentTenantId();

        var rows = costOfGoodsSoldSummaryWebService.summarize(tenantId, itemId, dateFrom, dateTo);
        byte[] content = costOfGoodsSoldSummaryCsvExportService.export(rows);

        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=riepilogo_costo_del_venduto.csv")
                .contentType(new org.springframework.http.MediaType("text", "csv"))
                .body(content);
    }
    @GetMapping("/cogs-summary")
    public String costOfGoodsSoldSummary(@RequestParam(name = "itemId", required = false) Long itemId,
                                         @RequestParam(name = "dateFrom", required = false)
                                         @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
                                         java.time.LocalDate dateFrom,
                                         @RequestParam(name = "dateTo", required = false)
                                         @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
                                         java.time.LocalDate dateTo,
                                         Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();

        model.addAttribute("rows",
                costOfGoodsSoldSummaryWebService.summarize(tenantId, itemId, dateFrom, dateTo));
        model.addAttribute("itemOptions", itemWebService.findStockManagedOptions(tenantId));
        model.addAttribute("itemId", itemId);
        model.addAttribute("dateFrom", dateFrom);
        model.addAttribute("dateTo", dateTo);
        model.addAttribute("activeMenu", "items");

        return "warehouse/inventory/inventory-cogs-summary";
    }

    @GetMapping("/stock-summary")
    public String stockSummary(@RequestParam(name = "q", required = false) String q,
                               Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();

        model.addAttribute("rows", inventoryStockValuationListWebService.findAll(tenantId, q));
        model.addAttribute("q", q);
        model.addAttribute("activeMenu", "items");

        return "warehouse/inventory/inventory-stock-summary";
    }

    @GetMapping("/consistency-check")
    public String consistencyCheck(@RequestParam(name = "q", required = false) String q,
                                   @RequestParam(name = "onlyDifferences", required = false) Boolean onlyDifferences,
                                   Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();

        try {
            model.addAttribute("rows",
                    inventoryConsistencyCheckWebService.findAll(tenantId, q, onlyDifferences));
            model.addAttribute("q", q);
            model.addAttribute("onlyDifferences", Boolean.TRUE.equals(onlyDifferences));
            model.addAttribute("activeMenu", "items");
            return "warehouse/inventory/inventory-consistency-check";
        }
        catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("activeMenu", "items");
            return "warehouse/inventory/inventory-consistency-check";
        }
    }
    @GetMapping("/consistency-check/export.csv")
    public org.springframework.http.ResponseEntity<byte[]> exportConsistencyCheckCsv(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "onlyDifferences", required = false) Boolean onlyDifferences) {

        Long tenantId = tenantContext.getCurrentTenantId();

        var rows = inventoryConsistencyCheckWebService.findAll(tenantId, q, onlyDifferences);
        byte[] content = inventoryConsistencyCheckCsvExportService.export(rows);

        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=controllo_coerenza_magazzino.csv")
                .contentType(new org.springframework.http.MediaType("text", "csv"))
                .body(content);
    }
    @GetMapping("/outbound/export.csv")
    public org.springframework.http.ResponseEntity<byte[]> exportOutboundValuationsCsv(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "causalCode", required = false) String causalCode,
            @RequestParam(name = "dateFrom", required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate dateFrom,
            @RequestParam(name = "dateTo", required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate dateTo) {

        Long tenantId = tenantContext.getCurrentTenantId();

        var rows = outboundValuationListWebService.findAll(tenantId, q, causalCode, dateFrom, dateTo);
        byte[] content = outboundValuationCsvExportService.export(rows);

        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=scarichi_valorizzati.csv")
                .contentType(new org.springframework.http.MediaType("text", "csv"))
                .body(content);
    }
}
