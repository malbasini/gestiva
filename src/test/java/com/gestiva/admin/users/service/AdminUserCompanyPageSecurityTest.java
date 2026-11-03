package com.gestiva.admin.users.service;

import com.gestiva.admin.users.web.AdminUserPageView;
import com.gestiva.platform.company.entity.Company;
import com.gestiva.platform.company.repository.CompanyRepository;
import com.gestiva.security.role.repository.RoleRepository;
import com.gestiva.security.role.repository.UserRoleRepository;
import com.gestiva.security.tenant.repository.AppUserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminUserCompanyPageSecurityTest {

    @Test
    void shouldExposeOnlyActiveCompaniesOfCurrentTenant() {

        Long tenantId = 2L;

        CompanyRepository companies =
                mock(CompanyRepository.class);

        Company valid = mock(Company.class);
        when(valid.getId()).thenReturn(10L);
        when(valid.getTenantId()).thenReturn(tenantId);
        when(valid.getLegalName()).thenReturn("Società A");
        when(valid.isActive()).thenReturn(true);

        Company foreign = mock(Company.class);
        when(foreign.getTenantId()).thenReturn(3L);

        Company inactive = mock(Company.class);
        when(inactive.getTenantId()).thenReturn(tenantId);
        when(inactive.isActive()).thenReturn(false);

        when(companies
                .findByTenantIdAndActiveTrueOrderByLegalNameAsc(
                        tenantId
                ))
                .thenReturn(List.of(valid, foreign, inactive));

        AdminUserService service = new AdminUserService(
                mock(AppUserRepository.class),
                mock(UserRoleRepository.class),
                mock(RoleRepository.class),
                mock(PasswordEncoder.class),
                mock(AdminUserCompanyAssignmentService.class),
                companies
        );

        AdminUserPageView page = service.buildPage(tenantId);

        assertEquals(1, page.getAvailableCompanies().size());

        assertEquals(
                10L,
                page.getAvailableCompanies().get(0).getId()
        );

        assertEquals(
                "Società A",
                page.getAvailableCompanies().get(0).getLegalName()
        );

        assertEquals(
                AdminUserService.AVAILABLE_ROLES,
                page.getAvailableRoles()
        );

        verify(companies)
                .findByTenantIdAndActiveTrueOrderByLegalNameAsc(
                        tenantId
                );
    }
}
