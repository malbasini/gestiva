package com.gestiva.purchasing.invoice.controller;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.purchasing.invoice.service.SupplierInvoicePartialApplicationService;
import com.gestiva.security.usercontext.TenantContext;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Punto di ingresso HTTP per la futura fatturazione
 * dei ricevimenti parziali.
 *
 * La route è protetta in SecurityConfig:
 * ADMIN e PURCHASING, POST con CSRF.
 *
 * Non modifica il controller legacy.
 * Non introduce registrazioni contabili.
 *
 * Il servizio applicativo mantiene il feature gate:
 * la presenza della route non abilita la funzionalità.
 */
@Controller
@RequestMapping("/goods-receipts")
public class SupplierInvoicePartialPageController {

    private final SupplierInvoicePartialApplicationService service;
    private final TenantContext tenantContext;

    public SupplierInvoicePartialPageController(
            SupplierInvoicePartialApplicationService service,
            TenantContext tenantContext) {

        this.service = service;
        this.tenantContext = tenantContext;
    }

    @PostMapping("/{id}/invoice-partial")
    public String createPartialInvoice(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        String redirect = "redirect:/goods-receipts/" + id;

        try {
            Long tenantId = tenantContext.getCurrentTenantId();

            Long invoiceId = service.create(tenantId, id);

            if (invoiceId == null || invoiceId <= 0) {
                throw new IllegalStateException(
                        "Creazione fattura senza identificativo valido."
                );
            }

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Fattura fornitore parziale DRAFT creata con successo."
            );

            return redirect;

        } catch (BusinessException ex) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage()
            );

            return redirect;
        }
    }
}
