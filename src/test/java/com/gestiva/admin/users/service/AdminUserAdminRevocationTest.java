package com.gestiva.admin.users.service;

import com.gestiva.platform.company.repository.CompanyRepository;
import com.gestiva.admin.users.web.AdminUserEditForm;
import com.gestiva.security.role.entity.Role;
import com.gestiva.security.role.entity.UserRole;
import com.gestiva.security.role.repository.RoleRepository;
import com.gestiva.security.role.repository.UserRoleRepository;
import com.gestiva.security.tenant.entity.AppUser;
import com.gestiva.security.tenant.repository.AppUserRepository;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AdminUserAdminRevocationTest {

    private static final Long TENANT_ID = 2L;
    private static final Long USER_ID = 100L;
    private static final Long SALES_ROLE_ID = 201L;

    @Test
    void shouldRevokeAdminAndPreserveSalesRole() {

        AppUserRepository appUsers =
                mock(AppUserRepository.class);

        UserRoleRepository userRoles =
                mock(UserRoleRepository.class);

        RoleRepository roles =
                mock(RoleRepository.class);

        AdminUserService service = new AdminUserService(
                appUsers,
                userRoles,
                roles,
                mock(PasswordEncoder.class),
                mock(AdminUserCompanyAssignmentService.class),
                org.mockito.Mockito.mock(CompanyRepository.class)
        );

        AppUser user = mock(AppUser.class);

        when(user.getId()).thenReturn(USER_ID);

        when(appUsers.findByTenantIdAndId(
                TENANT_ID, USER_ID
        )).thenReturn(Optional.of(user));

        // L'unico ruolo da mantenere è Vendite.
        Role salesRole = mock(Role.class);

        when(salesRole.getId())
                .thenReturn(SALES_ROLE_ID);

        when(salesRole.getTenantId())
                .thenReturn(TENANT_ID);

        when(salesRole.getCode())
                .thenReturn("ROLE_SALES");

        when(roles.findByTenantIdAndCodeInOrderByCodeAsc(
                eq(TENANT_ID),
                anyCollection()
        )).thenReturn(List.of(salesRole));

        UserRole adminAccess = new UserRole();
        adminAccess.setTenantId(TENANT_ID);
        adminAccess.setUserId(USER_ID);
        adminAccess.setRoleId(200L);

        UserRole salesAccess = new UserRole();
        salesAccess.setTenantId(TENANT_ID);
        salesAccess.setUserId(USER_ID);
        salesAccess.setRoleId(SALES_ROLE_ID);

        when(userRoles.findByTenantIdAndUserId(
                TENANT_ID, USER_ID
        )).thenReturn(List.of(adminAccess, salesAccess));

        AdminUserEditForm form = new AdminUserEditForm();

        form.setId(USER_ID);
        form.setFirstName("Utente");
        form.setLastName("Test");
        form.setEmail("utente@example.invalid");
        form.setActive(true);

        // Revoca dell'amministratore.
        form.setAdmin(false);

        // Conserviamo il ruolo Vendite.
        form.setRoles(List.of("ROLE_SALES"));

        service.updateUser(TENANT_ID, form);

        // Il flag amministrativo deve essere disattivato.
        verify(user).setAdmin(false);

        verify(appUsers).save(user);

        // Revoca soltanto ROLE_ADMIN.
        verify(userRoles).findByTenantIdAndUserId(
                TENANT_ID, USER_ID
        );
        verify(userRoles).deleteAll(List.of(adminAccess));

        // Il ruolo Vendite già assegnato viene conservato.
        verify(userRoles, never()).save(any(UserRole.class));

        // Verifichiamo che ROLE_ADMIN non sia stato richiesto
        // durante la nuova assegnazione.
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<String>> requestedCodes =
                ArgumentCaptor.forClass(Collection.class);

        verify(roles)
                .findByTenantIdAndCodeInOrderByCodeAsc(
                        eq(TENANT_ID),
                        requestedCodes.capture()
                );

        assertEquals(
                Set.of("ROLE_SALES"),
                new HashSet<>(requestedCodes.getValue()),
                "La revoca non deve mantenere ROLE_ADMIN"
        );

        verifyNoMoreInteractions(userRoles);
    }
}
