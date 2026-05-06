package com.gestiva.warehouse.item.controller;

import com.gestiva.security.usercontext.TenantContext;
import com.gestiva.warehouse.item.web.ItemForm;
import com.gestiva.warehouse.item.web.ItemWebService;
import com.gestiva.warehouse.stock.web.StockMovementWebService;
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

    public ItemPageController(ItemWebService itemWebService,
                              TenantContext tenantContext,
                              StockMovementWebService stockMovementWebService) {

        this.itemWebService = itemWebService;
        this.tenantContext = tenantContext;
        this.stockMovementWebService = stockMovementWebService;
    }

    @GetMapping
    public String list(Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();
        model.addAttribute("items", itemWebService.findAll(tenantId));
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
}
