package com.gestiva.logistics.ddt.service;

import com.gestiva.logistics.ddt.web.DeliveryNoteDocumentWebService;
import com.gestiva.logistics.ddt.web.DeliveryNoteDocumentView;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;

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

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Errore durante la generazione del PDF DDT", ex);
        }
    }
}