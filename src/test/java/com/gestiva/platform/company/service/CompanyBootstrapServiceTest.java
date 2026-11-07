package com.gestiva.platform.company.service;

import com.gestiva.accounting.v2.account.service.AccountChartBootstrapService;
import com.gestiva.platform.company.repository.CompanyRepository;
import com.gestiva.settings.company.repository.CompanySettingsRepository;
import com.gestiva.settings.sequence.repository.DocumentSequenceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.List;
import com.gestiva.platform.company.entity.Company;
import com.gestiva.platform.company.entity.CompanySettings;
import com.gestiva.settings.sequence.entity.DocumentSequence;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyBootstrapServiceTest {

    private static final Long TENANT_ID = 2L;
    private static final Long COMPANY_ID = 1L;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private CompanySettingsRepository companySettingsRepository;

    @Mock
    private DocumentSequenceRepository documentSequenceRepository;

    @Mock
    private AccountChartBootstrapService accountChartBootstrapService;

    @InjectMocks
    private CompanyBootstrapService service;

    @Test
    void initializeCompany_shouldRejectCompanyOutsideTenant() {

        when(companyRepository.findByTenantIdAndIdForUpdate(
                TENANT_ID, COMPANY_ID
        )).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.initializeCompany(TENANT_ID, COMPANY_ID)
        );

        verify(companyRepository).findByTenantIdAndIdForUpdate(
                TENANT_ID, COMPANY_ID
        );

        verifyNoInteractions(
                companySettingsRepository,
                documentSequenceRepository,
                accountChartBootstrapService
        );
    }

    @Test
    void initializeCompany_shouldPreserveExistingConfiguration() {

        Company company = new Company();
        CompanySettings settings = new CompanySettings();

        when(companyRepository.findByTenantIdAndIdForUpdate(
                TENANT_ID, COMPANY_ID
        )).thenReturn(Optional.of(company));

        when(companyRepository.findByTenantIdAndId(
                TENANT_ID, COMPANY_ID
        )).thenReturn(Optional.of(company));

        when(companySettingsRepository.findByTenantIdAndCompanyId(
                TENANT_ID, COMPANY_ID
        )).thenReturn(Optional.of(settings));

        List<String> types = List.of(
                "QUOTE",
                "SALES_ORDER",
                "DELIVERY_NOTE",
                "SALES_INVOICE",
                "PURCHASE_ORDER",
                "GOODS_RECEIPT",
                "SUPPLIER_INVOICE",
                "JOURNAL_ENTRY",
                "SUPPLIER_CREDIT_NOTE",
                "CUSTOMER_CREDIT_NOTE"
        );

        List<DocumentSequence> sequences = types.stream()
                .map(type -> {
                    DocumentSequence sequence = new DocumentSequence();
                    sequence.setDocumentType(type);
                    return sequence;
                })
                .toList();

        when(documentSequenceRepository
                .findByTenantIdAndCompanyIdOrderByDocumentTypeAsc(
                        TENANT_ID, COMPANY_ID
                )).thenReturn(sequences);

        service.initializeCompany(TENANT_ID, COMPANY_ID);

        verify(companyRepository)
                .findByTenantIdAndIdForUpdate(TENANT_ID, COMPANY_ID);

        verify(companyRepository, times(2))
                .findByTenantIdAndId(TENANT_ID, COMPANY_ID);

        verify(companySettingsRepository)
                .findByTenantIdAndCompanyId(TENANT_ID, COMPANY_ID);

        verify(documentSequenceRepository)
                .findByTenantIdAndCompanyIdOrderByDocumentTypeAsc(
                        TENANT_ID, COMPANY_ID
                );

        verify(accountChartBootstrapService)
                .initializeDefaultChartOfAccounts(
                        TENANT_ID, COMPANY_ID
                );

        verify(companySettingsRepository, never()).save(any());
        verify(documentSequenceRepository, never()).save(any());
    }

    @Test
    void initializeCompany_shouldRejectNullIdentifiers() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.initializeCompany(null, COMPANY_ID)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.initializeCompany(TENANT_ID, null)
        );

        verifyNoInteractions(
                companyRepository,
                companySettingsRepository,
                documentSequenceRepository,
                accountChartBootstrapService
        );
    }


    @Test
    void initializeCompany_shouldCompletePartialConfiguration() {

        Company company = new Company();
        CompanySettings settings = new CompanySettings();

        when(companyRepository.findByTenantIdAndIdForUpdate(
                TENANT_ID, COMPANY_ID
        )).thenReturn(Optional.of(company));

        when(companyRepository.findByTenantIdAndId(
                TENANT_ID, COMPANY_ID
        )).thenReturn(Optional.of(company));

        when(companySettingsRepository.findByTenantIdAndCompanyId(
                TENANT_ID, COMPANY_ID
        )).thenReturn(Optional.of(settings));

        DocumentSequence quote = new DocumentSequence();
        quote.setDocumentType("QUOTE");
        quote.setPrefix("CUSTOM-Q-");
        quote.setNextNumber(125L);
        quote.setPaddingSize(7);

        DocumentSequence invoice = new DocumentSequence();
        invoice.setDocumentType("SALES_INVOICE");
        invoice.setPrefix("CUSTOM-I-");
        invoice.setNextNumber(450L);
        invoice.setPaddingSize(6);

        when(documentSequenceRepository
                .findByTenantIdAndCompanyIdOrderByDocumentTypeAsc(
                        TENANT_ID, COMPANY_ID
                )).thenReturn(List.of(quote, invoice));

        service.initializeCompany(TENANT_ID, COMPANY_ID);

        verify(companySettingsRepository, never()).save(any());

        org.mockito.ArgumentCaptor<DocumentSequence> captor =
                org.mockito.ArgumentCaptor.forClass(DocumentSequence.class);

        verify(documentSequenceRepository, times(8))
                .save(captor.capture());

        java.util.Map<String, String> expectedPrefixes =
                java.util.Map.of(
                        "SALES_ORDER", "SO-",
                        "DELIVERY_NOTE", "DDT-",
                        "PURCHASE_ORDER", "PO-",
                        "GOODS_RECEIPT", "GR-",
                        "SUPPLIER_INVOICE", "SI-",
                        "JOURNAL_ENTRY", "JE-",
                        "SUPPLIER_CREDIT_NOTE", "SCN-",
                        "CUSTOMER_CREDIT_NOTE", "CCN-"
                );

        org.junit.jupiter.api.Assertions.assertEquals(
                expectedPrefixes.keySet(),
                captor.getAllValues().stream()
                        .map(DocumentSequence::getDocumentType)
                        .collect(java.util.stream.Collectors.toSet())
        );

        for (DocumentSequence sequence : captor.getAllValues()) {
            org.junit.jupiter.api.Assertions.assertEquals(
                    TENANT_ID, sequence.getTenantId());
            org.junit.jupiter.api.Assertions.assertEquals(
                    COMPANY_ID, sequence.getCompanyId());
            org.junit.jupiter.api.Assertions.assertEquals(
                    expectedPrefixes.get(sequence.getDocumentType()),
                    sequence.getPrefix());
            org.junit.jupiter.api.Assertions.assertEquals(
                    1L, sequence.getNextNumber());
            org.junit.jupiter.api.Assertions.assertEquals(
                    5, sequence.getPaddingSize());
        }

        org.junit.jupiter.api.Assertions.assertEquals(
                "CUSTOM-Q-", quote.getPrefix());
        org.junit.jupiter.api.Assertions.assertEquals(
                125L, quote.getNextNumber());
        org.junit.jupiter.api.Assertions.assertEquals(
                7, quote.getPaddingSize());

        org.junit.jupiter.api.Assertions.assertEquals(
                "CUSTOM-I-", invoice.getPrefix());
        org.junit.jupiter.api.Assertions.assertEquals(
                450L, invoice.getNextNumber());
        org.junit.jupiter.api.Assertions.assertEquals(
                6, invoice.getPaddingSize());

        verify(accountChartBootstrapService)
                .initializeDefaultChartOfAccounts(
                        TENANT_ID, COMPANY_ID);
    }


