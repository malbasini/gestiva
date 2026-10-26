package com.gestiva.warehouse.item.controller;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.usercontext.TenantContext;
import com.gestiva.warehouse.item.entity.Item;
import com.gestiva.warehouse.item.repository.ItemRepository;
import com.gestiva.warehouse.item.repository.ItemUomRepository;
import com.gestiva.warehouse.item.service.ItemUomService;
import com.gestiva.warehouse.item.web.ItemUomForm;
import com.gestiva.warehouse.uom.entity.UnitOfMeasure;
import com.gestiva.warehouse.uom.repository.UnitOfMeasureRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/items/{itemId}/uoms")
public class ItemUomPageController {

    private final TenantContext tenantContext;
    private final ItemRepository itemRepository;
    private final ItemUomRepository itemUomRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;
    private final ItemUomService itemUomService;

    public ItemUomPageController(
            TenantContext tenantContext,
            ItemRepository itemRepository,
            ItemUomRepository itemUomRepository,
            UnitOfMeasureRepository unitOfMeasureRepository,
            ItemUomService itemUomService) {

        this.tenantContext = tenantContext;
        this.itemRepository = itemRepository;
        this.itemUomRepository = itemUomRepository;
        this.unitOfMeasureRepository = unitOfMeasureRepository;
        this.itemUomService = itemUomService;
    }

    @GetMapping("/new")
    public String createForm(
            @PathVariable Long itemId,
            Model model) {

        Long tenantId = tenantContext.getCurrentTenantId();
        Item item = findItem(tenantId, itemId);

        model.addAttribute("itemUomForm", new ItemUomForm());
        prepareModel(model, tenantId, item);

        return "warehouse/item/item-uom-form";
    }

    @PostMapping
    public String create(
            @PathVariable Long itemId,
            @Valid @ModelAttribute("itemUomForm") ItemUomForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        Long tenantId = tenantContext.getCurrentTenantId();
        Item item = findItem(tenantId, itemId);

        if (bindingResult.hasErrors()) {
            prepareModel(model, tenantId, item);
            return "warehouse/item/item-uom-form";
        }

        try {
            itemUomService.create(
                    tenantId,
                    itemId,
                    form.getUomId(),
                    form.getConversionFactor(),
                    form.isPurchaseUom(),
                    form.isSalesUom(),
                    form.getBarcode()
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Unità alternativa associata correttamente."
            );

            return "redirect:/items/" + itemId;

        } catch (BusinessException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            prepareModel(model, tenantId, item);
            return "warehouse/item/item-uom-form";
        }
    }

    private Item findItem(Long tenantId, Long itemId) {
        return itemRepository.findWithBaseUomByTenantIdAndId(tenantId, itemId)
                .orElseThrow(() -> new BusinessException(
                        "Articolo non trovato."
                ));
    }

    private void prepareModel(
            Model model,
            Long tenantId,
            Item item) {

        Set<Long> alreadyAssociated = itemUomRepository
                .findByTenantIdAndItemIdOrderByIdAsc(
                        tenantId,
                        item.getId()
                )
                .stream()
                .map(association -> association.getUom().getId())
                .collect(Collectors.toSet());

        List<UnitOfMeasure> availableUoms =
                unitOfMeasureRepository
                        .findByTenantIdAndActiveTrueOrderByCodeAsc(tenantId)
                        .stream()
                        .filter(uom ->
                                uom.getDimension()
                                        == item.getBaseUom().getDimension()
                        )
                        .filter(uom ->
                                !uom.getId().equals(item.getBaseUom().getId())
                        )
                        .filter(uom ->
                                !alreadyAssociated.contains(uom.getId())
                        )
                        .toList();

        model.addAttribute("item", item);
        model.addAttribute("baseUom", item.getBaseUom());
        model.addAttribute("availableUoms", availableUoms);
        model.addAttribute("activeMenu", "items");
    }
}
