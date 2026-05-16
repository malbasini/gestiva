package com.gestiva.inventory.item.controller;

import com.gestiva.inventory.valuation.web.InventoryValuationWebService;
import com.gestiva.security.usercontext.TenantContext;
import com.gestiva.inventory.item.web.ItemAutocompleteView;
import com.gestiva.inventory.item.web.ItemWebService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/items")
public class ItemLookupController {

    private final ItemWebService itemWebService;
    private final TenantContext tenantContext;
    private final InventoryValuationWebService inventoryValuationWebService;

    public ItemLookupController(ItemWebService itemWebService,
                                TenantContext tenantContext,
                                InventoryValuationWebService inventoryValuationWebService) {

        this.itemWebService = itemWebService;
        this.tenantContext = tenantContext;
        this.inventoryValuationWebService = inventoryValuationWebService;
    }

    @GetMapping("/{id}/autocomplete")
    public ItemAutocompleteView getAutocomplete(@PathVariable Long id) {
        Long tenantId = tenantContext.getCurrentTenantId();
        return itemWebService.getAutocompleteData(tenantId, id);
    }
}