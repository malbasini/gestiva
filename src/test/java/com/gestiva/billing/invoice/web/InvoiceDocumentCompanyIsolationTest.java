package com.gestiva.billing.invoice.web;

import com.gestiva.billing.invoice.repository.InvoiceLineRepository;
import com.gestiva.billing.invoice.repository.InvoiceRepository;
import com.gestiva.common.exception.NotFoundException;
import com.gestiva.crm.contact.repository.CustomerRepository;
import com.gestiva.security.company.context.CompanyContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceDocumentCompanyIsolationTest {

    private static final Long TENANT_ID = 999L;
    private static final Long COMPANY_ID = 123L;
    private static final Long FOREIGN_INVOICE_ID = 456L;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private InvoiceLineRepository invoiceLineRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CompanyContext companyContext;

    @InjectMocks
    private InvoiceDocumentWebService service;

    @Test
    void shouldRejectDocumentOutsideActiveCompany() {

        when(companyContext.requireCurrentCompanyId())
                .thenReturn(COMPANY_ID);

        when(invoiceRepository.findByTenantIdAndCompanyIdAndId(
                TENANT_ID, COMPANY_ID, FOREIGN_INVOICE_ID
        )).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> service.getDocument(
                        TENANT_ID, FOREIGN_INVOICE_ID
                )
        );

        verify(invoiceRepository).findByTenantIdAndCompanyIdAndId(
                TENANT_ID, COMPANY_ID, FOREIGN_INVOICE_ID
        );

        verifyNoInteractions(
                invoiceLineRepository,
                customerRepository
        );
    }
}
