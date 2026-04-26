package com.gestiva.sales.quote.pdf;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.io.ByteArrayOutputStream;
import java.util.Locale;

@Service
public class QuotePdfService {

    private final QuotePdfDataService quotePdfDataService;
    private final TemplateEngine templateEngine;

    public QuotePdfService(QuotePdfDataService quotePdfDataService,
                           TemplateEngine templateEngine) {
        this.quotePdfDataService = quotePdfDataService;
        this.templateEngine = templateEngine;
    }

    public byte[] generateQuotePdf(Long tenantId, Long quoteId) {
        QuotePdfView view = quotePdfDataService.buildView(tenantId, quoteId);

        Context context = new Context(Locale.ITALIAN);
        context.setVariable("quote", view);

        String html = templateEngine.process("pdf/quote-pdf", context);

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Errore durante la generazione del PDF del preventivo", ex);
        }
    }
}