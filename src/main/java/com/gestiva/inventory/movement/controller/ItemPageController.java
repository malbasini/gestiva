package com.gestiva.inventory.movement.controller;

import com.gestiva.inventory.movement.web.InventoryLedgerWebService;
import com.gestiva.security.usercontext.TenantContext;
import com.gestiva.inventory.item.web.ItemForm;
import com.gestiva.inventory.item.web.ItemWebService;
import com.gestiva.inventory.stock.web.StockMovementWebService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/items")
public class ItemPageController {

    private final ItemWebService itemWebService;
    private final TenantContext tenantContext;
    private final StockMovementWebService stockMovementWebService;
    private final InventoryLedgerWebService inventoryLedgerWebService;

    public ItemPageController(ItemWebService itemWebService,
                              TenantContext tenantContext,
                              StockMovementWebService stockMovementWebService,
                              InventoryLedgerWebService inventoryLedgerWebService) {

        this.itemWebService = itemWebService;
        this.tenantContext = tenantContext;
        this.stockMovementWebService = stockMovementWebService;
        this.inventoryLedgerWebService = inventoryLedgerWebService;
    }

    @GetMapping
    public String list(@RequestParam(name = "page", defaultValue = "0") int page,
                       @RequestParam(name = "size", defaultValue = "10") int size,
                       @RequestParam(name = "q", required = false) String q,
                       @RequestParam(name = "status", required = false) String status,
                       Model model) {

        Long tenantId = tenantContext.getCurrentTenantId();
        var resultPage = itemWebService.findPage(tenantId, page, size, q, status);
        model.addAttribute("page", resultPage);
        model.addAttribute("items", resultPage);
        model.addAttribute("q", q);
        model.addAttribute("status", status);
        model.addAttribute("size", size);
        model.addAttribute("activeMenu", "items");
        return "warehouse/item/item-list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();
        var item = itemWebService.getDetail(tenantId, id);
        model.addAttribute("item", item);
        if (item.isStockManaged()) {
            model.addAttribute("recentMovements", stockMovementWebService.getRecentMovements(tenantId, id));
        } else {
            model.addAttribute("recentMovements", java.util.Collections.emptyList());
        }
        model.addAttribute("activeMenu", "items");
        return "warehouse/item/item-detail";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("itemForm", new ItemForm());
        model.addAttribute("formMode", "create");
        model.addAttribute("activeMenu", "items");
        return "warehouse/item/item-form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("itemForm") ItemForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("formMode", "create");
            model.addAttribute("activeMenu", "items");
            return "warehouse/item/item-form";
        }

        Long tenantId = tenantContext.getCurrentTenantId();
        Long id = itemWebService.create(tenantId, form);

        redirectAttributes.addFlashAttribute("successMessage", "Articolo creato con successo.");
        return "redirect:/items/" + id;
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();
        model.addAttribute("itemForm", itemWebService.getForm(tenantId, id));
        model.addAttribute("itemId", id);
        model.addAttribute("formMode", "edit");
        model.addAttribute("activeMenu", "items");
        return "warehouse/item/item-form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("itemForm") ItemForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("itemId", id);
            model.addAttribute("formMode", "edit");
            model.addAttribute("activeMenu", "items");
            return "warehouse/item/item-form";
        }

        Long tenantId = tenantContext.getCurrentTenantId();
        itemWebService.update(tenantId, id, form);

        redirectAttributes.addFlashAttribute("successMessage", "Articolo aggiornato con successo.");
        return "redirect:/items/" + id;
    }

    @GetMapping("/{id}/inventory")
    public String inventoryLedger(@PathVariable Long id, Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();

        model.addAttribute("ledger", inventoryLedgerWebService.getLedger(tenantId, id));
        model.addAttribute("activeMenu", "items");

        return "warehouse/item/item-inventory-ledger";
    }
}
