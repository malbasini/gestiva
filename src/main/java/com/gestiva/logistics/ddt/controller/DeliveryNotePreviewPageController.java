package com.gestiva.logistics.ddt.controller;

import com.gestiva.logistics.ddt.web.DeliveryNoteDocumentWebService;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/delivery-notes")
public class DeliveryNotePreviewPageController {

    private final DeliveryNoteDocumentWebService deliveryNoteDocumentWebService;
    private final TenantContext tenantContext;

    public DeliveryNotePreviewPageController(DeliveryNoteDocumentWebService deliveryNoteDocumentWebService,
                                             TenantContext tenantContext) {
        this.deliveryNoteDocumentWebService = deliveryNoteDocumentWebService;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/{id}/preview")
    public String preview(@PathVariable Long id,
                          @RequestParam(required = false) Long tenantId,
                          Model model) {

        Long resolvedTenantId = tenantId != null ? tenantId : tenantContext.getCurrentTenantId();

        model.addAttribute("deliveryNote", deliveryNoteDocumentWebService.getDocument(resolvedTenantId, id));
        model.addAttribute("tenantId", resolvedTenantId);
        model.addAttribute("activeMenu", "ddt");

        return "ddt/delivery-note-preview";
    }
}