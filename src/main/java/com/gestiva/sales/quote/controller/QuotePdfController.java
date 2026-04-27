package com.gestiva.sales.quote.controller;

import com.gestiva.sales.quote.pdf.QuotePdfService;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quotes")
public class QuotePdfController {

    private final QuotePdfService quotePdfService;
    private final TenantContext tenantContext;

    public QuotePdfController(QuotePdfService quotePdfService,
                              TenantContext tenantContext) {
        this.quotePdfService = quotePdfService;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id,
                                              @RequestParam(required = false) Long tenantId) {
        Long resolvedTenantId = tenantId != null ? tenantId : tenantContext.getCurrentTenantId();
        byte[] pdf = quotePdfService.generateQuotePdf(resolvedTenantId, id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("preventivo-" + id + ".pdf")
                        .build()
        );

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }

    @GetMapping("/{id}/pdf/preview")
    public ResponseEntity<byte[]> previewPdf(@PathVariable Long id,
                                             @RequestParam(required = false) Long tenantId) {
        Long resolvedTenantId = tenantId != null ? tenantId : tenantContext.getCurrentTenantId();
        byte[] pdf = quotePdfService.generateQuotePdf(resolvedTenantId, id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.inline()
                        .filename("preventivo-" + id + ".pdf")
                        .build()
        );

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }
}