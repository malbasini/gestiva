package com.gestiva.sales.order.pdf;

import com.gestiva.crm.contact.entity.Customer;
import com.gestiva.platform.tenant.entity.Tenant;
import com.gestiva.sales.order.dto.SalesOrderResponse;
import com.gestiva.sales.order.service.SalesOrderService;
import com.gestiva.sales.quote.dto.QuoteCreateRequest;
import com.gestiva.sales.quote.dto.QuoteLineRequest;
import com.gestiva.sales.quote.dto.QuoteResponse;
import com.gestiva.sales.quote.dto.QuoteUpdateRequest;
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

class SalesOrderPdfServiceIntegrationTest extends AbstractMySqlIntegrationTest {

    @Autowired
    private QuoteService quoteService;

    @Autowired
    private SalesOrderService salesOrderService;

    @Autowired
    private SalesOrderPdfService salesOrderPdfService;

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
    void generateSalesOrderPdf_shouldReturnNonEmptyPdf() {
        QuoteResponse quote = createSentQuote();
        SalesOrderResponse order = salesOrderService.convertFromQuote(tenantId, quote.getId());

        byte[] pdf = salesOrderPdfService.generateSalesOrderPdf(tenantId, order.getId());

        assertNotNull(pdf);
        assertTrue(pdf.length > 100);

        String header = new String(pdf, 0, 4, StandardCharsets.US_ASCII);
        assertEquals("%PDF", header);
    }

    private QuoteResponse createSentQuote() {
        QuoteCreateRequest request = new QuoteCreateRequest();
        request.setCustomerId(customerId);
        request.setQuoteDate(LocalDate.of(2026, 4, 28));
        request.setValidUntil(LocalDate.of(2026, 5, 28));
        request.setCurrencyCode("EUR");

        QuoteLineRequest line1 = new QuoteLineRequest();
        line1.setDescription("Modulo CRM");
        line1.setQuantity(new BigDecimal("1.000"));
        line1.setUnitPrice(new BigDecimal("1000.00"));
        line1.setDiscountPct(new BigDecimal("10.00"));
        line1.setTaxPct(new BigDecimal("22.00"));

        QuoteLineRequest line2 = new QuoteLineRequest();
        line2.setDescription("Setup");
        line2.setQuantity(new BigDecimal("1.000"));
        line2.setUnitPrice(new BigDecimal("200.00"));
        line2.setDiscountPct(new BigDecimal("0.00"));
        line2.setTaxPct(new BigDecimal("22.00"));

        request.setLines(List.of(line1, line2));

        QuoteResponse created = quoteService.create(tenantId, request);

        QuoteUpdateRequest update = new QuoteUpdateRequest();
        update.setCustomerId(customerId);
        update.setQuoteDate(created.getQuoteDate());
        update.setValidUntil(created.getValidUntil());
        update.setStatus("SENT");
        update.setCurrencyCode(created.getCurrencyCode());
        update.setLines(List.of(line1, line2));

        return quoteService.update(tenantId, created.getId(), update);
    }
}