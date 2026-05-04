package com.gestiva.billing.invoice.service;

import com.gestiva.billing.invoice.web.InvoiceDocumentWebService;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;

@Service
public class InvoicePdfService {

    private final InvoiceDocumentWebService invoiceDocumentWebService;
    private final TemplateEngine templateEngine;

    public InvoicePdfService(InvoiceDocumentWebService invoiceDocumentWebService,
                             TemplateEngine templateEngine) {
        this.invoiceDocumentWebService = invoiceDocumentWebService;
        this.templateEngine = templateEngine;
    }

    public byte[] generatePdf(Long tenantId, Long invoiceId) {
        var invoice = invoiceDocumentWebService.getDocument(tenantId, invoiceId);

        Context context = new Context();
        context.setVariable("invoice", invoice);

        String html = templateEngine.process("pdf/invoice-pdf", context);

        // Qui usa lo stesso identico meccanismo che già utilizzi per preventivi/ordini/DDT
        return getBytes(html);
    }

    public static byte[] getBytes(String html) {
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