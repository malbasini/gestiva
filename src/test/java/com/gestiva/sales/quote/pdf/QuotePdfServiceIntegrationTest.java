package com.gestiva.sales.quote.pdf;

import com.gestiva.crm.contact.entity.Customer;
import com.gestiva.platform.tenant.entity.Tenant;
import com.gestiva.sales.quote.dto.QuoteCreateRequest;
import com.gestiva.sales.quote.dto.QuoteLineRequest;
import com.gestiva.sales.quote.dto.QuoteResponse;
import com.gestiva.sales.quote.service.QuoteService;
import com.gestiva.support.AbstractMySqlIntegrationTest;
import com.gestiva.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QuotePdfServiceIntegrationTest extends AbstractMySqlIntegrationTest {

    @Autowired
    private QuoteService quoteService;

    @Autowired
    private QuotePdfService quotePdfService;

    @Autowired
    private TestDataFactory testDataFactory;

    private Long tenantId;
    private Long customerId;

    @BeforeEach
    void setUp() {
        Tenant tenant = testDataFactory.createTenant();
        Customer customer = testDataFactory.createCustomer(tenant.getId(), "Rossi Srl");
        this.tenantId = tenant.getId();
        this.customerId = customer.getId();
    }

    @Test
    void generateQuotePdf_shouldReturnNonEmptyPdf() {
        QuoteCreateRequest request = new QuoteCreateRequest();
        request.setCustomerId(customerId);
        request.setQuoteDate(LocalDate.of(2026, 4, 26));
        request.setValidUntil(LocalDate.of(2026, 5, 26));
        request.setCurrencyCode("EUR");
        request.setNotes("Preventivo PDF test");

        QuoteLineRequest line = new QuoteLineRequest();
        line.setDescription("Modulo CRM");
        line.setQuantity(new BigDecimal("1.000"));
        line.setUnitPrice(new BigDecimal("1000.00"));
        line.setDiscountPct(new BigDecimal("10.00"));
        line.setTaxPct(new BigDecimal("22.00"));

        request.setLines(List.of(line));

        QuoteResponse quote = quoteService.create(tenantId, request);

        byte[] pdf = quotePdfService.generateQuotePdf(tenantId, quote.getId());

        assertNotNull(pdf);
        assertTrue(pdf.length > 100);

        String header = new String(pdf, 0, 4, StandardCharsets.US_ASCII);
        assertEquals("%PDF", header);
    }
}