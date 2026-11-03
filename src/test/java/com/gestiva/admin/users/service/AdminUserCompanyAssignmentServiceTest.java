package com.gestiva.admin.users.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.platform.company.entity.Company;
import com.gestiva.platform.company.repository.CompanyRepository;
import com.gestiva.security.company.entity.UserCompanyAccess;
import com.gestiva.security.company.repository.UserCompanyAccessRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AdminUserCompanyAssignmentServiceTest {

    private static final Long TENANT = 2L;
    private static final Long USER = 100L;

    private CompanyRepository companies;
    private UserCompanyAccessRepository accesses;
    private AdminUserCompanyAssignmentService service;

    @BeforeEach
    void setup() {
        companies = mock(CompanyRepository.class);
        accesses = mock(UserCompanyAccessRepository.class);

        service = new AdminUserCompanyAssignmentService(
                companies, accesses
        );
    }

    private Company company(Long tenantId, Long companyId) {
        Company c = mock(Company.class);

        when(c.getTenantId()).thenReturn(tenantId);
        when(c.getId()).thenReturn(companyId);
        when(c.isActive()).thenReturn(true);

        return c;
    }

    private UserCompanyAccess access(Long companyId) {
        UserCompanyAccess a = new UserCompanyAccess();
        a.setTenantId(TENANT);
        a.setUserId(USER);
        a.setCompanyId(companyId);
        return a;
    }

    @Test
    void shouldAssignMultipleCompanies() {

        Company company10 = company(TENANT, 10L);
        Company company20 = company(TENANT, 20L);

        when(companies.findByTenantIdAndId(TENANT, 10L))
                .thenReturn(Optional.of(company10));

        when(companies.findByTenantIdAndId(TENANT, 20L))
                .thenReturn(Optional.of(company20));

        when(accesses.findByTenantIdAndUserId(TENANT, USER))
                .thenReturn(List.of());

        service.replaceAccess(
                TENANT, USER, List.of(10L, 20L), false
        );

        ArgumentCaptor<UserCompanyAccess> captor =
                ArgumentCaptor.forClass(UserCompanyAccess.class);

        verify(accesses, times(2))
                .save(captor.capture());

        assertEquals(
                Set.of(10L, 20L),
                captor.getAllValues()
                        .stream()
                        .map(UserCompanyAccess::getCompanyId)
                        .collect(Collectors.toSet())
        );

        assertTrue(captor.getAllValues().stream()
                .allMatch(a -> TENANT.equals(a.getTenantId())
                        && USER.equals(a.getUserId())));

        verify(accesses, never())
                .deleteAll(any());
    }

    @Test
    void shouldRejectForeignCompanyBeforeAnyWrite() {

        Company company10 = company(TENANT, 10L);
        Company foreignCompany = company(3L, 20L);

        when(companies.findByTenantIdAndId(TENANT, 10L))
                .thenReturn(Optional.of(company10));

        // Risultato incoerente simulato: società tenant 3.
        when(companies.findByTenantIdAndId(TENANT, 20L))
                .thenReturn(Optional.of(foreignCompany));

        assertThrows(
                BusinessException.class,
                () -> service.replaceAccess(
                        TENANT, USER, List.of(10L, 20L), false
                )
        );

        verify(accesses, never()).save(any());
        verify(accesses, never()).deleteAll(any());
    }

    @Test
    void shouldRevokeOnlyRemovedCompany() {

        Company company20 = company(TENANT, 20L);

        when(companies.findByTenantIdAndId(TENANT, 20L))
                .thenReturn(Optional.of(company20));

        UserCompanyAccess oldAccess = access(10L);
        UserCompanyAccess keptAccess = access(20L);

        when(accesses.findByTenantIdAndUserId(TENANT, USER))
                .thenReturn(List.of(oldAccess, keptAccess));

        service.replaceAccess(
                TENANT, USER, List.of(20L), false
        );

        verify(accesses).deleteAll(List.of(oldAccess));
        verify(accesses, never()).save(any());
    }

    @Test
    void shouldRequireCompanyForRegularUser() {

        assertThrows(
                BusinessException.class,
                () -> service.replaceAccess(
                        TENANT, USER, List.of(), false
                )
        );

        verifyNoInteractions(companies, accesses);
    }

    @Test
    void shouldPreserveExistingInactiveCompanyWhenUpdatingActiveOnes() {

        // La società 10 è attiva e rimane selezionata.
        Company activeCompany = company(TENANT, 10L);

        when(companies.findByTenantIdAndId(TENANT, 10L))
                .thenReturn(Optional.of(activeCompany));

        // La società 20 era autorizzata in precedenza,
        // ma ora è inattiva e non compare nel form.
        Company inactiveCompany = mock(Company.class);

        when(inactiveCompany.getTenantId()).thenReturn(TENANT);
        when(inactiveCompany.getId()).thenReturn(20L);
        when(inactiveCompany.isActive()).thenReturn(false);

        when(companies.findByTenantIdAndId(TENANT, 20L))
                .thenReturn(Optional.of(inactiveCompany));

        UserCompanyAccess activeAccess = access(10L);
        UserCompanyAccess inactiveAccess = access(20L);

        when(accesses.findByTenantIdAndUserId(TENANT, USER))
                .thenReturn(List.of(activeAccess, inactiveAccess));

        // Il form invia soltanto la società attiva 10.
        service.replaceAccess(
                TENANT,
                USER,
                List.of(10L),
                false
        );

        // L'associazione alla società inattiva non deve
        // essere cancellata implicitamente.
        verify(accesses, never()).deleteAll(any());
        verify(accesses, never()).save(any());
    }

    @Test
    void shouldAllowAdminWithoutExplicitAssignments() {

        when(accesses.findByTenantIdAndUserId(TENANT, USER))
                .thenReturn(List.of());

        service.replaceAccess(
                TENANT, USER, List.of(), true
        );

        verify(accesses, never()).save(any());
        verify(accesses, never()).deleteAll(any());
    }
}
