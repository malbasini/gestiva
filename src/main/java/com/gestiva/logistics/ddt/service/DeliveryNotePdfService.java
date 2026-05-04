package com.gestiva.logistics.ddt.service;

import com.gestiva.logistics.ddt.web.DeliveryNoteDocumentView;
import com.gestiva.logistics.ddt.web.DeliveryNoteDocumentWebService;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import static com.gestiva.billing.invoice.service.InvoicePdfService.getBytes;

@Service
public class DeliveryNotePdfService {

    private final DeliveryNoteDocumentWebService deliveryNoteDocumentWebService;
    private final TemplateEngine templateEngine;

    public DeliveryNotePdfService(DeliveryNoteDocumentWebService deliveryNoteDocumentWebService,
                                  TemplateEngine templateEngine) {
        this.deliveryNoteDocumentWebService = deliveryNoteDocumentWebService;
        this.templateEngine = templateEngine;
    }

    public byte[] generatePdf(Long tenantId, Long deliveryNoteId) {
        DeliveryNoteDocumentView deliveryNote =
                deliveryNoteDocumentWebService.getDocument(tenantId, deliveryNoteId);

        Context context = new Context();
        context.setVariable("deliveryNote", deliveryNote);

        String html = templateEngine.process("pdf/delivery-note-pdf", context);

        return getBytes(html);
    }
}