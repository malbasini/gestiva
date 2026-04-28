package com.gestiva.sales.order.controller;

import com.gestiva.sales.order.pdf.SalesOrderPdfService;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class SalesOrderPdfController {

    private final SalesOrderPdfService salesOrderPdfService;
    private final TenantContext tenantContext;

    public SalesOrderPdfController(SalesOrderPdfService salesOrderPdfService,
                                   TenantContext tenantContext) {
        this.salesOrderPdfService = salesOrderPdfService;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id,
                                              @RequestParam(required = false) Long tenantId) {
        Long resolvedTenantId = tenantId != null ? tenantId : tenantContext.getCurrentTenantId();
        byte[] pdf = salesOrderPdfService.generateSalesOrderPdf(resolvedTenantId, id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("ordine-" + id + ".pdf")
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
        byte[] pdf = salesOrderPdfService.generateSalesOrderPdf(resolvedTenantId, id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.inline()
                        .filename("ordine-" + id + ".pdf")
                        .build()
        );

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }
}