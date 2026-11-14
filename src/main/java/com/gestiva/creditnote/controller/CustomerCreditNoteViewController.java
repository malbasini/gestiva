
package com.gestiva.creditnote.controller;

import com.gestiva.creditnote.service.CustomerCreditNoteDetailService;
import com.gestiva.creditnote.service.CustomerCreditNoteCompensationViewService;
import com.gestiva.security.usercontext.TenantContext;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * GESTIVA V2 - PASSO 5.13.2395DC
 *
 * Consultazione dettaglio nota di credito cliente.
 *
 * Nessuna scrittura su database.
 * Nessuna registrazione contabile.
 *
 * Isolamento tenant/company demandato al servizio
 * CustomerCreditNoteDetailService.
 */
@Controller
@RequestMapping("/customer-credit-notes")
public class CustomerCreditNoteViewController {

    private final TenantContext tenantContext;

    private final CustomerCreditNoteDetailService detailService;
    private final CustomerCreditNoteCompensationViewService compensationViewService;

    public CustomerCreditNoteViewController(
            TenantContext tenantContext,
            CustomerCreditNoteDetailService detailService,
            CustomerCreditNoteCompensationViewService compensationViewService) {

        this.tenantContext = tenantContext;
        this.detailService = detailService;
        this.compensationViewService = compensationViewService;
    }

    @GetMapping("/{id}")
    public String detail(
            @PathVariable Long id,
            Authentication authentication,
            Model model) {

        Long tenantId = tenantContext.getCurrentTenantId();

        var detail = detailService.getDetail(
                tenantId,
                id
        );

        var note = detail.note();

        boolean authorizedToRegister =
                authentication != null
                        && authentication.isAuthenticated()
                        && authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                "ROLE_ADMIN".equals(
                                        authority.getAuthority()
                                )
                                        || "ROLE_ACCOUNTING".equals(
                                        authority.getAuthority()
                                )
                        );

        boolean canRegister =
                authorizedToRegister
                        && "DRAFT".equals(note.getStatus());

        // GESTIVA V2 - PASSO 5.13.2395KS
        // Dati di sola lettura per la compensazione clienti V11.
        // Visibili esclusivamente a ADMIN/ACCOUNTING su note REGISTERED.
        boolean canCompensate = false;

        if (authorizedToRegister
                && "REGISTERED".equals(note.getStatus())) {

            var compensationView = compensationViewService.getView(
                    tenantId,
                    id
            );

            model.addAttribute(
                    "compensationPosition",
                    compensationView.position()
            );
            model.addAttribute(
                    "compensableDues",
                    compensationView.eligibleDues()
            );

            canCompensate = compensationView.compensable();

            if (canCompensate) {
                model.addAttribute(
                        "compensationOperationKey",
                        java.util.UUID.randomUUID().toString()
                );
                model.addAttribute(
                        "compensationDate",
                        java.time.LocalDate.now()
                );
            }
        }

        model.addAttribute(
                "canCompensateCreditNote",
                canCompensate
        );

        model.addAttribute(
                "creditNote",
                note
        );

        model.addAttribute(
                "creditNoteLines",
                detail.lines()
        );

        model.addAttribute(
                "canRegisterCreditNote",
                canRegister
        );

        model.addAttribute(
                "activeMenu",
                "invoices"
        );

        return "creditnote/customer-credit-note-detail";
    }
}
