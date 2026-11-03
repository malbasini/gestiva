package com.gestiva.security.service;

import com.gestiva.accounting.v2.account.repository.AccountRepository;
import com.gestiva.platform.company.entity.Company;
import com.gestiva.platform.company.repository.CompanyRepository;
import com.gestiva.platform.company.service.CompanyBootstrapService;
import com.gestiva.security.role.entity.Role;
import com.gestiva.security.role.entity.UserRole;
import com.gestiva.security.role.repository.RoleRepository;
import com.gestiva.security.role.repository.UserRoleRepository;
import com.gestiva.security.role.service.StandardRoleBootstrapService;
import com.gestiva.security.tenant.entity.AppUser;
import com.gestiva.security.tenant.entity.Tenant;
import com.gestiva.security.tenant.repository.AppUserRepository;
import com.gestiva.security.tenant.repository.TenantRepository;
import com.gestiva.security.web.RegistrationForm;
import com.gestiva.security.web.RegistrationResult;
import com.gestiva.warehouse.uom.service.UnitOfMeasureInitializer;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RegistrationRoleBootstrapTest {

    private static final Long TENANT_ID = 250L;
    private static final Long COMPANY_ID = 350L;
    private static final Long USER_ID = 450L;
    private static final Long ADMIN_ROLE_ID = 550L;

    @Test
    void shouldInitializeRolesBeforeAssigningAdminToFounder() {

        TenantRepository tenants = mock(TenantRepository.class);
        AppUserRepository users = mock(AppUserRepository.class);
        RoleRepository roles = mock(RoleRepository.class);
        UserRoleRepository userRoles = mock(UserRoleRepository.class);
        PasswordEncoder passwords = mock(PasswordEncoder.class);
        AccountRepository accounts = mock(AccountRepository.class);
        UnitOfMeasureInitializer uoms =
                mock(UnitOfMeasureInitializer.class);
        CompanyRepository companies = mock(CompanyRepository.class);
        CompanyBootstrapService companyBootstrap =
                mock(CompanyBootstrapService.class);
        StandardRoleBootstrapService roleBootstrap =
                mock(StandardRoleBootstrapService.class);

        RegistrationService service = new RegistrationService(
                tenants,
                users,
                roles,
                roleBootstrap,
                userRoles,
                passwords,
                accounts,
                uoms,
                companies,
                companyBootstrap
        );

        RegistrationForm form = mock(RegistrationForm.class);

        when(form.getTenantName()).thenReturn("Tenant di test");
        when(form.getTenantEmail())
                .thenReturn("tenant@example.invalid");
        when(form.getFirstName()).thenReturn("Utente");
        when(form.getLastName()).thenReturn("Fondatore");
        when(form.getUserEmail())
                .thenReturn("fondatore@example.invalid");
        when(form.getPassword()).thenReturn("TestPassword123!");
        when(form.getConfirmPassword())
                .thenReturn("TestPassword123!");

        when(passwords.encode("TestPassword123!"))
                .thenReturn("HASH_TEST");

        Tenant savedTenant = mock(Tenant.class);
        when(savedTenant.getId()).thenReturn(TENANT_ID);
        when(savedTenant.getName()).thenReturn("Tenant di test");
        when(savedTenant.getSlug()).thenReturn("tenant-di-test");

        when(tenants.save(any(Tenant.class)))
                .thenReturn(savedTenant);

        Company savedCompany = mock(Company.class);
        when(savedCompany.getId()).thenReturn(COMPANY_ID);

        when(companies.saveAndFlush(any(Company.class)))
                .thenReturn(savedCompany);

        AppUser savedUser = mock(AppUser.class);
        when(savedUser.getId()).thenReturn(USER_ID);

        when(users.save(any(AppUser.class)))
                .thenReturn(savedUser);

        Role adminRole = mock(Role.class);
        when(adminRole.getId()).thenReturn(ADMIN_ROLE_ID);

        when(roles.findByTenantIdAndCode(
                TENANT_ID, "ROLE_ADMIN"
        )).thenReturn(Optional.of(adminRole));

        RegistrationResult result = service.register(form);

        assertEquals(TENANT_ID, result.getTenantId());
        assertEquals(USER_ID, result.getUserId());
        assertEquals(
                "tenant-di-test",
                result.getTenantSlug()
        );

        verify(uoms).initializeForTenant(TENANT_ID);

        verify(companyBootstrap).initializeCompany(
                TENANT_ID, COMPANY_ID
        );

        InOrder order = inOrder(
                roleBootstrap,
                roles,
                userRoles
        );

        order.verify(roleBootstrap)
                .initializeForTenant(TENANT_ID);

        order.verify(roles)
                .findByTenantIdAndCode(
                        TENANT_ID, "ROLE_ADMIN"
                );

        ArgumentCaptor<UserRole> assignment =
                ArgumentCaptor.forClass(UserRole.class);

        order.verify(userRoles)
                .save(assignment.capture());

        UserRole savedAssignment = assignment.getValue();

        assertEquals(
                TENANT_ID,
                savedAssignment.getTenantId()
        );

        assertEquals(
                USER_ID,
                savedAssignment.getUserId()
        );

        assertEquals(
                ADMIN_ROLE_ID,
                savedAssignment.getRoleId()
        );

        verify(userRoles, times(1))
                .save(any(UserRole.class));

        System.out.println(
                "REGISTRAZIONE RUOLI OK: bootstrap prima "
                + "dell'assegnazione ROLE_ADMIN."
        );
    }
}
