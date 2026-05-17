package com.gestiva.inventory.valuation.controller;

import com.gestiva.inventory.valuation.repository.InventoryMovementRepository;
import com.gestiva.inventory.valuation.web.InventoryConsumptionWebService;
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

    public InventoryValuationPageController(InventoryConsumptionWebService inventoryConsumptionWebService,
                                            TenantContext tenantContext,
                                            InventoryMovementRepository inventoryMovementRepository,
                                            OutboundValuationListWebService outboundValuationListWebService) {

        this.inventoryConsumptionWebService = inventoryConsumptionWebService;
        this.tenantContext = tenantContext;
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.outboundValuationListWebService = outboundValuationListWebService;
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
}
