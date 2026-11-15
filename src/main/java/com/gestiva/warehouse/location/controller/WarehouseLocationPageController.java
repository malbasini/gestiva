package com.gestiva.warehouse.location.controller;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.security.usercontext.TenantContext;
import com.gestiva.warehouse.location.entity.WarehouseLocationType;
import com.gestiva.warehouse.location.service.WarehouseLocationService;
import com.gestiva.warehouse.location.service.WarehouseService;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/warehouse/locations")
@PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
public class WarehouseLocationPageController {

    private final WarehouseLocationService locationService;
    private final WarehouseService warehouseService;
    private final TenantContext tenantContext;
    private final CompanyContext companyContext;

    public WarehouseLocationPageController(
            WarehouseLocationService locationService,
            WarehouseService warehouseService,
            TenantContext tenantContext,
            CompanyContext companyContext) {

        this.locationService = locationService;
        this.warehouseService = warehouseService;
        this.tenantContext = tenantContext;
        this.companyContext = companyContext;
    }

    @GetMapping
    public String list(
            @RequestParam(required = false) Long warehouseId,
            Model model,
            RedirectAttributes redirectAttributes) {

        Long tenantId = tenantContext.getCurrentTenantId();
        Long companyId;

        try {
            companyId = companyContext.requireCurrentCompanyId();
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Seleziona una società prima di gestire le ubicazioni."
            );
            return "redirect:/company/select";
        }

        var warehouses = warehouseService.findActive(
                tenantId,
                companyId
        );

        model.addAttribute("warehouses", warehouses);
        model.addAttribute("selectedWarehouseId", warehouseId);
        model.addAttribute("activeMenu", "locations");

        if (warehouseId == null) {
            model.addAttribute("locations", List.of());
        } else {
            try {
                boolean allowedWarehouse = warehouses.stream()
                        .anyMatch(w -> warehouseId.equals(w.getId()));

                if (!allowedWarehouse) {
                    throw new BusinessException(
                            "Magazzino non disponibile per la società corrente."
                    );
                }

                model.addAttribute(
                        "locations",
                        locationService.findAll(
                                tenantId,
                                companyId,
                                warehouseId
                        )
                );
            } catch (BusinessException ex) {
                model.addAttribute("errorMessage", ex.getMessage());
                model.addAttribute("selectedWarehouseId", null);
                model.addAttribute("locations", List.of());
            }
        }

        return "warehouse/location/location-list";
    }

    @GetMapping("/new")
    public String createForm(
            @RequestParam(required = false) Long warehouseId,
            Model model,
            RedirectAttributes redirectAttributes) {

        Long tenantId = tenantContext.getCurrentTenantId();
        Long companyId;

        try {
            companyId = companyContext.requireCurrentCompanyId();
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Seleziona una società prima di creare ubicazioni."
            );
            return "redirect:/company/select";
        }

        if (warehouseId == null) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Seleziona prima un magazzino."
            );
            return "redirect:/warehouse/locations";
        }

        var warehouses = warehouseService.findActive(
                tenantId,
                companyId
        );

        boolean allowedWarehouse = warehouses.stream()
                .anyMatch(w -> warehouseId.equals(w.getId()));

        if (!allowedWarehouse) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Magazzino non disponibile per la società corrente."
            );
            return "redirect:/warehouse/locations";
        }

        var form = new WarehouseLocationForm();
        form.setWarehouseId(warehouseId);

        model.addAttribute("locationForm", form);
        model.addAttribute("warehouses", warehouses);
        model.addAttribute("locationTypes", WarehouseLocationType.values());
        model.addAttribute("activeMenu", "locations");

        model.addAttribute(
                "parentLocations",
                locationService.findAll(
                        tenantId,
                        companyId,
                        warehouseId
                ).stream()
                 .filter(location -> location.isActive())
                 .toList()
        );

        return "warehouse/location/location-form";
    }


    @PostMapping
    public String create(
            @Valid @ModelAttribute("locationForm") WarehouseLocationForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        Long tenantId = tenantContext.getCurrentTenantId();
        Long companyId;

        try {
            companyId = companyContext.requireCurrentCompanyId();
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Seleziona una società prima di creare ubicazioni."
            );
            return "redirect:/company/select";
        }

        var warehouses = warehouseService.findActive(
                tenantId,
                companyId
        );

        Long warehouseId = form.getWarehouseId();

        boolean allowedWarehouse =
                warehouseId != null &&
                warehouses.stream()
                        .anyMatch(w -> warehouseId.equals(w.getId()));

        if (!allowedWarehouse) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Magazzino non disponibile per la società corrente."
            );
            return "redirect:/warehouse/locations";
        }

        model.addAttribute("warehouses", warehouses);
        model.addAttribute("locationTypes", WarehouseLocationType.values());
        model.addAttribute("activeMenu", "locations");

        if (bindingResult.hasErrors()) {
            model.addAttribute(
                    "parentLocations",
                    locationService.findAll(
                            tenantId,
                            companyId,
                            warehouseId
                    ).stream()
                     .filter(location -> location.isActive())
                     .toList()
            );
            return "warehouse/location/location-form";
        }

        try {
            locationService.create(
                    tenantId,
                    companyId,
                    warehouseId,
                    form.getParentLocationId(),
                    form.getCode(),
                    form.getName(),
                    form.getLocationType(),
                    form.getBarcode()
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Ubicazione creata correttamente."
            );

            redirectAttributes.addAttribute(
                    "warehouseId",
                    warehouseId
            );

            return "redirect:/warehouse/locations";

        } catch (BusinessException ex) {
            model.addAttribute(
                    "errorMessage",
                    ex.getMessage()
            );

            model.addAttribute(
                    "parentLocations",
                    locationService.findAll(
                            tenantId,
                            companyId,
                            warehouseId
                    ).stream()
                     .filter(location -> location.isActive())
                     .toList()
            );

            return "warehouse/location/location-form";
        }
    }

}
