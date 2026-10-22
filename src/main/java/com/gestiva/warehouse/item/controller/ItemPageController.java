package com.gestiva.warehouse.item.controller;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.warehouse.item.web.ItemMovementWebService;
import com.gestiva.warehouse.inventory.movement.web.InventoryLedgerWebService;
import com.gestiva.warehouse.inventory.valuation.web.InventoryValuationWebService;
import com.gestiva.security.usercontext.TenantContext;
import com.gestiva.warehouse.item.web.ItemForm;
import com.gestiva.warehouse.item.web.ItemWebService;
import com.gestiva.warehouse.uom.repository.UnitOfMeasureRepository;
import com.gestiva.warehouse.item.repository.ItemRepository;
import com.gestiva.warehouse.uom.entity.UnitOfMeasure;
import com.gestiva.common.exception.NotFoundException;
import java.util.ArrayList;
import java.util.List;
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
    private final InventoryLedgerWebService inventoryLedgerWebService;
    private final InventoryValuationWebService inventoryValuationWebService;
    private final ItemMovementWebService itemMovementWebService;
    private final UnitOfMeasureRepository unitOfMeasureRepository;
    private final ItemRepository itemRepository;


    public ItemPageController(ItemWebService itemWebService,
                              TenantContext tenantContext,
                              InventoryLedgerWebService inventoryLedgerWebService,
                              InventoryValuationWebService inventoryValuationWebService,
                              ItemMovementWebService itemMovementWebService,
                              UnitOfMeasureRepository unitOfMeasureRepository,
                              ItemRepository itemRepository) {

        this.itemWebService = itemWebService;
        this.tenantContext = tenantContext;
        this.inventoryLedgerWebService = inventoryLedgerWebService;
        this.inventoryValuationWebService = inventoryValuationWebService;
        this.itemMovementWebService = itemMovementWebService;
        this.unitOfMeasureRepository = unitOfMeasureRepository;
        this.itemRepository = itemRepository;
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
        model.addAttribute("recentMovements", itemMovementWebService.findRecentMovements(tenantId, id));
        model.addAttribute("activeMenu", "items");
        return "warehouse/item/item-detail";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute(
                "availableUoms",
                unitOfMeasureRepository.findByTenantIdAndActiveTrueOrderByCodeAsc(
                        tenantContext.getCurrentTenantId()
                )
        );
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
            model.addAttribute(
                    "availableUoms",
                    unitOfMeasureRepository.findByTenantIdAndActiveTrueOrderByCodeAsc(
                            tenantContext.getCurrentTenantId()
                    )
            );
            model.addAttribute("formMode", "create");
            model.addAttribute("activeMenu", "items");
            return "warehouse/item/item-form";
        }

        Long tenantId = tenantContext.getCurrentTenantId();

        try {
            Long id = itemWebService.create(tenantId, form);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Articolo creato con successo."
            );

            return "redirect:/items/" + id;

        } catch (BusinessException ex) {

            model.addAttribute("errorMessage", ex.getMessage());

            model.addAttribute(
                    "availableUoms",
                    unitOfMeasureRepository
                            .findByTenantIdAndActiveTrueOrderByCodeAsc(tenantId)
            );

            model.addAttribute("formMode", "create");
            model.addAttribute("activeMenu", "items");

            return "warehouse/item/item-form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();
        model.addAttribute("itemForm", itemWebService.getForm(tenantId, id));
        model.addAttribute(
                "availableUoms",
                availableUomsForEdit(tenantId, id)
        );
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
            model.addAttribute(
                    "availableUoms",
                    availableUomsForEdit(
                            tenantContext.getCurrentTenantId(),
                            id
                    )
            );
            model.addAttribute("itemId", id);
            model.addAttribute("formMode", "edit");
            model.addAttribute("activeMenu", "items");
            return "warehouse/item/item-form";
        }

        Long tenantId = tenantContext.getCurrentTenantId();

        try {
            itemWebService.update(tenantId, id, form);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Articolo aggiornato con successo."
            );

            return "redirect:/items/" + id;

        } catch (BusinessException ex) {

            model.addAttribute("errorMessage", ex.getMessage());

            model.addAttribute(
                    "availableUoms",
                    availableUomsForEdit(tenantId, id)
            );

            model.addAttribute("itemId", id);
            model.addAttribute("formMode", "edit");
            model.addAttribute("activeMenu", "items");

            return "warehouse/item/item-form";
        }
    }

    private List<UnitOfMeasure> availableUomsForEdit(
            Long tenantId,
            Long itemId) {

        List<UnitOfMeasure> available = new ArrayList<>(
                unitOfMeasureRepository
                        .findByTenantIdAndActiveTrueOrderByCodeAsc(tenantId)
        );

        var item = itemRepository.findByTenantIdAndId(tenantId, itemId)
                .orElseThrow(() ->
                        new NotFoundException("Articolo non trovato")
                );

        UnitOfMeasure currentUom = item.getBaseUom();

        if (currentUom != null
                && available.stream().noneMatch(
                        uom -> uom.getId().equals(currentUom.getId())
                )) {
            available.add(currentUom);
        }

        available.sort(
                java.util.Comparator.comparing(UnitOfMeasure::getCode)
        );

        return available;
    }

    @GetMapping("/{id}/inventory")
    public String inventoryLedger(@PathVariable Long id, Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();

        model.addAttribute("ledger", inventoryLedgerWebService.getLedger(tenantId, id));
        model.addAttribute("activeMenu", "items");

        return "warehouse/item/item-inventory-ledger";
    }
    @GetMapping("/{id}/valuation")
    public String valuation(@PathVariable Long id, Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();
        model.addAttribute("valuation", inventoryValuationWebService.getItemValuation(tenantId, id));
        model.addAttribute("activeMenu", "items");
        return "warehouse/item/item-valuation";
    }
}
