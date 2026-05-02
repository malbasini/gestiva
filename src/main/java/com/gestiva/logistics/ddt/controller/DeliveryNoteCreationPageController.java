package com.gestiva.logistics.ddt.controller;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.logistics.ddt.dto.DeliveryNoteResponse;
import com.gestiva.logistics.ddt.service.DeliveryNoteService;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/orders")
public class DeliveryNoteCreationPageController {

    private final DeliveryNoteService deliveryNoteService;
    private final TenantContext tenantContext;

    public DeliveryNoteCreationPageController(DeliveryNoteService deliveryNoteService,
                                              TenantContext tenantContext) {
        this.deliveryNoteService = deliveryNoteService;
        this.tenantContext = tenantContext;
    }

    @PostMapping("/{id}/delivery-note")
    public String createDeliveryNote(@PathVariable Long id,
                                     @RequestParam(required = false) Long tenantId,
                                     RedirectAttributes redirectAttributes) {

        Long resolvedTenantId = tenantId != null ? tenantId : tenantContext.getCurrentTenantId();

        try {
            DeliveryNoteResponse response = deliveryNoteService.createFromSalesOrder(resolvedTenantId, id);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "DDT creato con successo: " + response.getDdtNumber()
            );
            return "redirect:/delivery-notes/" + response.getId() + "?tenantId=" + resolvedTenantId;
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/orders/" + id + "?tenantId=" + resolvedTenantId;
        }
    }
}