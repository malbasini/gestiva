package com.gestiva.purchasing.supplier.controller;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.supplier.web.SupplierForm;
import com.gestiva.purchasing.supplier.web.SupplierWebService;
import com.gestiva.security.usercontext.TenantContext;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/suppliers")
public class SupplierPageController {

    private final SupplierWebService supplierWebService;
    private final TenantContext tenantContext;

    public SupplierPageController(SupplierWebService supplierWebService,
                                  TenantContext tenantContext) {
        this.supplierWebService = supplierWebService;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    public String list(@RequestParam(name = "page", defaultValue = "0") int page,
                       @RequestParam(name = "size", defaultValue = "10") int size,
                       @RequestParam(name = "q", required = false) String q,
                       @RequestParam(name = "status", required = false) String status,
                       Model model) {

        Long tenantId = tenantContext.getCurrentTenantId();
        var resultPage = supplierWebService.findPage(tenantId, page, size, q, status);
        model.addAttribute("suppliers", resultPage);
        model.addAttribute("page", resultPage);
        model.addAttribute("q", q);
        model.addAttribute("status", status);
        model.addAttribute("size", size);
        model.addAttribute("activeMenu", "suppliers");
        return "purchasing/supplier/supplier-list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();
        model.addAttribute("supplier", supplierWebService.getDetail(tenantId, id));
        model.addAttribute("activeMenu", "suppliers");
        return "purchasing/supplier/supplier-detail";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("supplierForm", new SupplierForm());
        model.addAttribute("formMode", "create");
        model.addAttribute("activeMenu", "suppliers");
        return "purchasing/supplier/supplier-form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("supplierForm") SupplierForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("formMode", "create");
            model.addAttribute("activeMenu", "suppliers");
            return "purchasing/supplier/supplier-form";
        }
        Long id = null;
        try {
            Long tenantId = tenantContext.getCurrentTenantId();
            id = supplierWebService.create(tenantId, form);
            redirectAttributes.addFlashAttribute("successMessage", "Fornitore creato con successo.");
        }
        catch(BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/suppliers/" + id;
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();
        model.addAttribute("supplierForm", supplierWebService.getForm(tenantId, id));
        model.addAttribute("supplierId", id);
        model.addAttribute("formMode", "edit");
        model.addAttribute("activeMenu", "suppliers");
        return "purchasing/supplier/supplier-form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("supplierForm") SupplierForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("supplierId", id);
            model.addAttribute("formMode", "edit");
            model.addAttribute("activeMenu", "suppliers");
            return "purchasing/supplier/supplier-form";
        }

        Long tenantId = tenantContext.getCurrentTenantId();
        supplierWebService.update(tenantId, id, form);

        redirectAttributes.addFlashAttribute("successMessage", "Fornitore aggiornato con successo.");
        return "redirect:/suppliers/" + id;
    }
}
