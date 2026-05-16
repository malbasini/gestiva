package com.gestiva.inventory.item.controller;

import com.gestiva.security.usercontext.TenantContext;
import com.gestiva.inventory.item.web.ItemAutocompleteView;
import com.gestiva.inventory.item.web.ItemWebService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/items")
public class ItemLookupController {

    private final ItemWebService itemWebService;
    private final TenantContext tenantContext;

    public ItemLookupController(ItemWebService itemWebService,
                                TenantContext tenantContext) {
        this.itemWebService = itemWebService;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/{id}/autocomplete")
    public ItemAutocompleteView getAutocomplete(@PathVariable Long id) {
        Long tenantId = tenantContext.getCurrentTenantId();
        return itemWebService.getAutocompleteData(tenantId, id);
    }
}