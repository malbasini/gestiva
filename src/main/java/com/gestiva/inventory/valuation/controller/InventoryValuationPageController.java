package com.gestiva.inventory.valuation.controller;

import com.gestiva.inventory.item.web.ItemWebService;
import com.gestiva.inventory.valuation.repository.InventoryMovementRepository;
import com.gestiva.inventory.valuation.web.CostOfGoodsSoldSummaryWebService;
import com.gestiva.inventory.valuation.web.InventoryConsumptionWebService;
import com.gestiva.inventory.valuation.web.InventoryStockValuationListWebService;
import com.gestiva.inventory.valuation.web.OutboundValuationListWebService;
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


    public InventoryValuationPageController(InventoryConsumptionWebService inventoryConsumptionWebService,
                                            TenantContext tenantContext,
                                            InventoryMovementRepository inventoryMovementRepository,
                                            OutboundValuationListWebService outboundValuationListWebService,
                                            CostOfGoodsSoldSummaryWebService costOfGoodsSoldSummaryWebService,
                                            ItemWebService itemWebService,
                                            InventoryStockValuationListWebService inventoryStockValuationListWebService) {

        this.inventoryConsumptionWebService = inventoryConsumptionWebService;
        this.tenantContext = tenantContext;
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.outboundValuationListWebService = outboundValuationListWebService;
        this.costOfGoodsSoldSummaryWebService = costOfGoodsSoldSummaryWebService;
        this.itemWebService = itemWebService;
        this.inventoryStockValuationListWebService=inventoryStockValuationListWebService;
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


}
