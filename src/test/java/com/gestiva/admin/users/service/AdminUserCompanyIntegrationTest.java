package com.gestiva.admin.users.service;

import com.gestiva.platform.company.repository.CompanyRepository;
import com.gestiva.admin.users.web.AdminUserEditForm;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.role.entity.Role;
import com.gestiva.security.role.repository.RoleRepository;
import com.gestiva.security.role.repository.UserRoleRepository;
import com.gestiva.security.tenant.entity.AppUser;
import com.gestiva.security.tenant.repository.AppUserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AdminUserCompanyIntegrationTest {

    private static final Long TENANT = 2L;
    private static final Long USER = 100L;

    private AppUserRepository users;
    private UserRoleRepository userRoles;
    private RoleRepository roles;
    private PasswordEncoder passwords;
    private AdminUserCompanyAssignmentService assignments;
    private AdminUserService service;

    @BeforeEach
    void setup() {

        users = mock(AppUserRepository.class);
        userRoles = mock(UserRoleRepository.class);
        roles = mock(RoleRepository.class);
        passwords = mock(PasswordEncoder.class);

        assignments = mock(
                AdminUserCompanyAssignmentService.class
        );

        service = new AdminUserService(
                users,
                userRoles,
                roles,
                passwords,
                assignments,
                org.mockito.Mockito.mock(CompanyRepository.class)
        );

        when(roles.findByTenantIdAndCodeInOrderByCodeAsc(
                eq(TENANT), anyCollection()
        )).thenReturn(List.of());
    }

    private AdminUserEditForm form() {

        AdminUserEditForm form = new AdminUserEditForm();

        form.setId(USER);
        form.setFirstName("Mario");
        form.setLastName("Rossi");
        form.setEmail("mario@example.invalid");
        form.setPassword("PasswordTest123!");
        form.setActive(true);
        form.setAdmin(false);

        form.setCompanyIds(List.of(10L, 20L));

        return form;
    }

    @Test
    void createShouldAssignSelectedCompanies() {

        AppUser saved = mock(AppUser.class);

        when(saved.getId()).thenReturn(USER);
        when(users.save(any(AppUser.class)))
                .thenReturn(saved);

        when(passwords.encode(anyString()))
                .thenReturn("HASH_TEST");

        AdminUserEditForm form = form();

        service.createUser(TENANT, form);

        verify(assignments).replaceAccess(
                TENANT,
                USER,
                List.of(10L, 20L),
                false
        );

        verify(users, times(1))
                .save(any(AppUser.class));
    }

    @Test
    void updateShouldPreserveMultipleRolesAndAssignCompanies() {

        AppUser existing = mock(AppUser.class);

        when(existing.getId()).thenReturn(USER);

        when(users.findByTenantIdAndId(TENANT, USER))
                .thenReturn(Optional.of(existing));

        Role sales = mock(Role.class);
        Role warehouse = mock(Role.class);

        when(sales.getTenantId()).thenReturn(TENANT);
        when(sales.getCode()).thenReturn("ROLE_SALES");
        when(sales.getId()).thenReturn(11L);

        when(warehouse.getTenantId()).thenReturn(TENANT);
        when(warehouse.getCode()).thenReturn("ROLE_WAREHOUSE");
        when(warehouse.getId()).thenReturn(12L);

        when(roles.findByTenantIdAndCodeInOrderByCodeAsc(
                eq(TENANT), anyCollection()
        )).thenReturn(List.of(sales, warehouse));

        AdminUserEditForm form = form();
        form.setPassword("");
        form.setRoles(List.of(
                "ROLE_SALES",
                "ROLE_WAREHOUSE"
        ));

        service.updateUser(TENANT, form);

        verify(userRoles).deleteByTenantIdAndUserId(
                TENANT, USER
        );

        verify(userRoles, times(2))
                .save(any());

        verify(assignments).replaceAccess(
                TENANT,
                USER,
                List.of(10L, 20L),
                false
        );
    }

    @Test
    void companyAssignmentFailureShouldPropagate() {

        AppUser saved = mock(AppUser.class);

        when(saved.getId()).thenReturn(USER);

        when(users.save(any(AppUser.class)))
                .thenReturn(saved);

        when(passwords.encode(anyString()))
                .thenReturn("HASH_TEST");

        doThrow(new BusinessException(
                "Società non autorizzata."
        )).when(assignments).replaceAccess(
                eq(TENANT),
                eq(USER),
                anyList(),
                eq(false)
        );

        assertThrows(
                BusinessException.class,
                () -> service.createUser(TENANT, form())
        );

        verify(assignments).replaceAccess(
                TENANT,
                USER,
                List.of(10L, 20L),
                false
        );
    }
}
