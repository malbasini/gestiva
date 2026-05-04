package com.gestiva.billing.invoice.controller;

import com.gestiva.billing.invoice.service.InvoicePdfService;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invoices")
public class InvoicePdfController {

    private final InvoicePdfService invoicePdfService;
    private final TenantContext tenantContext;

    public InvoicePdfController(InvoicePdfService invoicePdfService,
                                TenantContext tenantContext) {
        this.invoicePdfService = invoicePdfService;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id,
                                              @RequestParam(required = false) Long tenantId) {

        Long resolvedTenantId = tenantId != null ? tenantId : tenantContext.getCurrentTenantId();

        byte[] pdf = invoicePdfService.generatePdf(resolvedTenantId, id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("invoice-" + id + ".pdf")
                        .build()
        );

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }

    @GetMapping("/{id}/preview")
    public ResponseEntity<byte[]> previewPdf(@PathVariable Long id,
                                             @RequestParam(required = false) Long tenantId) {
        Long resolvedTenantId = tenantId != null ? tenantId : tenantContext.getCurrentTenantId();
        byte[] pdf = invoicePdfService.generatePdf(resolvedTenantId, id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.inline()
                        .filename("invoice-" + id + ".pdf")
                        .build()
        );

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }
}