@Test
void initializeCompany_shouldNotAccessAnotherTenantCompany() {

    Long otherTenantId = 999L;

    when(companyRepository.findByTenantIdAndIdForUpdate(
            otherTenantId, COMPANY_ID
    )).thenReturn(Optional.empty());

    assertThrows(
            IllegalArgumentException.class,
            () -> service.initializeCompany(
                    otherTenantId, COMPANY_ID
            )
    );

    verify(companyRepository).findByTenantIdAndIdForUpdate(
            otherTenantId, COMPANY_ID
    );

    verify(companyRepository, never()).findByTenantIdAndIdForUpdate(
            TENANT_ID, COMPANY_ID
    );

    verifyNoInteractions(
            companySettingsRepository,
            documentSequenceRepository,
            accountChartBootstrapService
    );
  }


@Test
void initializeCompany_shouldPropagateAccountingBootstrapFailure() {

    Company company = new Company();
    CompanySettings settings = new CompanySettings();

    when(companyRepository.findByTenantIdAndIdForUpdate(
            TENANT_ID, COMPANY_ID
    )).thenReturn(Optional.of(company));

        when(companyRepository.findByTenantIdAndId(
                TENANT_ID, COMPANY_ID
        )).thenReturn(Optional.of(company));

    when(companySettingsRepository.findByTenantIdAndCompanyId(
            TENANT_ID, COMPANY_ID
    )).thenReturn(Optional.of(settings));

    when(documentSequenceRepository
            .findByTenantIdAndCompanyIdOrderByDocumentTypeAsc(
                    TENANT_ID, COMPANY_ID
            )).thenReturn(List.of());

    doThrow(new IllegalStateException(
            "Errore simulato durante il bootstrap contabile"
    )).when(accountChartBootstrapService)
            .initializeDefaultChartOfAccounts(
                    TENANT_ID, COMPANY_ID
            );

    IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> service.initializeCompany(
                    TENANT_ID, COMPANY_ID
            )
    );

    org.junit.jupiter.api.Assertions.assertEquals(
            "Errore simulato durante il bootstrap contabile",
            exception.getMessage()
    );

    verify(documentSequenceRepository, times(10))
            .save(any(DocumentSequence.class));

    verify(companySettingsRepository, never())
            .save(any());

    verify(accountChartBootstrapService)
            .initializeDefaultChartOfAccounts(
                    TENANT_ID, COMPANY_ID
            );
     }  

    @Test
    void initializeCompany_shouldAcquireLockBeforeInitialization() {

        Company company = new Company();

        when(companyRepository.findByTenantIdAndIdForUpdate(
                TENANT_ID, COMPANY_ID
        )).thenReturn(Optional.of(company));

        when(companyRepository.findByTenantIdAndId(
                TENANT_ID, COMPANY_ID
        )).thenReturn(Optional.of(company));

        when(companySettingsRepository.findByTenantIdAndCompanyId(
                TENANT_ID, COMPANY_ID
        )).thenReturn(Optional.of(new CompanySettings()));

        when(documentSequenceRepository
                .findByTenantIdAndCompanyIdOrderByDocumentTypeAsc(
                        TENANT_ID, COMPANY_ID
                )).thenReturn(List.of());

        service.initializeCompany(TENANT_ID, COMPANY_ID);

        org.mockito.InOrder order = inOrder(
                companyRepository,
                companySettingsRepository,
                documentSequenceRepository,
                accountChartBootstrapService
        );

        order.verify(companyRepository)
                .findByTenantIdAndIdForUpdate(TENANT_ID, COMPANY_ID);

        order.verify(companyRepository)
                .findByTenantIdAndId(TENANT_ID, COMPANY_ID);

        order.verify(companySettingsRepository)
                .findByTenantIdAndCompanyId(TENANT_ID, COMPANY_ID);

        order.verify(documentSequenceRepository)
                .findByTenantIdAndCompanyIdOrderByDocumentTypeAsc(
                        TENANT_ID, COMPANY_ID
                );

        order.verify(accountChartBootstrapService)
                .initializeDefaultChartOfAccounts(TENANT_ID, COMPANY_ID);
    }

}
