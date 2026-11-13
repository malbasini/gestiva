
package com.gestiva.creditnote.controller;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.creditnote.service.CustomerCreditNoteRegistrationService;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.security.usercontext.TenantContext;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * GESTIVA V2 - PASSO 5.13.2395DN
 *
 * Endpoint HTTP per la registrazione contabile
 * delle note di credito cliente.
 *
 * Autorizzazioni:
 * ADMIN / ACCOUNTING tramite SecurityConfig.
 *
 * Tenant: TenantContext autenticato.
 * Company: CompanyContext attivo.
 *
 * Le operazioni contabili rimangono interamente
 * nel CustomerCreditNoteRegistrationService.
 */
@Controller
@RequestMapping("/customer-credit-notes")
public class CustomerCreditNoteRegistrationPageController {

    private final TenantContext tenantContext;
    private final CompanyContext companyContext;
    private final CustomerCreditNoteRegistrationService registrationService;

    public CustomerCreditNoteRegistrationPageController(
            TenantContext tenantContext,
            CompanyContext companyContext,
            CustomerCreditNoteRegistrationService registrationService) {

        this.tenantContext = tenantContext;
        this.companyContext = companyContext;
        this.registrationService = registrationService;
    }

    /**
     * POST /customer-credit-notes/{id}/register
     *
     * Registra una nota CUSTOMER in stato DRAFT.
     */
    @PostMapping("/{id}/register")
    public String register(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        Long tenantId = tenantContext.getCurrentTenantId();

        // La verifica della società resta fuori dal catch:
        // eventuali eccezioni di autorizzazione
        // devono propagarsi.
        companyContext.requireCurrentCompanyId();

        if (tenantId == null || tenantId <= 0) {
            throw new BusinessException(
                    "Tenant autenticato non valido.");
        }

        if (id == null || id <= 0) {
            throw new BusinessException(
                    "Identificativo nota cliente non valido.");
        }

        String redirect =
                "redirect:/customer-credit-notes/" + id;

        try {

            var result = registrationService.register(
                    tenantId,
                    id
            );

            if (result == null
                    || result.creditNoteId() == null
                    || !id.equals(result.creditNoteId())
                    || result.journalEntryId() == null
                    || result.financialPositionId() == null) {

                throw new IllegalStateException(
                        "Registrazione CCN senza risultato valido.");
            }

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Nota di credito cliente registrata "
                            + "e contabilizzata correttamente."
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
