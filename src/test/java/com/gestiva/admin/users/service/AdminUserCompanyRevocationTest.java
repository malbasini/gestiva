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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminUserCompanyRevocationTest {

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
                companies,
                accesses
        );
    }

    private Company company(Long id, boolean active) {

        Company company = mock(Company.class);

        when(company.getId()).thenReturn(id);
        when(company.getTenantId()).thenReturn(TENANT);
        when(company.isActive()).thenReturn(active);

        return company;
    }

    private UserCompanyAccess access(Long companyId) {

        UserCompanyAccess access = new UserCompanyAccess();

        access.setTenantId(TENANT);
        access.setUserId(USER);
        access.setCompanyId(companyId);

        return access;
    }

    @Test
    void ordinaryUserCannotHaveEmptyCompanySelection() {

        assertThrows(
                BusinessException.class,
                () -> service.replaceAccess(
                        TENANT,
                        USER,
                        List.of(),
                        false
                )
        );

        verifyNoInteractions(companies, accesses);
    }

    @Test
    void ordinaryUserCannotHaveNullCompanySelection() {

        assertThrows(
                BusinessException.class,
                () -> service.replaceAccess(
                        TENANT,
                        USER,
                        null,
                        false
                )
        );

        verifyNoInteractions(companies, accesses);
    }

    @Test
    void revokeActiveCompanyButPreserveInactiveAssociation() {

        // Società 10: attiva e confermata nel form.
        // Società 20: attiva e deselezionata.
        // Società 30: inattiva e non visibile nel form.
        Company active10 = company(10L, true);
        Company active20 = company(20L, true);
        Company inactive30 = company(30L, false);

        when(companies.findByTenantIdAndId(TENANT, 10L))
                .thenReturn(Optional.of(active10));

        when(companies.findByTenantIdAndId(TENANT, 20L))
                .thenReturn(Optional.of(active20));

        when(companies.findByTenantIdAndId(TENANT, 30L))
                .thenReturn(Optional.of(inactive30));

        UserCompanyAccess a10 = access(10L);
        UserCompanyAccess a20 = access(20L);
        UserCompanyAccess a30 = access(30L);

        when(accesses.findByTenantIdAndUserId(TENANT, USER))
                .thenReturn(List.of(a10, a20, a30));

        service.replaceAccess(
                TENANT,
                USER,
                List.of(10L),
                false
        );

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<UserCompanyAccess>> captor =
                ArgumentCaptor.forClass(Iterable.class);

        verify(accesses).deleteAll(captor.capture());

        List<UserCompanyAccess> deleted =
                new java.util.ArrayList<>();

        captor.getValue().forEach(deleted::add);

        assertEquals(1, deleted.size());

        assertEquals(
                20L,
                deleted.get(0).getCompanyId(),
                "Deve essere revocata soltanto la società 20"
        );

        assertFalse(deleted.contains(a10));
        assertFalse(deleted.contains(a30));

        verify(accesses, never()).save(any());
    }
}
