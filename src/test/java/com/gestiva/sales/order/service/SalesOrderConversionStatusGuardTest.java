package com.gestiva.sales.order.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.crm.contact.repository.CustomerRepository;
import com.gestiva.sales.order.mapper.SalesOrderMapper;
import com.gestiva.sales.order.repository.SalesOrderLineRepository;
import com.gestiva.sales.order.repository.SalesOrderRepository;
import com.gestiva.sales.quote.entity.Quote;
import com.gestiva.sales.quote.repository.QuoteLineRepository;
import com.gestiva.sales.quote.repository.QuoteRepository;
import com.gestiva.settings.sequence.repository.DocumentSequenceRepository;
import com.gestiva.settings.sequence.service.DocumentSequenceService;
import com.gestiva.security.company.context.CompanyContext;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalesOrderConversionStatusGuardTest {

    private static final Long TENANT_ID = 2L;
    private static final Long COMPANY_ID = 3L;
    private static final Long QUOTE_ID = 10L;

    @Mock
    private SalesOrderRepository salesOrderRepository;

    @Mock
    private SalesOrderLineRepository salesOrderLineRepository;

    @Mock
    private QuoteRepository quoteRepository;

    @Mock
    private QuoteLineRepository quoteLineRepository;

    @Mock
    private DocumentSequenceRepository documentSequenceRepository;

    @Mock
    private SalesOrderMapper salesOrderMapper;

    @Mock
    private DocumentSequenceService documentSequenceService;

    @Mock
    private CompanyContext companyContext;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private SalesOrderService salesOrderService;

    @Test
    void draftQuoteCannotBeConverted() {
        assertConversionRejected("DRAFT");
    }

    @Test
    void sentQuoteCannotBeConverted() {
        assertConversionRejected("SENT");
    }

    @Test
    void rejectedQuoteCannotBeConverted() {
        assertConversionRejected("REJECTED");
    }

    private void assertConversionRejected(String status) {

        Quote quote = new Quote();
        quote.setId(QUOTE_ID);
        quote.setTenantId(TENANT_ID);
        quote.setCompanyId(COMPANY_ID);
        quote.setStatus(status);

        when(companyContext.resolveCurrentCompanyId())
                .thenReturn(COMPANY_ID);

        when(quoteRepository.findByTenantIdAndCompanyIdAndId(
                TENANT_ID,
                COMPANY_ID,
                QUOTE_ID
        )).thenReturn(Optional.of(quote));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> salesOrderService.convertFromQuote(
                        TENANT_ID,
                        QUOTE_ID
                )
        );

        assertTrue(
                exception.getMessage().contains("non è convertibile"),
                "Messaggio inatteso: " + exception.getMessage()
        );

        verifyNoInteractions(
                salesOrderRepository,
                salesOrderLineRepository,
                quoteLineRepository,
                documentSequenceRepository,
                documentSequenceService,
                customerRepository,
                salesOrderMapper
        );

        verify(quoteRepository, never()).save(any());
    }
}
