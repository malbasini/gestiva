package com.gestiva.creditnote.controller;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.creditnote.repository.CreditNoteFinancialPositionRepository;
import com.gestiva.creditnote.service.SupplierCreditNoteCompensationService;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.security.usercontext.TenantContext;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/supplier-credit-notes")
public class SupplierCreditNoteCompensationPageController {

    private final TenantContext tenantContext;
    private final CompanyContext companyContext;
    private final CreditNoteFinancialPositionRepository positions;
    private final SupplierCreditNoteCompensationService compensationService;

    public SupplierCreditNoteCompensationPageController(
            TenantContext tenantContext,
            CompanyContext companyContext,
            CreditNoteFinancialPositionRepository positions,
            SupplierCreditNoteCompensationService compensationService) {

        this.tenantContext = tenantContext;
        this.companyContext = companyContext;
        this.positions = positions;
        this.compensationService = compensationService;
    }

    // GESTIVA V2 - PASSO 5.13.2392M
    // Endpoint POST per compensazioni note di credito fornitore.
    //
    // Tenant e company provengono esclusivamente
    // dal contesto autenticato.
    //
    // Il positionId non viene accettato dal browser.
    // La posizione viene recuperata tramite la nota.
    //
    // Il servizio transazionale gestisce:
    // lock, idempotenza, importi, fornitore, valuta e quadrature.

    @PostMapping("/{id}/compensations")
    public String compensate(
            @PathVariable Long id,
            @RequestParam("paymentDueId") Long paymentDueId,
            @RequestParam("amount") BigDecimal amount,
            @RequestParam("operationKey") String operationKey,
            @RequestParam("compensationDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate compensationDate,
            @RequestParam(
                    name = "notes",
                    required = false
            ) String notes,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        boolean authorized = authentication != null
                && authentication.isAuthenticated()
                && authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        "ROLE_ADMIN".equals(
                                authority.getAuthority())
                                || "ROLE_ACCOUNTING".equals(
                                authority.getAuthority()));
        if (!authorized) {
            throw new AccessDeniedException(
                    "Operazione di compensazione non autorizzata.");
        }

        Long tenantId = tenantContext.getCurrentTenantId();
        Long companyId = companyContext.requireCurrentCompanyId();

        if (tenantId == null || tenantId <= 0
                || companyId == null || companyId <= 0) {
            throw new AccessDeniedException(
                    "Contesto aziendale non valido.");
        }
        if (id == null || id <= 0) {
            throw new BusinessException(
                    "Identificativo nota di credito non valido.");
        }
        String redirect =
                "redirect:/supplier-credit-notes/" + id;
        try {
            if (paymentDueId == null || paymentDueId <= 0) {
                throw new BusinessException(
                        "Selezionare una scadenza valida.");
            }
            if (amount == null
                    || amount.signum() <= 0
                    || amount.scale() > 2) {
                throw new BusinessException(
                        "Importo compensazione non valido.");
            }
            if (compensationDate == null) {
                throw new BusinessException(
                        "Data compensazione obbligatoria.");
            }
            if (operationKey == null
                    || operationKey.isBlank()
                    || operationKey.length() > 100) {
                throw new BusinessException(
                        "Chiave operazione non valida.");
            }

            // Recupero server-side della posizione:
            // nessun positionId fornito dall'utente.

            var position = positions
                    .findByTenantIdAndCompanyIdAndCreditNoteId(
                            tenantId,
                            companyId,
                            id
                    )
                    .orElseThrow(() -> new BusinessException(
                            "Posizione finanziaria della nota "
                                    + "non trovata nella societa."));
            Long compensationId = compensationService.compensate(
                    tenantId,
                    position.getId(),
                    paymentDueId,
                    amount,
                    compensationDate,
                    operationKey,
                    notes
            );
            if (compensationId == null || compensationId <= 0) {
                throw new IllegalStateException(
                        "Compensazione senza identificativo valido.");
            }
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Compensazione registrata correttamente. ID: "
                            + compensationId
            );
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage()
            );
        }
        return redirect;
    }
}
