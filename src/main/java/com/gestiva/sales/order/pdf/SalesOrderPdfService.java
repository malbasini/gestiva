package com.gestiva.sales.order.pdf;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.io.ByteArrayOutputStream;
import java.util.Locale;

@Service
public class SalesOrderPdfService {

    private final SalesOrderPdfDataService salesOrderPdfDataService;
    private final TemplateEngine templateEngine;

    public SalesOrderPdfService(SalesOrderPdfDataService salesOrderPdfDataService,
                                TemplateEngine templateEngine) {
        this.salesOrderPdfDataService = salesOrderPdfDataService;
        this.templateEngine = templateEngine;
    }

    public byte[] generateSalesOrderPdf(Long tenantId, Long orderId) {
        SalesOrderPdfView view = salesOrderPdfDataService.buildView(tenantId, orderId);

        Context context = new Context(Locale.ITALIAN);
        context.setVariable("order", view);

        String html = templateEngine.process("pdf/sales-order-pdf", context);

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Errore durante la generazione del PDF dell'ordine", ex);
        }
    }
}