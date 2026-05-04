package com.gestiva.logistics.ddt.controller;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.logistics.ddt.service.DeliveryNoteWorkflowService;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/delivery-notes")
public class DeliveryNoteWorkflowPageController {

    private final DeliveryNoteWorkflowService deliveryNoteWorkflowService;
    private final TenantContext tenantContext;

    public DeliveryNoteWorkflowPageController(DeliveryNoteWorkflowService deliveryNoteWorkflowService,
                                              TenantContext tenantContext) {
        this.deliveryNoteWorkflowService = deliveryNoteWorkflowService;
        this.tenantContext = tenantContext;
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id,
                         @RequestParam(required = false) Long tenantId,
                         RedirectAttributes redirectAttributes) {

        Long resolvedTenantId = tenantId != null ? tenantId : tenantContext.getCurrentTenantId();

        try {
            deliveryNoteWorkflowService.cancel(resolvedTenantId, id);
            redirectAttributes.addFlashAttribute("successMessage", "DDT annullato con successo.");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/delivery-notes/" + id + "?tenantId=" + resolvedTenantId;
    }
}