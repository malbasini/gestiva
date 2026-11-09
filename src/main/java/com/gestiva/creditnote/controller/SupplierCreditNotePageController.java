package com.gestiva.creditnote.controller;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.creditnote.service.SupplierCreditNoteDraftService;
import com.gestiva.creditnote.service.SupplierCreditNoteRegistrationService;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.security.usercontext.TenantContext;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

            return "redirect:/supplier-credit-notes/" + creditNoteId;

        } catch (BusinessException ex) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage()
            );
        }

        return redirect;
    }

    /**
     * Crea una nota di credito fornitore parziale in DRAFT.
     *
     * Le liste lineIds e quantities devono essere allineate.
     * Ogni identificativo riga deve essere univoco.
     *
     * Il servizio applicativo verifica la fattura,
     * l'appartenenza delle righe, i limiti cumulativi
     * e gli importi.
     */
    @PostMapping("/partial")
    public String createPartialDraft(
            @RequestParam("supplierInvoiceId") Long supplierInvoiceId,
            @RequestParam("reason") String reason,
            @RequestParam("lineIds") List<Long> lineIds,
            @RequestParam("quantities") List<BigDecimal> quantities,
            RedirectAttributes redirectAttributes) {

        Long tenantId = tenantContext.getCurrentTenantId();

        // Il controllo deve avvenire anche per richieste
        // inviate direttamente, senza passare dalla UI.
        companyContext.requireCurrentCompanyId();

        if (supplierInvoiceId == null || supplierInvoiceId <= 0) {
            throw new BusinessException(
                    "Identificativo fattura fornitore non valido.");
        }

        String invoiceRedirect =
                "redirect:/supplier-invoices/" + supplierInvoiceId;

        try {
            if (reason == null || reason.isBlank()
                    || reason.length() > 500) {
                throw new BusinessException(
                        "Indicare una causale valida (massimo 500 caratteri).");
            }

            if (lineIds == null || quantities == null
                    || lineIds.isEmpty()
                    || lineIds.size() != quantities.size()
                    || lineIds.size() > 500) {
                throw new BusinessException(
                        "Righe o quantita' della nota non valide.");
            }

            Map<Long, BigDecimal> selected = new LinkedHashMap<>();

            for (int index = 0; index < lineIds.size(); index++) {
                Long lineId = lineIds.get(index);
                BigDecimal quantity = quantities.get(index);

                if (lineId == null || lineId <= 0
                        || quantity == null
                        || quantity.signum() <= 0
                        || quantity.scale() > 3) {
                    throw new BusinessException(
                            "Identificativo riga o quantita' non valido.");
                }

                if (selected.putIfAbsent(lineId, quantity) != null) {
                    throw new BusinessException(
                            "La stessa riga non puo' essere selezionata due volte.");
                }
            }

            Long creditNoteId = draftService.createPartialDraft(
                    tenantId,
                    supplierInvoiceId,
                    selected,
                    reason
            );

            if (creditNoteId == null || creditNoteId <= 0) {
                throw new IllegalStateException(
                        "Creazione nota parziale senza ID valido.");
            }

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Nota di credito fornitore parziale DRAFT creata."
            );

            return "redirect:/supplier-credit-notes/" + creditNoteId;

        } catch (BusinessException ex) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage()
            );

            return invoiceRedirect;
        }
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

        // Mantiene l'utente sul dettaglio della nota,
        // sia dopo la registrazione sia in caso di errore.
        return "redirect:/supplier-credit-notes/" + id;
    }
}
