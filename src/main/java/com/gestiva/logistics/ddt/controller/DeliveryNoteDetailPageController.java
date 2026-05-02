package com.gestiva.logistics.ddt.controller;

import com.gestiva.logistics.ddt.web.DeliveryNoteDetailWebService;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/delivery-notes")
public class DeliveryNoteDetailPageController {

    private final DeliveryNoteDetailWebService deliveryNoteDetailWebService;
    private final TenantContext tenantContext;

    public DeliveryNoteDetailPageController(DeliveryNoteDetailWebService deliveryNoteDetailWebService,
                                            TenantContext tenantContext) {
        this.deliveryNoteDetailWebService = deliveryNoteDetailWebService;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         @RequestParam(required = false) Long tenantId,
                         Model model) {

        Long resolvedTenantId = tenantId != null ? tenantId : tenantContext.getCurrentTenantId();

        var deliveryNote = deliveryNoteDetailWebService.getDetail(resolvedTenantId, id);

        model.addAttribute("deliveryNote", deliveryNote);
        model.addAttribute("tenantId", resolvedTenantId);
        model.addAttribute("activeMenu", "orders");

        return "ddt/delivery-note-detail";
    }
}