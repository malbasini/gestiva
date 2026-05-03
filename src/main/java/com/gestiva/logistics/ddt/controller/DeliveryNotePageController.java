package com.gestiva.logistics.ddt.controller;

import com.gestiva.logistics.ddt.dto.DeliveryNoteSearchRequest;
import com.gestiva.logistics.ddt.web.DeliveryNoteWebService;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/delivery-notes")
public class DeliveryNotePageController {

    private final DeliveryNoteWebService deliveryNoteWebService;
    private final TenantContext tenantContext;

    public DeliveryNotePageController(DeliveryNoteWebService deliveryNoteWebService,
                                      TenantContext tenantContext) {
        this.deliveryNoteWebService = deliveryNoteWebService;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String search,
                       @RequestParam(required = false) String status,
                       @RequestParam(required = false) Long customerId,
                       @RequestParam(required = false) Long salesOrderId,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(defaultValue = "ddtDate") String sortBy,
                       @RequestParam(defaultValue = "desc") String sortDir,
                       @RequestParam(required = false) Long tenantId,
                       Model model) {

        Long resolvedTenantId = tenantId != null ? tenantId : tenantContext.getCurrentTenantId();

        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        var pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        DeliveryNoteSearchRequest request = new DeliveryNoteSearchRequest();
        request.setSearch(search);
        request.setStatus(status);
        request.setCustomerId(customerId);
        request.setSalesOrderId(salesOrderId);

        var result = deliveryNoteWebService.search(resolvedTenantId, request, pageable);

        model.addAttribute("deliveryNotesPage", result);
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("customerId", customerId);
        model.addAttribute("salesOrderId", salesOrderId);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("tenantId", resolvedTenantId);
        model.addAttribute("activeMenu", "ddt");

        return "ddt/delivery-note-list";
    }
}