package com.gestiva.platform.company.service;

import com.gestiva.platform.company.entity.Company;
import com.gestiva.platform.company.repository.CompanyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyManagementServiceTest {

    private static final Long TENANT_ID = 999L;
    private static final Long COMPANY_ID = 500L;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private CompanyBootstrapService companyBootstrapService;

    @InjectMocks
    private CompanyManagementService service;

    @Test
    void createCompany_shouldInitializeBootstrap() {

        when(companyRepository.findByTenantIdAndCode(
                TENANT_ID, "TEST01"
        )).thenReturn(Optional.empty());

        when(companyRepository.saveAndFlush(any(Company.class)))
                .thenAnswer(invocation -> {
                    Company company = invocation.getArgument(0);
                    company.setId(COMPANY_ID);
                    return company;
                });

        Company created = service.createCompany(
                TENANT_ID,
                "test01",
                "Società di prova"
        );

        assertNotNull(created);
        assertEquals(TENANT_ID, created.getTenantId());
        assertEquals(COMPANY_ID, created.getId());
        assertEquals("TEST01", created.getCode());
        assertEquals("Società di prova", created.getLegalName());
        assertEquals("IT", created.getCountryCode());
        assertEquals("EUR", created.getCurrencyCode());
        assertEquals("it", created.getLocaleCode());
        assertTrue(created.isActive());

        var order = inOrder(
                companyRepository,
                companyBootstrapService
        );

        order.verify(companyRepository)
                .saveAndFlush(any(Company.class));

        order.verify(companyBootstrapService)
                .initializeCompany(TENANT_ID, COMPANY_ID);
    }

    @Test
    void createCompany_shouldRejectDuplicateCode() {

        Company existing = new Company();
        existing.setTenantId(TENANT_ID);
        existing.setCode("TEST01");

        when(companyRepository.findByTenantIdAndCode(
                TENANT_ID, "TEST01"
        )).thenReturn(Optional.of(existing));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.createCompany(
                        TENANT_ID,
                        "test01",
                        "Seconda società"
                )
        );

        assertEquals(
                "Codice società già utilizzato nel tenant.",
                exception.getMessage()
        );

        verify(companyRepository, never())
                .saveAndFlush(any(Company.class));

        verifyNoInteractions(companyBootstrapService);
    }


    @Test
    void getCompany_shouldRejectCompanyOutsideTenant() {

        Long otherTenantId = 998L;

        when(companyRepository.findByTenantIdAndId(
                otherTenantId, COMPANY_ID
        )).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.getCompany(
                        otherTenantId, COMPANY_ID
                )
        );

        assertEquals(
                "Società non trovata per il tenant indicato.",
                exception.getMessage()
        );

        verify(companyRepository).findByTenantIdAndId(
                otherTenantId, COMPANY_ID
        );

        verify(companyRepository, never()).findById(
                COMPANY_ID
        );

        verifyNoInteractions(companyBootstrapService);
    }


    @Test
    void updateCompany_shouldPreserveCodeAndSkipBootstrap() {

        Company existing = new Company();
        existing.setTenantId(TENANT_ID);
        existing.setId(COMPANY_ID);
        existing.setCode("TEST01");
        existing.setLegalName("Società originale");
        existing.setActive(true);

        Company other = new Company();
        other.setTenantId(TENANT_ID);
        other.setId(501L);
        other.setCode("TEST02");
        other.setLegalName("Altra società");
        other.setActive(true);

        when(companyRepository.findAllByTenantIdForUpdate(
                TENANT_ID
        )).thenReturn(java.util.List.of(existing, other));

        when(companyRepository.save(existing))
                .thenReturn(existing);

        Company updated = service.updateCompany(
                TENANT_ID,
                COMPANY_ID,
                "Società aggiornata",
                false
        );

        assertSame(existing, updated);
        assertEquals("TEST01", updated.getCode());
        assertEquals("Società aggiornata", updated.getLegalName());
        assertFalse(updated.isActive());

        assertTrue(other.isActive());

        verify(companyRepository).findAllByTenantIdForUpdate(
                TENANT_ID
        );

        verify(companyRepository, never())
                .findByTenantIdAndId(anyLong(), anyLong());

        verify(companyRepository).save(existing);
        verify(companyRepository, never())
                .saveAndFlush(any(Company.class));

        verifyNoInteractions(companyBootstrapService);
    }


    @Test
    void updateCompany_shouldRejectDeactivationOfLastActiveCompany() {

        Company company = new Company();
        company.setTenantId(TENANT_ID);
        company.setId(COMPANY_ID);
        company.setCode("TEST01");
        company.setLegalName("Società principale");
        company.setActive(true);

        when(companyRepository.findAllByTenantIdForUpdate(
                TENANT_ID
        )).thenReturn(java.util.List.of(company));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.updateCompany(
                        TENANT_ID,
                        COMPANY_ID,
                        "Società modificata",
                        false
                )
        );

        assertEquals(
                "Impossibile disattivare l'ultima società attiva del tenant.",
                exception.getMessage()
        );

        assertTrue(company.isActive());
        assertEquals("Società principale", company.getLegalName());

        verify(companyRepository).findAllByTenantIdForUpdate(
                TENANT_ID
        );

        verify(companyRepository, never()).save(any(Company.class));
        verify(companyRepository, never())
                .saveAndFlush(any(Company.class));

        verifyNoInteractions(companyBootstrapService);
        verifyNoMoreInteractions(companyRepository);
    }


    @Test
    void updateCompany_shouldReactivateInactiveCompany() {

        Company company = new Company();
        company.setTenantId(TENANT_ID);
        company.setId(COMPANY_ID);
        company.setCode("TEST01");
        company.setLegalName("Società inattiva");
        company.setActive(false);

        when(companyRepository.findAllByTenantIdForUpdate(
                TENANT_ID
        )).thenReturn(java.util.List.of(company));

        when(companyRepository.save(company))
                .thenReturn(company);

        Company updated = service.updateCompany(
                TENANT_ID,
                COMPANY_ID,
                "Società riattivata",
                true
        );

        assertSame(company, updated);
        assertTrue(updated.isActive());
        assertEquals("TEST01", updated.getCode());
        assertEquals("Società riattivata", updated.getLegalName());

        verify(companyRepository).findAllByTenantIdForUpdate(
                TENANT_ID
        );

        verify(companyRepository).save(company);

        verifyNoInteractions(companyBootstrapService);
        verifyNoMoreInteractions(companyRepository);
    }

}
