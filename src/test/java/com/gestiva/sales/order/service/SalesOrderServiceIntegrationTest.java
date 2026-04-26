package com.gestiva.sales.order.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.crm.contact.entity.Customer;
import com.gestiva.platform.tenant.entity.Tenant;
import com.gestiva.sales.order.dto.SalesOrderResponse;
import com.gestiva.sales.quote.dto.QuoteCreateRequest;
import com.gestiva.sales.quote.dto.QuoteLineRequest;
import com.gestiva.sales.quote.dto.QuoteResponse;
import com.gestiva.sales.quote.dto.QuoteUpdateRequest;
import com.gestiva.sales.quote.service.QuoteService;
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

class SalesOrderServiceIntegrationTest extends AbstractMySqlIntegrationTest {

    @Autowired
    private QuoteService quoteService;

    @Autowired
    private SalesOrderService salesOrderService;

    @Autowired
    private TestDataFactory testDataFactory;

    private Long tenantId;
    private Long customerId;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void setUp() {
        databaseCleaner.clean();
        Tenant tenant = testDataFactory.createTenant();
        Customer customer = testDataFactory.createCustomer(tenant.getId(), "Rossi Srl");
        this.tenantId = tenant.getId();
        this.customerId = customer.getId();
    }

    @Test
    void convertFromQuote_shouldCreateSalesOrderAndCopyLines() {
        QuoteResponse quote = createSentQuote();

        SalesOrderResponse order = salesOrderService.convertFromQuote(tenantId, quote.getId());

        assertNotNull(order.getId());
        assertEquals(quote.getId(), order.getQuoteId());
        assertEquals(customerId, order.getCustomerId());
        assertEquals("CONFIRMED", order.getStatus());
        assertEquals(quote.getTotalAmount(), order.getTotalAmount());
        assertEquals(2, order.getLines().size());
        assertNotNull(order.getOrderNumber());
        assertTrue(order.getOrderNumber().startsWith("ORD-2026-"));
    }

    @Test
    void convertFromQuote_shouldThrowBusinessException_whenQuoteAlreadyConverted() {
        QuoteResponse quote = createSentQuote();

        salesOrderService.convertFromQuote(tenantId, quote.getId());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> salesOrderService.convertFromQuote(tenantId, quote.getId()));

        assertTrue(ex.getMessage().contains("Esiste già un ordine"));
    }

    @Test
    void convertFromQuote_shouldThrowBusinessException_whenQuoteStatusIsNotConvertible() {
        QuoteCreateRequest request = new QuoteCreateRequest();
        request.setCustomerId(customerId);
        request.setQuoteDate(LocalDate.of(2026, 4, 26));
        request.setValidUntil(LocalDate.of(2026, 5, 26));
        request.setCurrencyCode("EUR");

        QuoteLineRequest line = new QuoteLineRequest();
        line.setDescription("Modulo base");
        line.setQuantity(new BigDecimal("1.000"));
        line.setUnitPrice(new BigDecimal("500.00"));
        line.setDiscountPct(new BigDecimal("0.00"));
        line.setTaxPct(new BigDecimal("22.00"));

        request.setLines(List.of(line));

        QuoteResponse quote = quoteService.create(tenantId, request);
        assertEquals("DRAFT", quote.getStatus());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> salesOrderService.convertFromQuote(tenantId, quote.getId()));

        assertTrue(ex.getMessage().contains("non è convertibile"));
    }

    private QuoteResponse createSentQuote() {
        QuoteCreateRequest request = new QuoteCreateRequest();
        request.setCustomerId(customerId);
        request.setQuoteDate(LocalDate.of(2026, 4, 26));
        request.setValidUntil(LocalDate.of(2026, 5, 26));
        request.setCurrencyCode("EUR");
        request.setNotes("Preventivo convertibile");

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

        QuoteUpdateRequest updateRequest = new QuoteUpdateRequest();
        updateRequest.setCustomerId(customerId);
        updateRequest.setQuoteDate(created.getQuoteDate());
        updateRequest.setValidUntil(created.getValidUntil());
        updateRequest.setStatus("SENT");
        updateRequest.setCurrencyCode(created.getCurrencyCode());
        updateRequest.setNotes(created.getNotes());
        updateRequest.setLines(List.of(line1, line2));

        return quoteService.update(tenantId, created.getId(), updateRequest);
    }
}