package com.gestiva.purchasing.order.controller;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.order.web.PurchaseOrderForm;
import com.gestiva.purchasing.order.web.PurchaseOrderWebService;
import com.gestiva.purchasing.supplier.web.SupplierWebService;
import com.gestiva.security.usercontext.TenantContext;
import com.gestiva.inventory.item.web.ItemWebService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/purchase-orders")
public class PurchaseOrderPageController {

    private final PurchaseOrderWebService purchaseOrderWebService;
    private final SupplierWebService supplierWebService;
    private final ItemWebService itemWebService;
    private final TenantContext tenantContext;

    public PurchaseOrderPageController(PurchaseOrderWebService purchaseOrderWebService,
                                       SupplierWebService supplierWebService,
                                       ItemWebService itemWebService,
                                       TenantContext tenantContext) {
        this.purchaseOrderWebService = purchaseOrderWebService;
        this.supplierWebService = supplierWebService;
        this.itemWebService = itemWebService;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();
        model.addAttribute("purchaseOrder", purchaseOrderWebService.getDetail(tenantId, id));
        model.addAttribute("activeMenu", "purchaseOrders");
        return "purchasing/order/purchase-order-detail";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();
        model.addAttribute("purchaseOrderForm", purchaseOrderWebService.buildCreateForm());
        model.addAttribute("supplierOptions", supplierWebService.findOptions(tenantId));
        model.addAttribute("itemOptions", itemWebService.findOptions(tenantId));
        model.addAttribute("formMode", "create");
        model.addAttribute("activeMenu", "purchaseOrders");
        return "purchasing/order/purchase-order-form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("purchaseOrderForm") PurchaseOrderForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        Long tenantId = tenantContext.getCurrentTenantId();
        try {
            if (bindingResult.hasErrors()) {
                model.addAttribute("supplierOptions", supplierWebService.findOptions(tenantId));
                model.addAttribute("itemOptions", itemWebService.findOptions(tenantId));
                model.addAttribute("formMode", "create");
                model.addAttribute("activeMenu", "purchaseOrders");
                model.addAttribute("errorMessage", bindingResult.getAllErrors().getFirst().getDefaultMessage());
                return "purchasing/order/purchase-order-form";
            }
            Long id = purchaseOrderWebService.create(tenantId, form);
            purchaseOrderWebService.validateLines(form.getLines());
            redirectAttributes.addFlashAttribute("successMessage", "Ordine fornitore creato con successo.");
            return "redirect:/purchase-orders/" + id;
        }
        catch(Exception ex)
        {
            model.addAttribute("supplierOptions", supplierWebService.findOptions(tenantId));
            model.addAttribute("itemOptions", itemWebService.findOptions(tenantId));
            model.addAttribute("formMode", "create");
            model.addAttribute("activeMenu", "purchaseOrders");
            model.addAttribute("errorMessage", ex.getMessage());
            return "purchasing/order/purchase-order-form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();
        model.addAttribute("purchaseOrderForm", purchaseOrderWebService.getForm(tenantId, id));
        model.addAttribute("supplierOptions", supplierWebService.findOptions(tenantId));
        model.addAttribute("itemOptions", itemWebService.findOptions(tenantId));
        model.addAttribute("purchaseOrderId", id);
        model.addAttribute("formMode", "edit");
        model.addAttribute("activeMenu", "purchaseOrders");
        return "purchasing/order/purchase-order-form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("purchaseOrderForm") PurchaseOrderForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        Long tenantId = tenantContext.getCurrentTenantId();
        if (bindingResult.hasErrors()) {
            model.addAttribute("supplierOptions", supplierWebService.findOptions(tenantId));
            model.addAttribute("itemOptions", itemWebService.findOptions(tenantId));
            model.addAttribute("purchaseOrderId", id);
            model.addAttribute("formMode", "edit");
            model.addAttribute("activeMenu", "purchaseOrders");
            model.addAttribute("errorMessage", bindingResult.getAllErrors().getFirst().getDefaultMessage());
            return "purchasing/order/purchase-order-form";
        }

        try {
            purchaseOrderWebService.validateLines(form.getLines());
            purchaseOrderWebService.update(tenantId, id, form);
            redirectAttributes.addFlashAttribute("successMessage", "Ordine fornitore aggiornato con successo.");
        }
        catch(Exception ex)
        {
            model.addAttribute("supplierOptions", supplierWebService.findOptions(tenantId));
            model.addAttribute("itemOptions", itemWebService.findOptions(tenantId));
            model.addAttribute("purchaseOrderId", id);
            model.addAttribute("formMode", "edit");
            model.addAttribute("activeMenu", "purchaseOrders");
            model.addAttribute("errorMessage", ex.getMessage());
            return "purchasing/order/purchase-order-form";
        }
        return "redirect:/purchase-orders/" + id;
    }
    @PostMapping(params = "addLine")
    public String addLineCreate(@ModelAttribute("purchaseOrderForm") PurchaseOrderForm form,
                                Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();

        form.getLines().add(purchaseOrderWebService.buildDefaultLine());

        model.addAttribute("supplierOptions", supplierWebService.findOptions(tenantId));
        model.addAttribute("itemOptions", itemWebService.findOptions(tenantId));
        model.addAttribute("formMode", "create");
        model.addAttribute("activeMenu", "purchaseOrders");
        return "purchasing/order/purchase-order-form";
    }
    @PostMapping(params = "removeLine")
    public String removeLineCreate(@ModelAttribute("purchaseOrderForm") PurchaseOrderForm form,
                                   @RequestParam("removeLine") int index,
                                   Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();

        if (form.getLines() != null && index >= 0 && index < form.getLines().size()) {
            form.getLines().remove(index);
        }

        if (form.getLines() == null || form.getLines().isEmpty()) {
            form.getLines().add(purchaseOrderWebService.buildDefaultLine());
        }

        model.addAttribute("supplierOptions", supplierWebService.findOptions(tenantId));
        model.addAttribute("itemOptions", itemWebService.findOptions(tenantId));
        model.addAttribute("formMode", "create");
        model.addAttribute("activeMenu", "purchaseOrders");
        return "purchasing/order/purchase-order-form";
    }

    @PostMapping(value = "/{id}", params = "addLine")
    public String addLineEdit(@PathVariable Long id,
                              @ModelAttribute("purchaseOrderForm") PurchaseOrderForm form,
                              Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();

        form.getLines().add(purchaseOrderWebService.buildDefaultLine());

        model.addAttribute("supplierOptions", supplierWebService.findOptions(tenantId));
        model.addAttribute("itemOptions", itemWebService.findOptions(tenantId));
        model.addAttribute("purchaseOrderId", id);
        model.addAttribute("formMode", "edit");
        model.addAttribute("activeMenu", "purchaseOrders");
        return "purchasing/order/purchase-order-form";
    }

    @PostMapping(value = "/{id}", params = "removeLine")
    public String removeLineEdit(@PathVariable Long id,
                                 @ModelAttribute("purchaseOrderForm") PurchaseOrderForm form,
                                 @RequestParam("removeLine") int index,
                                 Model model) {
        Long tenantId = tenantContext.getCurrentTenantId();

        if (form.getLines() != null && index >= 0 && index < form.getLines().size()) {
            form.getLines().remove(index);
        }

        if (form.getLines() == null || form.getLines().isEmpty()) {
            form.getLines().add(purchaseOrderWebService.buildDefaultLine());
        }

        model.addAttribute("supplierOptions", supplierWebService.findOptions(tenantId));
        model.addAttribute("itemOptions", itemWebService.findOptions(tenantId));
        model.addAttribute("purchaseOrderId", id);
        model.addAttribute("formMode", "edit");
        model.addAttribute("activeMenu", "purchaseOrders");
        return "purchasing/order/purchase-order-form";
    }

    @GetMapping
    public String list(@RequestParam(name = "page", defaultValue = "0") int page,
                       @RequestParam(name = "size", defaultValue = "10") int size,
                       @RequestParam(name = "q", required = false) String q,
                       @RequestParam(name = "status", required = false) String status,
                       @RequestParam(name = "dateFrom", required = false)
                       @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate dateFrom,
                       @RequestParam(name = "dateTo", required = false)
                       @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
                       java.time.LocalDate dateTo,
                       Model model) {

        Long tenantId = tenantContext.getCurrentTenantId();

        var resultPage = purchaseOrderWebService.findPage(tenantId, page, size, q, status, dateFrom, dateTo);
        model.addAttribute("page", resultPage);
        model.addAttribute("q", q);
        model.addAttribute("status", status);
        model.addAttribute("dateFrom", dateFrom);
        model.addAttribute("dateTo", dateTo);
        model.addAttribute("size", size);
        model.addAttribute("activeMenu", "purchaseOrders");

        return "purchasing/order/purchase-order-list";
    }








}