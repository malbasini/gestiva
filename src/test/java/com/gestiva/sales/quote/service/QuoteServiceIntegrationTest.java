package com.gestiva.sales.quote.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.crm.contact.entity.Customer;
import com.gestiva.platform.tenant.entity.Tenant;
import com.gestiva.sales.quote.dto.QuoteCreateRequest;
import com.gestiva.sales.quote.dto.QuoteLineRequest;
import com.gestiva.sales.quote.dto.QuoteResponse;
import com.gestiva.sales.quote.dto.QuoteUpdateRequest;
import com.gestiva.support.AbstractMySqlIntegrationTest;
import com.gestiva.support.DatabaseCleaner;
import com.gestiva.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QuoteServiceIntegrationTest extends AbstractMySqlIntegrationTest {

    @Autowired
    private QuoteService quoteService;

    @Autowired
    private TestDataFactory testDataFactory;

    private Long tenantId;
    private Long customerId;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void cleanAndSetUp() {
        databaseCleaner.clean();
        Tenant tenant = testDataFactory.createTenant();
        Customer customer = testDataFactory.createCustomer(tenant.getId(), "Rossi Srl");
        this.tenantId = tenant.getId();
        this.customerId = customer.getId();
    }

    @Test
    void create_shouldCalculateSubtotalTaxAndTotal_andSaveLines() {
        QuoteCreateRequest request = new QuoteCreateRequest();
        request.setCustomerId(customerId);
        request.setQuoteDate(LocalDate.of(2026, 4, 26));
        request.setValidUntil(LocalDate.of(2026, 5, 26));
        request.setCurrencyCode("EUR");
        request.setNotes("Preventivo test");

        QuoteLineRequest line1 = new QuoteLineRequest();
        line1.setDescription("Modulo CRM");
        line1.setQuantity(new BigDecimal("1.000"));
        line1.setUnitPrice(new BigDecimal("1000.00"));
        line1.setDiscountPct(new BigDecimal("10.00"));
        line1.setTaxPct(new BigDecimal("22.00"));

        QuoteLineRequest line2 = new QuoteLineRequest();
        line2.setDescription("Setup");
        line2.setQuantity(new BigDecimal("2.000"));
        line2.setUnitPrice(new BigDecimal("100.00"));
        line2.setDiscountPct(new BigDecimal("0.00"));
        line2.setTaxPct(new BigDecimal("22.00"));

        request.setLines(List.of(line1, line2));

        QuoteResponse response = quoteService.create(tenantId, request);

        assertNotNull(response.getId());
        assertNotNull(response.getQuoteNumber());
        assertEquals("DRAFT", response.getStatus());
        assertEquals("EUR", response.getCurrencyCode());

        // line1 = 1000 - 10% = 900
        // line2 = 2 * 100 = 200
        // subtotal = 1100
        // tax = 242
        // total = 1342
        assertEquals(new BigDecimal("1100.00"), response.getSubtotalAmount());
        assertEquals(new BigDecimal("242.00"), response.getTaxAmount());
        assertEquals(new BigDecimal("1342.00"), response.getTotalAmount());

        assertEquals(2, response.getLines().size());
        assertEquals(1, response.getLines().get(0).getLineNo());
        assertEquals(2, response.getLines().get(1).getLineNo());
        assertEquals(new BigDecimal("900.00"), response.getLines().get(0).getLineTotal());
        assertEquals(new BigDecimal("200.00"), response.getLines().get(1).getLineTotal());
    }

    @Test
    void create_shouldThrowBusinessException_whenLinesAreEmpty() {
        QuoteCreateRequest request = new QuoteCreateRequest();
        request.setCustomerId(customerId);
        request.setQuoteDate(LocalDate.of(2026, 4, 26));
        request.setValidUntil(LocalDate.of(2026, 5, 26));
        request.setCurrencyCode("EUR");
        request.setLines(List.of());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> quoteService.create(tenantId, request));

        assertTrue(ex.getMessage().contains("almeno una riga"));
    }

    @Test
    void update_shouldReplaceLinesAndRecalculateTotals() {
        QuoteCreateRequest createRequest = new QuoteCreateRequest();
        createRequest.setCustomerId(customerId);
        createRequest.setQuoteDate(LocalDate.of(2026, 4, 26));
        createRequest.setValidUntil(LocalDate.of(2026, 5, 26));
        createRequest.setCurrencyCode("EUR");

        QuoteLineRequest originalLine = new QuoteLineRequest();
        originalLine.setDescription("Servizio base");
        originalLine.setQuantity(new BigDecimal("1.000"));
        originalLine.setUnitPrice(new BigDecimal("500.00"));
        originalLine.setDiscountPct(new BigDecimal("0.00"));
        originalLine.setTaxPct(new BigDecimal("22.00"));

        createRequest.setLines(List.of(originalLine));

        QuoteResponse created = quoteService.create(tenantId, createRequest);

        QuoteUpdateRequest updateRequest = new QuoteUpdateRequest();
        updateRequest.setCustomerId(customerId);
        updateRequest.setQuoteDate(LocalDate.of(2026, 4, 27));
        updateRequest.setValidUntil(LocalDate.of(2026, 5, 31));
        updateRequest.setStatus("SENT");
        updateRequest.setCurrencyCode("EUR");
        updateRequest.setNotes("Aggiornato");

        QuoteLineRequest updatedLine1 = new QuoteLineRequest();
        updatedLine1.setDescription("Servizio premium");
        updatedLine1.setQuantity(new BigDecimal("2.000"));
        updatedLine1.setUnitPrice(new BigDecimal("300.00"));
        updatedLine1.setDiscountPct(new BigDecimal("5.00"));
        updatedLine1.setTaxPct(new BigDecimal("22.00"));

        QuoteLineRequest updatedLine2 = new QuoteLineRequest();
        updatedLine2.setDescription("Formazione");
        updatedLine2.setQuantity(new BigDecimal("1.000"));
        updatedLine2.setUnitPrice(new BigDecimal("100.00"));
        updatedLine2.setDiscountPct(new BigDecimal("0.00"));
        updatedLine2.setTaxPct(new BigDecimal("22.00"));

        updateRequest.setLines(List.of(updatedLine1, updatedLine2));

        QuoteResponse updated = quoteService.update(tenantId, created.getId(), updateRequest);

        // line1 = 600 - 5% = 570
        // line2 = 100
        // subtotal = 670
        // tax = 147.40
        // total = 817.40
        assertEquals("SENT", updated.getStatus());
        assertEquals(new BigDecimal("670.00"), updated.getSubtotalAmount());
        assertEquals(new BigDecimal("147.40"), updated.getTaxAmount());
        assertEquals(new BigDecimal("817.40"), updated.getTotalAmount());
        assertEquals(2, updated.getLines().size());
    }
}