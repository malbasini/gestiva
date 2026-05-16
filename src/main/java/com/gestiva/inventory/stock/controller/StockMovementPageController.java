package com.gestiva.inventory.stock.controller;

import com.gestiva.security.usercontext.TenantContext;
import com.gestiva.inventory.stock.web.StockMovementForm;
import com.gestiva.inventory.stock.web.StockMovementWebService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/items/{itemId}/stock-movements")
public class StockMovementPageController {

    private final StockMovementWebService stockMovementWebService;
    private final TenantContext tenantContext;

    public StockMovementPageController(StockMovementWebService stockMovementWebService,
                                       TenantContext tenantContext) {
        this.stockMovementWebService = stockMovementWebService;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/new")
    public String createForm(@PathVariable Long itemId, Model model) {
        StockMovementForm form = new StockMovementForm();
        form.setMovementDate(LocalDate.now());
        form.setDirection("IN");
        form.setReasonCode("MANUAL_LOAD");

        model.addAttribute("stockMovementForm", form);
        model.addAttribute("itemId", itemId);
        model.addAttribute("activeMenu", "items");
        return "warehouse/stock/stock-movement-form";
    }

    @PostMapping
    public String create(@PathVariable Long itemId,
                         @Valid @ModelAttribute("stockMovementForm") StockMovementForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("itemId", itemId);
            model.addAttribute("activeMenu", "items");
            return "warehouse/stock/stock-movement-form";
        }

        Long tenantId = tenantContext.getCurrentTenantId();
        stockMovementWebService.createManualMovement(tenantId, itemId, form);

        redirectAttributes.addFlashAttribute("successMessage", "Movimento di magazzino registrato con successo.");
        return "redirect:/items/" + itemId;
    }
}