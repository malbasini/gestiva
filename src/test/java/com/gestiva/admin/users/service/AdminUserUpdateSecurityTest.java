package com.gestiva.admin.users.service;

import com.gestiva.platform.company.repository.CompanyRepository;
import com.gestiva.admin.users.web.AdminUserEditForm;
import com.gestiva.security.role.entity.UserRole;
import com.gestiva.security.role.repository.RoleRepository;
import com.gestiva.security.role.repository.UserRoleRepository;
import com.gestiva.security.tenant.entity.AppUser;
import com.gestiva.security.tenant.repository.AppUserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

class AdminUserUpdateSecurityTest {

    private static final Long TENANT_ID = 2L;
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
    }

    @Test
    void shouldDeletePreviousUserRolesOnlyOnce() {

        AppUser user = mock(AppUser.class);

        when(user.getId()).thenReturn(USER_ID);

        when(appUserRepository.findByTenantIdAndId(
                TENANT_ID, USER_ID
        )).thenReturn(Optional.of(user));

        // Due associazioni precedentemente assegnate
        // allo stesso utente.
        UserRole sales = mock(UserRole.class);
        UserRole warehouse = mock(UserRole.class);

        when(sales.getUserId()).thenReturn(USER_ID);
        when(warehouse.getUserId()).thenReturn(USER_ID);

        when(userRoleRepository.findByTenantIdAndUserId(
                TENANT_ID, USER_ID
        )).thenReturn(List.of(sales, warehouse));

        // In questa prova l'amministratore deseleziona
        // tutti i ruoli. Non sono necessarie nuove associazioni.
        when(roleRepository
                .findByTenantIdAndCodeInOrderByCodeAsc(
                        eq(TENANT_ID),
                        anyCollection()
                ))
                .thenReturn(List.of());

        AdminUserEditForm form = new AdminUserEditForm();

        form.setId(USER_ID);
        form.setFirstName("Utente");
        form.setLastName("Test");
        form.setEmail("utente@example.invalid");
        form.setActive(true);
        form.setAdmin(false);
        form.setRoles(List.of());

        service.updateUser(TENANT_ID, form);

        // La sostituzione delle associazioni deve
        // effettuare una sola cancellazione.
        verify(userRoleRepository, times(1))
                .deleteByTenantIdAndUserId(
                        TENANT_ID, USER_ID
                );

        // Non devono verificarsi altre cancellazioni.
        verify(userRoleRepository, times(1))
                .deleteByTenantIdAndUserId(
                        anyLong(), anyLong()
                );

        // Nessuna nuova associazione: ruoli deselezionati.
        verify(userRoleRepository, never())
                .save(any(UserRole.class));
    }
}
