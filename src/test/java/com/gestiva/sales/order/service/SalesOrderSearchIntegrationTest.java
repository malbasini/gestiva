package com.gestiva.sales.order.service;

import com.gestiva.common.dto.PageResponse;
import com.gestiva.crm.contact.entity.Customer;
import com.gestiva.platform.tenant.entity.Tenant;
import com.gestiva.sales.order.dto.SalesOrderResponse;
import com.gestiva.sales.order.dto.SalesOrderSearchRequest;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SalesOrderSearchIntegrationTest extends AbstractMySqlIntegrationTest {

    @Autowired
    private QuoteService quoteService;

    @Autowired
    private SalesOrderService salesOrderService;

    @Autowired
    private TestDataFactory testDataFactory;

    private Long tenantId;
    private Long otherTenantId;
    private Long customerAId;
    private Long customerBId;

    private Long orderAId;
    private Long orderBId;
    private Long quoteAId;
    private Long quoteBId;

    @BeforeEach
    void setUp() {
        Tenant tenant = testDataFactory.createTenant();
        Tenant otherTenant = testDataFactory.createTenant();

        Customer customerA = testDataFactory.createCustomer(tenant.getId(), "Rossi Srl");
        Customer customerB = testDataFactory.createCustomer(tenant.getId(), "Bianchi Srl");
        Customer otherCustomer = testDataFactory.createCustomer(otherTenant.getId(), "Alfa Srl");

        this.tenantId = tenant.getId();
        this.otherTenantId = otherTenant.getId();
        this.customerAId = customerA.getId();
        this.customerBId = customerB.getId();

        QuoteResponse quoteA = createSentQuote(tenantId, customerAId, "Modulo CRM", "1000.00");
        QuoteResponse quoteB = createSentQuote(tenantId, customerBId, "Setup", "200.00");
        QuoteResponse otherTenantQuote = createSentQuote(otherTenantId, otherCustomer.getId(), "Servizio Altro Tenant", "500.00");

        this.quoteAId = quoteA.getId();
        this.quoteBId = quoteB.getId();

        SalesOrderResponse orderA = salesOrderService.convertFromQuote(tenantId, quoteA.getId());
        SalesOrderResponse orderB = salesOrderService.convertFromQuote(tenantId, quoteB.getId());
        salesOrderService.convertFromQuote(otherTenantId, otherTenantQuote.getId());

        this.orderAId = orderA.getId();
        this.orderBId = orderB.getId();
    }

    @Test
    void search_shouldFilterByQuoteId() {
        SalesOrderSearchRequest request = new SalesOrderSearchRequest();
        request.setQuoteId(quoteAId);

        PageResponse<SalesOrderResponse> response = salesOrderService.search(
                tenantId,
                request,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "orderDate"))
        );

        assertEquals(1, response.getContent().size());
        assertEquals(quoteAId, response.getContent().get(0).getQuoteId());
        assertEquals(orderAId, response.getContent().get(0).getId());
    }

    @Test
    void search_shouldFilterByCustomerId() {
        SalesOrderSearchRequest request = new SalesOrderSearchRequest();
        request.setCustomerId(customerBId);

        PageResponse<SalesOrderResponse> response = salesOrderService.search(
                tenantId,
                request,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "orderDate"))
        );

        assertEquals(1, response.getContent().size());
        assertEquals(customerBId, response.getContent().get(0).getCustomerId());
        assertEquals(orderBId, response.getContent().get(0).getId());
    }

    @Test
    void search_shouldFilterByStatus() {
        SalesOrderSearchRequest request = new SalesOrderSearchRequest();
        request.setStatus("CONFIRMED");

        PageResponse<SalesOrderResponse> response = salesOrderService.search(
                tenantId,
                request,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "orderDate"))
        );

        assertEquals(2, response.getContent().size());
        assertTrue(response.getContent().stream().allMatch(o -> "CONFIRMED".equals(o.getStatus())));
    }

    @Test
    void search_shouldSupportPagination() {
        SalesOrderSearchRequest request = new SalesOrderSearchRequest();

        PageResponse<SalesOrderResponse> response = salesOrderService.search(
                tenantId,
                request,
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "orderDate"))
        );

        assertEquals(1, response.getContent().size());
        assertEquals(0, response.getPage());
        assertEquals(1, response.getSize());
        assertEquals(2, response.getTotalElements());
        assertEquals(2, response.getTotalPages());
        assertTrue(response.isFirst());
        assertFalse(response.isLast());
    }

    @Test
    void search_shouldReturnOnlyOrdersOfCurrentTenant() {
        SalesOrderSearchRequest request = new SalesOrderSearchRequest();

        PageResponse<SalesOrderResponse> response = salesOrderService.search(
                tenantId,
                request,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "orderDate"))
        );

        var ids = response.getContent().stream().map(SalesOrderResponse::getId).toList();

        assertEquals(2, response.getContent().size());
        assertTrue(ids.contains(orderAId));
        assertTrue(ids.contains(orderBId));
    }

    private QuoteResponse createSentQuote(Long tenantId,
                                          Long customerId,
                                          String description,
                                          String unitPrice) {
        QuoteCreateRequest createRequest = new QuoteCreateRequest();
        createRequest.setCustomerId(customerId);
        createRequest.setQuoteDate(LocalDate.of(2026, 4, 29));
        createRequest.setValidUntil(LocalDate.of(2026, 5, 29));
        createRequest.setCurrencyCode("EUR");

        QuoteLineRequest line = new QuoteLineRequest();
        line.setDescription(description);
        line.setQuantity(new BigDecimal("1.000"));
        line.setUnitPrice(new BigDecimal(unitPrice));
        line.setDiscountPct(new BigDecimal("0.00"));
        line.setTaxPct(new BigDecimal("22.00"));

        createRequest.setLines(List.of(line));

        QuoteResponse created = quoteService.create(tenantId, createRequest);

        QuoteUpdateRequest updateRequest = new QuoteUpdateRequest();
        updateRequest.setCustomerId(customerId);
        updateRequest.setQuoteDate(created.getQuoteDate());
        updateRequest.setValidUntil(created.getValidUntil());
        updateRequest.setStatus("SENT");
        updateRequest.setCurrencyCode(created.getCurrencyCode());
        updateRequest.setLines(List.of(line));

        return quoteService.update(tenantId, created.getId(), updateRequest);
    }
}