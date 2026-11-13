
package com.gestiva.billing.invoice.controller;

import com.gestiva.billing.invoice.web.InvoiceDetailWebService;
import com.gestiva.creditnote.service.CustomerCreditNoteDetailService;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/invoices")
public class InvoiceDetailPageController {

    private final InvoiceDetailWebService invoiceDetailWebService;
    private final CustomerCreditNoteDetailService creditNoteDetailService;
    private final TenantContext tenantContext;

    public InvoiceDetailPageController(
            InvoiceDetailWebService invoiceDetailWebService,
            CustomerCreditNoteDetailService creditNoteDetailService,
            TenantContext tenantContext) {

        this.invoiceDetailWebService = invoiceDetailWebService;
        this.creditNoteDetailService = creditNoteDetailService;
        this.tenantContext = tenantContext;
    }

    /**
     * GESTIVA V2 - PASSO 5.13.2395BN
     *
     * Dettaglio fattura cliente con note di credito associate.
     *
     * Il tenant viene ricavato dal contesto autenticato.
     * Il servizio CCN verifica la società attiva.
     *
     * Nessuna operazione di scrittura.
     */
    @GetMapping("/{id}")
    public String detail(
            @PathVariable Long id,
            @RequestParam(required = false) Long tenantId,
            Model model,
            Authentication authentication) {

        Long resolvedTenantId =
                tenantContext.getCurrentTenantId();

        // Recupera la fattura verificando tenant/company.
        var invoice = invoiceDetailWebService.getDetail(
                resolvedTenantId, id);

        model.addAttribute("invoice", invoice);
        model.addAttribute("tenantId", resolvedTenantId);
        model.addAttribute("activeMenu", "invoices");

        // Recupera esclusivamente note CUSTOMER
        // appartenenti alla fattura e alla società attiva.
        model.addAttribute(
                "customerCreditNotes",
                creditNoteDetailService
                        .findCustomerNotesForInvoice(
                                resolvedTenantId, id)
        );

        // I ruoli di creazione coincidono con quelli
        // autorizzati in SecurityConfig.
        boolean authorized =
                authentication != null
                        && authentication.isAuthenticated()
                        && authentication.getAuthorities().stream()
                        .anyMatch(authority ->
                                "ROLE_ADMIN".equals(
                                        authority.getAuthority())
                                        || "ROLE_SALES".equals(
                                        authority.getAuthority())
                                        || "ROLE_ACCOUNTING".equals(
                                        authority.getAuthority()));

        boolean canCreateCustomerCreditNote =
                authorized
                        && "ISSUED".equals(invoice.getStatus());

        model.addAttribute(
                "canCreateCustomerCreditNote",
                canCreateCustomerCreditNote);

        return "invoice/invoice-detail";
    }
}
