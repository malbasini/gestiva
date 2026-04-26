package com.gestiva.security;

import com.gestiva.common.exception.NotFoundException;
import com.gestiva.crm.contact.entity.Customer;
import com.gestiva.platform.tenant.entity.Tenant;
import com.gestiva.sales.order.service.SalesOrderService;
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

import static org.junit.jupiter.api.Assertions.assertThrows;

class TenantIsolationIntegrationTest extends AbstractMySqlIntegrationTest {

    @Autowired
    private QuoteService quoteService;

    @Autowired
    private SalesOrderService salesOrderService;

    @Autowired
    private TestDataFactory testDataFactory;

    private Long tenantAId;
    private Long tenantBId;
    private Long customerAId;
    private Long customerBId;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void setUp() {
        databaseCleaner.clean();
        Tenant tenantA = testDataFactory.createTenant();
        Tenant tenantB = testDataFactory.createTenant();

        Customer customerA = testDataFactory.createCustomer(tenantA.getId(), "Rossi Srl");
        Customer customerB = testDataFactory.createCustomer(tenantB.getId(), "Bianchi Srl");

        this.tenantAId = tenantA.getId();
        this.tenantBId = tenantB.getId();
        this.customerAId = customerA.getId();
        this.customerBId = customerB.getId();
    }

    @Test
    void findQuoteById_shouldFail_whenQuoteBelongsToDifferentTenant() {
        QuoteResponse quoteA = createSentQuoteForTenantA();

        assertThrows(NotFoundException.class,
                () -> quoteService.findById(tenantBId, quoteA.getId()));
    }

    @Test
    void updateQuote_shouldFail_whenQuoteBelongsToDifferentTenant() {
        QuoteResponse quoteA = createSentQuoteForTenantA();

        QuoteUpdateRequest updateRequest = new QuoteUpdateRequest();
        updateRequest.setCustomerId(customerBId);
        updateRequest.setQuoteDate(LocalDate.of(2026, 4, 26));
        updateRequest.setValidUntil(LocalDate.of(2026, 5, 26));
        updateRequest.setStatus("SENT");
        updateRequest.setCurrencyCode("EUR");
        updateRequest.setNotes("Tentativo cross-tenant");
        updateRequest.setLines(List.of(buildLine("Servizio non autorizzato", "1.000", "100.00", "0.00", "22.00")));

        assertThrows(NotFoundException.class,
                () -> quoteService.update(tenantBId, quoteA.getId(), updateRequest));
    }

    @Test
    void deleteQuote_shouldFail_whenQuoteBelongsToDifferentTenant() {
        QuoteResponse quoteA = createSentQuoteForTenantA();

        assertThrows(NotFoundException.class,
                () -> quoteService.delete(tenantBId, quoteA.getId()));
    }

    @Test
    void convertQuoteToOrder_shouldFail_whenQuoteBelongsToDifferentTenant() {
        QuoteResponse quoteA = createSentQuoteForTenantA();

        assertThrows(NotFoundException.class,
                () -> salesOrderService.convertFromQuote(tenantBId, quoteA.getId()));
    }

    private QuoteResponse createSentQuoteForTenantA() {
        QuoteCreateRequest createRequest = new QuoteCreateRequest();
        createRequest.setCustomerId(customerAId);
        createRequest.setQuoteDate(LocalDate.of(2026, 4, 26));
        createRequest.setValidUntil(LocalDate.of(2026, 5, 26));
        createRequest.setCurrencyCode("EUR");
        createRequest.setNotes("Preventivo tenant A");
        createRequest.setLines(List.of(
                buildLine("Modulo CRM", "1.000", "1000.00", "10.00", "22.00"),
                buildLine("Setup", "1.000", "200.00", "0.00", "22.00")
        ));

        QuoteResponse created = quoteService.create(tenantAId, createRequest);

        QuoteUpdateRequest updateRequest = new QuoteUpdateRequest();
        updateRequest.setCustomerId(customerAId);
        updateRequest.setQuoteDate(created.getQuoteDate());
        updateRequest.setValidUntil(created.getValidUntil());
        updateRequest.setStatus("SENT");
        updateRequest.setCurrencyCode(created.getCurrencyCode());
        updateRequest.setNotes(created.getNotes());
        updateRequest.setLines(List.of(
                buildLine("Modulo CRM", "1.000", "1000.00", "10.00", "22.00"),
                buildLine("Setup", "1.000", "200.00", "0.00", "22.00")
        ));

        return quoteService.update(tenantAId, created.getId(), updateRequest);
    }

    private QuoteLineRequest buildLine(String description,
                                       String quantity,
                                       String unitPrice,
                                       String discountPct,
                                       String taxPct) {
        QuoteLineRequest line = new QuoteLineRequest();
        line.setDescription(description);
        line.setQuantity(new BigDecimal(quantity));
        line.setUnitPrice(new BigDecimal(unitPrice));
        line.setDiscountPct(new BigDecimal(discountPct));
        line.setTaxPct(new BigDecimal(taxPct));
        return line;
    }
    @Test
    void findAll_shouldReturnOnlyQuotesOfCurrentTenant() {
        createSentQuoteForTenantA();

        QuoteCreateRequest createRequestB = new QuoteCreateRequest();
        createRequestB.setCustomerId(customerBId);
        createRequestB.setQuoteDate(LocalDate.of(2026, 4, 27));
        createRequestB.setValidUntil(LocalDate.of(2026, 5, 27));
        createRequestB.setCurrencyCode("EUR");
        createRequestB.setLines(List.of(
                buildLine("Servizio B", "1.000", "300.00", "0.00", "22.00")
        ));

        quoteService.create(tenantBId, createRequestB);

        var quotesA = quoteService.findAll(tenantAId);
        var quotesB = quoteService.findAll(tenantBId);

        org.junit.jupiter.api.Assertions.assertEquals(1, quotesA.size());
        org.junit.jupiter.api.Assertions.assertEquals(1, quotesB.size());
        org.junit.jupiter.api.Assertions.assertNotEquals(quotesA.get(0).getId(), quotesB.get(0).getId());
    }
}