package com.gestiva.logistics.ddt.controller;

import com.gestiva.logistics.ddt.service.DeliveryNotePdfService;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/delivery-notes")
public class DeliveryNotePdfController {

    private final DeliveryNotePdfService deliveryNotePdfService;
    private final TenantContext tenantContext;

    public DeliveryNotePdfController(DeliveryNotePdfService deliveryNotePdfService,
                                     TenantContext tenantContext) {
        this.deliveryNotePdfService = deliveryNotePdfService;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id,
                                              @RequestParam(required = false) Long tenantId) {

        Long resolvedTenantId = tenantId != null ? tenantId : tenantContext.getCurrentTenantId();

        byte[] pdf = deliveryNotePdfService.generatePdf(resolvedTenantId, id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("ddt-" + id + ".pdf")
                        .build()
        );

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }
}