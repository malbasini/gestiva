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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminUserAdminConsistencyTest {

    private static final Long TENANT_ID = 2L;
    private static final Long USER_ID = 100L;
    private static final Long ADMIN_ROLE_ID = 200L;

    @Test
    void shouldRecognizeAdminRoleWhenLoadingEditForm() {

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
        when(user.getStatus()).thenReturn("ACTIVE");
        when(user.isAdmin()).thenReturn(false);

        when(appUsers.findByTenantIdAndId(
                TENANT_ID, USER_ID
        )).thenReturn(Optional.of(user));

        Role adminRole = mock(Role.class);

        when(adminRole.getId()).thenReturn(ADMIN_ROLE_ID);
        when(adminRole.getCode()).thenReturn("ROLE_ADMIN");

        when(roles.findByTenantId(TENANT_ID))
                .thenReturn(List.of(adminRole));

        UserRole assignment = mock(UserRole.class);

        when(assignment.getRoleId())
                .thenReturn(ADMIN_ROLE_ID);

        when(userRoles.findByTenantIdAndUserId(
                TENANT_ID, USER_ID
        )).thenReturn(List.of(assignment));

        AdminUserEditForm form = service.loadEditForm(
                TENANT_ID, USER_ID
        );

        assertTrue(
                form.isAdmin(),
                "L'utente con ROLE_ADMIN deve risultare "
                + "amministratore anche quando il flag "
                + "AppUser.admin è false"
        );
    }
}
