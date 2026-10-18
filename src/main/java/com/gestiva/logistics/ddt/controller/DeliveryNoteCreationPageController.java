package com.gestiva.logistics.ddt.controller;

import com.gestiva.common.exception.BusinessException;
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

    public DeliveryNoteCreationPageController(
            DeliveryNoteService deliveryNoteService,
            TenantContext tenantContext) {

        this.deliveryNoteService = deliveryNoteService;
        this.tenantContext = tenantContext;
    }

    @PostMapping("/{id}/delivery-note")
    public String createDeliveryNote(
            @PathVariable Long id,
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) Long warehouseId,
            RedirectAttributes redirectAttributes) {

        Long resolvedTenantId = tenantId != null
                ? tenantId
                : tenantContext.getCurrentTenantId();

        try {

            if (warehouseId == null) {
                throw new BusinessException(
                        "Selezionare un magazzino di partenza."
                );
            }

            // BLOCCO TEMPORANEO:
            // La creazione del DDT rimane disabilitata
            // finché il servizio non gestisce e valida
            // il magazzino della società corrente.
            throw new BusinessException(
                    "Creazione DDT temporaneamente non disponibile: "
                            + "validazione del magazzino in fase di completamento."
            );

        } catch (BusinessException ex) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage()
            );

            return "redirect:/orders/" + id
                    + "?tenantId=" + resolvedTenantId;
        }
    }
}