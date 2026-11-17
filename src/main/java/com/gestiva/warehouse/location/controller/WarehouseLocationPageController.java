package com.gestiva.warehouse.location.controller;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.security.usercontext.TenantContext;
import com.gestiva.warehouse.location.entity.WarehouseLocationType;
import com.gestiva.warehouse.location.service.WarehouseLocationService;
import com.gestiva.warehouse.location.service.WarehouseLocationDeactivationService;
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
    private final WarehouseLocationDeactivationService deactivationService;
    private final WarehouseService warehouseService;
    private final TenantContext tenantContext;
    private final CompanyContext companyContext;

    public WarehouseLocationPageController(
            WarehouseLocationService locationService,
            WarehouseLocationDeactivationService deactivationService,
            WarehouseService warehouseService,
            TenantContext tenantContext,
            CompanyContext companyContext) {

        this.locationService = locationService;
        this.deactivationService = deactivationService;
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



    @GetMapping("/{locationId}/edit")
    public String editForm(
            @org.springframework.web.bind.annotation.PathVariable Long locationId,
            @RequestParam Long warehouseId,
            Model model,
            RedirectAttributes redirectAttributes) {

        Long tenantId = tenantContext.getCurrentTenantId();
        Long companyId;

        try {
            companyId = companyContext.requireCurrentCompanyId();
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Seleziona una società prima di modificare ubicazioni."
            );
            return "redirect:/company/select";
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

        try {
            var location = locationService.getById(
                    tenantId,
                    companyId,
                    warehouseId,
                    locationId
            );

            var form = new WarehouseLocationForm();
            form.setWarehouseId(warehouseId);
            form.setCode(location.getCode());
            form.setName(location.getName());
            form.setLocationType(location.getLocationType());
            form.setBarcode(location.getBarcode());

            if (location.getParentLocation() != null) {
                form.setParentLocationId(
                        location.getParentLocation().getId()
                );
            }

            model.addAttribute("locationForm", form);
            model.addAttribute("editMode", true);
            model.addAttribute("editingLocationId", locationId);
            model.addAttribute("warehouses", warehouses);
            model.addAttribute(
                    "locationTypes",
                    WarehouseLocationType.values()
            );
            model.addAttribute("activeMenu", "locations");

            model.addAttribute(
                    "parentLocations",
                    locationService.findAll(
                            tenantId,
                            companyId,
                            warehouseId
                    ).stream()
                     .filter(item -> item.isActive())
                     .filter(item -> !locationId.equals(item.getId()))
                     .toList()
            );

            return "warehouse/location/location-form";

        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage()
            );
            redirectAttributes.addAttribute(
                    "warehouseId",
                    warehouseId
            );
            return "redirect:/warehouse/locations";
        }
    }


    @PostMapping("/{locationId}/edit")
    public String update(
            @org.springframework.web.bind.annotation.PathVariable Long locationId,
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
                    "Seleziona una società prima di modificare ubicazioni."
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

        model.addAttribute("editMode", true);
        model.addAttribute("editingLocationId", locationId);
        model.addAttribute("warehouses", warehouses);
        model.addAttribute(
                "locationTypes",
                WarehouseLocationType.values()
        );
        model.addAttribute("activeMenu", "locations");

        try {
            var existingLocation = locationService.getById(
                    tenantId,
                    companyId,
                    warehouseId,
                    locationId
            );

            // Codice e padre provengono sempre dal record esistente,
            // mai dai valori modificabili della richiesta HTTP.
            form.setCode(existingLocation.getCode());
            form.setParentLocationId(
                    existingLocation.getParentLocation() == null
                            ? null
                            : existingLocation.getParentLocation().getId()
            );

            model.addAttribute(
                    "parentLocations",
                    locationService.findAll(
                            tenantId,
                            companyId,
                            warehouseId
                    ).stream()
                     .filter(item -> item.isActive())
                     .filter(item -> !locationId.equals(item.getId()))
                     .toList()
            );

            if (bindingResult.hasErrors()) {
                return "warehouse/location/location-form";
            }

            locationService.updateMetadata(
                    tenantId,
                    companyId,
                    warehouseId,
                    locationId,
                    form.getName(),
                    form.getLocationType(),
                    form.getBarcode()
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Ubicazione modificata correttamente."
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

            if (!model.containsAttribute("parentLocations")) {
                model.addAttribute("parentLocations", List.of());
            }

            return "warehouse/location/location-form";
        }
    }


    // Gestiva V2 - Disattivazione logica protetta di un'ubicazione.
    @PostMapping("/{locationId}/deactivate")
    public String deactivate(
            @org.springframework.web.bind.annotation.PathVariable Long locationId,
            @RequestParam Long warehouseId,
            RedirectAttributes redirectAttributes) {

        Long tenantId = tenantContext.getCurrentTenantId();
        Long companyId;

        try {
            companyId = companyContext.requireCurrentCompanyId();
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Seleziona una società prima di disattivare ubicazioni."
            );
            return "redirect:/company/select";
        }

        try {
            boolean allowedWarehouse = warehouseService.findActive(
                    tenantId,
                    companyId
            ).stream().anyMatch(w -> warehouseId.equals(w.getId()));

            if (!allowedWarehouse) {
                throw new BusinessException(
                        "Magazzino non disponibile per la società corrente."
                );
            }

            deactivationService.deactivate(
                    tenantId,
                    companyId,
                    warehouseId,
                    locationId
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Ubicazione disattivata correttamente."
            );

        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage()
            );
        }

        redirectAttributes.addAttribute("warehouseId", warehouseId);
        return "redirect:/warehouse/locations";
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
