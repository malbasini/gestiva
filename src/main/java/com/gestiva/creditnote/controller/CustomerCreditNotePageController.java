
package com.gestiva.creditnote.controller;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.creditnote.service.CustomerCreditNoteDraftService;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.security.usercontext.TenantContext;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/customer-credit-notes")
public class CustomerCreditNotePageController {

    private final TenantContext tenantContext;
    private final CompanyContext companyContext;
    private final CustomerCreditNoteDraftService draftService;

    public CustomerCreditNotePageController(
            TenantContext tenantContext,
            CompanyContext companyContext,
            CustomerCreditNoteDraftService draftService) {

        this.tenantContext = tenantContext;
        this.companyContext = companyContext;
        this.draftService = draftService;
    }

    /**
     * GESTIVA V2 - PASSO 5.13.2395BR
     *
     * Creazione nota di credito cliente completa.
     *
     * POST /customer-credit-notes
     */
    @PostMapping
    public String createFullDraft(
            @RequestParam("customerInvoiceId") Long customerInvoiceId,
            @RequestParam("reason") String reason,
            RedirectAttributes redirectAttributes) {

        Long tenantId = tenantContext.getCurrentTenantId();

        companyContext.requireCurrentCompanyId();

        if (customerInvoiceId == null
                || customerInvoiceId <= 0) {

            throw new BusinessException(
                    "Identificativo fattura cliente non valido.");
        }

        String redirect =
                "redirect:/invoices/" + customerInvoiceId;

        try {
            Long noteId = draftService.createFullDraft(
                    tenantId,
                    customerInvoiceId,
                    reason);

            if (noteId == null || noteId <= 0) {
                throw new IllegalStateException(
                        "Creazione nota cliente senza ID valido.");
            }

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Nota di credito cliente DRAFT creata. "
                            + "ID: " + noteId);

            // PASSO 5.13.2395DY
            // Creazione riuscita: apri il dettaglio CCN.
            return "redirect:/customer-credit-notes/" + noteId;


        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage());
        }

        return redirect;
    }

    /**
     * Creazione nota cliente parziale.
     *
     * POST /customer-credit-notes/partial
     */
    @PostMapping("/partial")
    public String createPartialDraft(
            @RequestParam("customerInvoiceId") Long customerInvoiceId,
            @RequestParam("reason") String reason,
            @RequestParam("lineIds") List<Long> lineIds,
            @RequestParam("quantities") List<BigDecimal> quantities,
            RedirectAttributes redirectAttributes) {

        Long tenantId = tenantContext.getCurrentTenantId();

        companyContext.requireCurrentCompanyId();

        if (customerInvoiceId == null
                || customerInvoiceId <= 0) {

            throw new BusinessException(
                    "Identificativo fattura cliente non valido.");
        }

        String redirect =
                "redirect:/invoices/" + customerInvoiceId;

        try {
            if (reason == null
                    || reason.isBlank()
                    || reason.length() > 500) {

                throw new BusinessException(
                        "Indicare una causale valida "
                                + "(massimo 500 caratteri).");
            }

            if (lineIds == null
                    || quantities == null
                    || lineIds.isEmpty()
                    || lineIds.size() != quantities.size()
                    || lineIds.size() > 500) {

                throw new BusinessException(
                        "Righe o quantità della nota non valide.");
            }

            Map<Long, BigDecimal> selected =
                    new LinkedHashMap<>();

            for (int i = 0; i < lineIds.size(); i++) {
                Long lineId = lineIds.get(i);
                BigDecimal quantity = quantities.get(i);

                if (lineId == null
                        || lineId <= 0
                        || quantity == null
                        || quantity.signum() <= 0
                        || quantity.scale() > 3) {

                    throw new BusinessException(
                            "Identificativo riga "
                                    + "o quantità non valido.");
                }

                if (selected.putIfAbsent(
                        lineId, quantity) != null) {

                    throw new BusinessException(
                            "La stessa riga non può "
                                    + "essere selezionata due volte.");
                }
            }

            Long noteId = draftService.createPartialDraft(
                    tenantId,
                    customerInvoiceId,
                    selected,
                    reason);

            if (noteId == null || noteId <= 0) {
                throw new IllegalStateException(
                        "Creazione nota parziale senza ID valido.");
            }

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Nota di credito cliente parziale DRAFT "
                            + "creata. ID: " + noteId);


            // PASSO 5.13.2395DY
            // Creazione riuscita: apri il dettaglio CCN.
            return "redirect:/customer-credit-notes/" + noteId;


        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage());
        }

        return redirect;
    }
}
