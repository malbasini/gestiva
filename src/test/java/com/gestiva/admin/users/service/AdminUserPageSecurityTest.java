package com.gestiva.admin.users.service;

import com.gestiva.platform.company.repository.CompanyRepository;
import com.gestiva.admin.users.web.AdminUserPageView;
import com.gestiva.admin.users.web.AdminUserRowView;
import com.gestiva.security.role.entity.Role;
import com.gestiva.security.role.entity.UserRole;
import com.gestiva.security.role.repository.RoleRepository;
import com.gestiva.security.role.repository.UserRoleRepository;
import com.gestiva.security.tenant.entity.AppUser;
import com.gestiva.security.tenant.repository.AppUserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminUserPageSecurityTest {

    private static final Long TENANT_ID = 2L;
    private static final Long OTHER_TENANT_ID = 3L;
    private static final Long USER_ID = 100L;

    private AppUserRepository appUserRepository;
    private UserRoleRepository userRoleRepository;
    private RoleRepository roleRepository;

    private AdminUserService service;

    @BeforeEach
    void setup() {

        appUserRepository = mock(AppUserRepository.class);
        userRoleRepository = mock(UserRoleRepository.class);
        roleRepository = mock(RoleRepository.class);

        service = new AdminUserService(
                appUserRepository,
                userRoleRepository,
                roleRepository,
                mock(PasswordEncoder.class),
                mock(AdminUserCompanyAssignmentService.class),
                org.mockito.Mockito.mock(CompanyRepository.class)
        );

        AppUser user = mock(AppUser.class);

        when(user.getId()).thenReturn(USER_ID);
        when(user.getFirstName()).thenReturn("Utente");
        when(user.getLastName()).thenReturn("Test");
        when(user.getEmail()).thenReturn(
                "utente@example.invalid"
        );
        when(user.getStatus()).thenReturn("ACTIVE");

        when(appUserRepository
                .findByTenantIdOrderByLastNameAscFirstNameAsc(
                        TENANT_ID
                ))
                .thenReturn(List.of(user));
    }

    private Role role(
            Long id,
            Long tenantId,
            String code,
            String name
    ) {

        Role result = mock(Role.class);

        when(result.getId()).thenReturn(id);
        when(result.getTenantId()).thenReturn(tenantId);
        when(result.getCode()).thenReturn(code);
        when(result.getName()).thenReturn(name);

        return result;
    }

    private void assignRole(Long roleId) {

        UserRole assignment = mock(UserRole.class);

        when(assignment.getUserId()).thenReturn(USER_ID);
        when(assignment.getRoleId()).thenReturn(roleId);

        when(userRoleRepository.findByTenantId(TENANT_ID))
                .thenReturn(List.of(assignment));
    }

    private AdminUserRowView loadSingleRow() {

        AdminUserPageView page = service.buildPage(TENANT_ID);

        assertEquals(
                1,
                page.getUsers().size(),
                "Deve essere presente un solo utente"
        );

        return page.getUsers().get(0);
    }

    @Test
    void shouldRecognizeAdminByCodeNotDescription() {

        Long roleId = 201L;

        Role adminRole = role(
                roleId,
                TENANT_ID,
                "ROLE_ADMIN",
                "Administrator"
        );

        assignRole(roleId);

        when(roleRepository.findByTenantId(TENANT_ID))
                .thenReturn(List.of(adminRole));

        AdminUserRowView row = loadSingleRow();

        assertTrue(
                row.isAdmin(),
                "ROLE_ADMIN deve identificare l'amministratore "
                + "indipendentemente dal nome descrittivo"
        );

        assertTrue(row.getRoles().contains("Administrator"));
    }

    @Test
    void shouldNotDisplayRoleFromAnotherTenant() {

        Long foreignRoleId = 301L;

        Role foreignRole = role(
                foreignRoleId,
                OTHER_TENANT_ID,
                "ROLE_ACCOUNTING",
                "Contabilita tenant esterno"
        );

        // Simuliamo un'associazione incoerente:
        // l'utente del tenant corrente punta a un ruolo esterno.
        assignRole(foreignRoleId);

        when(roleRepository.findByTenantId(TENANT_ID))
                .thenReturn(List.of(foreignRole));

        AdminUserRowView row = loadSingleRow();

        assertFalse(
                row.getRoles().contains(
                        "Contabilita tenant esterno"
                ),
                "L'elenco non deve mostrare ruoli "
                + "appartenenti ad altri tenant"
        );
    }
}
