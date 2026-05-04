package com.gestiva.billing.invoice.controller;

import com.gestiva.billing.invoice.dto.InvoiceSearchRequest;
import com.gestiva.billing.invoice.web.InvoiceWebService;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/invoices")
public class InvoicePageController {

    private final InvoiceWebService invoiceWebService;
    private final TenantContext tenantContext;

    public InvoicePageController(InvoiceWebService invoiceWebService,
                                 TenantContext tenantContext) {
        this.invoiceWebService = invoiceWebService;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String search,
                       @RequestParam(required = false) String status,
                       @RequestParam(required = false) Long customerId,
                       @RequestParam(required = false) Long deliveryNoteId,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(defaultValue = "invoiceDate") String sortBy,
                       @RequestParam(defaultValue = "desc") String sortDir,
                       @RequestParam(required = false) Long tenantId,
                       Model model) {

        Long resolvedTenantId = tenantId != null ? tenantId : tenantContext.getCurrentTenantId();

        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        var pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        InvoiceSearchRequest request = new InvoiceSearchRequest();
        request.setSearch(search);
        request.setStatus(status);
        request.setCustomerId(customerId);
        request.setDeliveryNoteId(deliveryNoteId);

        var result = invoiceWebService.search(resolvedTenantId, request, pageable);

        model.addAttribute("invoicesPage", result);
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("customerId", customerId);
        model.addAttribute("deliveryNoteId", deliveryNoteId);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("tenantId", resolvedTenantId);
        model.addAttribute("activeMenu", "invoices");
        return "invoice/invoice-list";
    }
}