package com.gestiva.warehouse.uom.controller;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.usercontext.TenantContext;
import com.gestiva.warehouse.uom.entity.UnitOfMeasureDimension;
import com.gestiva.warehouse.uom.service.UnitOfMeasureService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/warehouse/uoms")
@PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
public class UnitOfMeasurePageController {

    private final UnitOfMeasureService service;
    private final TenantContext tenantContext;

    public UnitOfMeasurePageController(
            UnitOfMeasureService service,
            TenantContext tenantContext
    ) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    public String list(Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();

        model.addAttribute("uoms", service.findAll(tenantId));
        model.addAttribute("activeMenu", "uoms");

        return "warehouse/uom/uom-list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("uomForm", new UomForm());
        prepareForm(model);

        return "warehouse/uom/uom-form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("uomForm") UomForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            prepareForm(model);
            return "warehouse/uom/uom-form";
        }

        try {
            service.create(
                    tenantContext.getCurrentTenantId(),
                    form.getCode(),
                    form.getName(),
                    form.getSymbol(),
                    form.getDimension(),
                    form.getDecimalPlaces()
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Unità di misura creata correttamente."
            );

            return "redirect:/warehouse/uoms";

        } catch (BusinessException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            prepareForm(model);

            return "warehouse/uom/uom-form";
        }
    }

    private void prepareForm(Model model) {
        model.addAttribute(
                "dimensions",
                UnitOfMeasureDimension.values()
        );
        model.addAttribute("activeMenu", "uoms");
    }

    public static class UomForm {

        @NotBlank
        @Size(max = 20)
        private String code;

        @NotBlank
        @Size(max = 100)
        private String name;

        @NotBlank
        @Size(max = 20)
        private String symbol;

        @NotNull
        private UnitOfMeasureDimension dimension;

        @NotNull
        @Min(0)
        @Max(6)
        private Integer decimalPlaces = 0;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public UnitOfMeasureDimension getDimension() {
            return dimension;
        }

        public void setDimension(UnitOfMeasureDimension dimension) {
            this.dimension = dimension;
        }

        public Integer getDecimalPlaces() {
            return decimalPlaces;
        }

        public void setDecimalPlaces(Integer decimalPlaces) {
            this.decimalPlaces = decimalPlaces;
        }
    }
}
