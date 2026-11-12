
package com.gestiva.creditnote.controller;

import com.gestiva.creditnote.service.SupplierCreditNoteDetailService;
import com.gestiva.creditnote.service.SupplierCreditNoteCompensationViewService;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/supplier-credit-notes")
public class SupplierCreditNoteViewController {

    private final TenantContext tenantContext;
    private final SupplierCreditNoteDetailService detailService;
    private final SupplierCreditNoteCompensationViewService compensationViewService;

    public SupplierCreditNoteViewController(
            TenantContext tenantContext,
            SupplierCreditNoteDetailService detailService,
            SupplierCreditNoteCompensationViewService
                    compensationViewService) {

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
        var detail = detailService.getDetail(tenantId, id);
        var note = detail.note();
        boolean authorized = authentication != null
                && authentication.isAuthenticated()
                && authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        "ROLE_ADMIN".equals(
                                authority.getAuthority())
                                || "ROLE_ACCOUNTING".equals(
                                authority.getAuthority()));
        boolean canRegister = authorized
                && "DRAFT".equals(note.getStatus());
        boolean registered =
                "REGISTERED".equals(note.getStatus());
        model.addAttribute("creditNote", note);
        model.addAttribute("creditNoteLines", detail.lines());
        model.addAttribute(
                "canRegisterCreditNote", canRegister);

        // GESTIVA V2 - PASSO 5.13.2392K
        // Lettura dati per compensazione V10.
        // Nessuna modifica finanziaria viene effettuata qui.

        boolean canCompensate = false;
        if (authorized && registered) {
            var compensationView =
                    compensationViewService.getView(
                            tenantId, id);
            model.addAttribute(
                    "compensationPosition",
                    compensationView.position());
            model.addAttribute(
                    "compensableDues",
                    compensationView.eligibleDues());
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
                "canCompensateCreditNote", canCompensate);
        model.addAttribute(
                "activeMenu", "supplierInvoices");
        return "creditnote/supplier-credit-note-detail";
    }
}
