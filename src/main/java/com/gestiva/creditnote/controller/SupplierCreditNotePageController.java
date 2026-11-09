package com.gestiva.creditnote.controller;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.creditnote.service.SupplierCreditNoteDraftService;
import com.gestiva.creditnote.service.SupplierCreditNoteRegistrationService;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.security.usercontext.TenantContext;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller HTTP per le note di credito fornitore.
 *
 * Le autorizzazioni per ruolo sono definite
 * in SecurityConfig.
 *
 * Il tenant proviene dall'utente autenticato.
 * La societa' attiva viene validata da CompanyContext.
 *
 * Non accetta tenantId o companyId dal browser.
 */
@Controller
@RequestMapping("/supplier-credit-notes")
public class SupplierCreditNotePageController {

    private final TenantContext tenantContext;
    private final CompanyContext companyContext;
    private final SupplierCreditNoteDraftService draftService;
    private final SupplierCreditNoteRegistrationService registrationService;

    public SupplierCreditNotePageController(
            TenantContext tenantContext,
            CompanyContext companyContext,
            SupplierCreditNoteDraftService draftService,
            SupplierCreditNoteRegistrationService registrationService) {

        this.tenantContext = tenantContext;
        this.companyContext = companyContext;
        this.draftService = draftService;
        this.registrationService = registrationService;
    }

    /**
     * Crea una nota di credito completa in stato DRAFT.
     *
     * POST /supplier-credit-notes
     */
    @PostMapping
    public String createFullDraft(
            @RequestParam("supplierInvoiceId") Long supplierInvoiceId,
            @RequestParam("reason") String reason,
            RedirectAttributes redirectAttributes) {

        Long tenantId = tenantContext.getCurrentTenantId();

        // Verifica accesso alla societa' attiva.
        // Le eccezioni di autorizzazione non vengono intercettate.
        companyContext.requireCurrentCompanyId();

        if (supplierInvoiceId == null || supplierInvoiceId <= 0) {
            throw new BusinessException(
                    "Identificativo fattura fornitore non valido.");
        }

        String redirect =
                "redirect:/supplier-invoices/" + supplierInvoiceId;

        try {
            Long creditNoteId = draftService.createFullDraft(
                    tenantId,
                    supplierInvoiceId,
                    reason
            );

            if (creditNoteId == null || creditNoteId <= 0) {
                throw new IllegalStateException(
                        "Creazione nota di credito senza ID valido.");
            }

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Nota di credito fornitore DRAFT creata. "
                            + "ID: " + creditNoteId
            );

        } catch (BusinessException ex) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage()
            );
        }

        return redirect;
    }

    /**
     * Registra e contabilizza una nota di credito DRAFT.
     *
     * POST /supplier-credit-notes/{id}/register
     */
    @PostMapping("/{id}/register")
    public String register(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        Long tenantId = tenantContext.getCurrentTenantId();

        // La societa' deve essere attiva e accessibile.
        companyContext.requireCurrentCompanyId();

        if (id == null || id <= 0) {
            throw new BusinessException(
                    "Identificativo nota di credito non valido.");
        }

        try {
            var result = registrationService.register(
                    tenantId,
                    id
            );

            if (result == null) {
                throw new IllegalStateException(
                        "Registrazione SCN senza risultato.");
            }

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Nota di credito fornitore registrata "
                            + "e contabilizzata correttamente."
            );

        } catch (BusinessException ex) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage()
            );
        }

        // Il dettaglio SCN sara' introdotto con
        // la successiva integrazione Thymeleaf.
        return "redirect:/supplier-invoices";
    }
}
