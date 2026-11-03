package com.gestiva.admin.users.service;

import com.gestiva.platform.company.repository.CompanyRepository;
import com.gestiva.admin.users.web.AdminUserEditForm;
import com.gestiva.common.exception.BusinessException;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AdminUserServiceSecurityTest {

    private AppUserRepository appUserRepository;
    private UserRoleRepository userRoleRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;

    private AdminUserService service;

    private static final Long TENANT_ID = 2L;
    private static final Long OTHER_TENANT_ID = 3L;
    private static final Long USER_ID = 100L;

    @BeforeEach
    void setup() {
        appUserRepository = mock(AppUserRepository.class);
        userRoleRepository = mock(UserRoleRepository.class);
        roleRepository = mock(RoleRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);

        service = new AdminUserService(
                appUserRepository,
                userRoleRepository,
                roleRepository,
                passwordEncoder,
                mock(AdminUserCompanyAssignmentService.class),
                org.mockito.Mockito.mock(CompanyRepository.class)
        );

        AppUser savedUser = mock(AppUser.class);
        when(savedUser.getId()).thenReturn(USER_ID);

        when(appUserRepository.save(any(AppUser.class)))
                .thenReturn(savedUser);

        when(passwordEncoder.encode(anyString()))
                .thenReturn("HASH_TEST");
    }

    private AdminUserEditForm form(List<String> roles) {
        AdminUserEditForm form = new AdminUserEditForm();

        form.setFirstName("Utente");
        form.setLastName("Test");
        form.setEmail("utente-test@example.invalid");
        form.setPassword("PasswordSoloTest123!");
        form.setActive(true);
        form.setAdmin(false);
        form.setRoles(roles);

        return form;
    }

    private Role role(Long tenantId, Long id, String name) {
        Role role = mock(Role.class);

        when(role.getTenantId()).thenReturn(tenantId);
        when(role.getId()).thenReturn(id);
        when(role.getCode()).thenReturn(name);

        return role;
    }

    @Test
    void shouldAssignMultipleRolesFromSameTenant() {

        Role sales = role(
                TENANT_ID, 101L, "ROLE_SALES"
        );

        Role warehouse = role(
                TENANT_ID, 102L, "ROLE_WAREHOUSE"
        );

        when(roleRepository.findByTenantIdAndCodeInOrderByCodeAsc(
                eq(TENANT_ID), anyCollection()
        )).thenReturn(List.of(sales, warehouse));

        service.createUser(
                TENANT_ID,
                form(List.of("ROLE_SALES", "ROLE_WAREHOUSE"))
        );

        verify(userRoleRepository, times(2))
                .save(any(UserRole.class));
    }

    @Test
    void shouldRejectRoleBelongingToAnotherTenant() {

        Role foreignRole = role(
                OTHER_TENANT_ID, 201L, "ROLE_SALES"
        );

        when(roleRepository.findByTenantIdAndCodeInOrderByCodeAsc(
                eq(TENANT_ID), anyCollection()
        )).thenReturn(List.of(foreignRole));

        assertThrows(
                BusinessException.class,
                () -> service.createUser(
                        TENANT_ID,
                        form(List.of("ROLE_SALES"))
                )
        );

        verify(userRoleRepository, never())
                .save(any(UserRole.class));
    }


    @Test
    void shouldRejectIncompleteRoleAssignment() {

        // L'amministratore richiede due ruoli.
        // Il repository ne restituisce soltanto uno.
        Role sales = role(
                TENANT_ID,
                101L,
                "ROLE_SALES"
        );

        when(roleRepository
                .findByTenantIdAndCodeInOrderByCodeAsc(
                        eq(TENANT_ID),
                        anyCollection()
                ))
                .thenReturn(List.of(sales));

        assertThrows(
                BusinessException.class,
                () -> service.createUser(
                        TENANT_ID,
                        form(List.of(
                                "ROLE_SALES",
                                "ROLE_WAREHOUSE"
                        ))
                ),
                "L'assenza di un ruolo richiesto deve bloccare l'operazione"
        );

        verify(userRoleRepository, never())
                .save(any(UserRole.class));
    }



    @Test
    void shouldLoadRoleCodesWhenEditingUser() {

        AppUser user = mock(AppUser.class);

        when(user.getId()).thenReturn(USER_ID);
        when(user.getStatus()).thenReturn("ACTIVE");
        when(user.getEmail())
                .thenReturn("utente-test@example.invalid");

        when(appUserRepository.findByTenantIdAndId(
                TENANT_ID, USER_ID
        )).thenReturn(java.util.Optional.of(user));

        Role sales = role(
                TENANT_ID, 101L, "ROLE_SALES"
        );

        Role warehouse = role(
                TENANT_ID, 102L, "ROLE_WAREHOUSE"
        );

        // Il nome descrittivo è diverso dal codice tecnico.
        when(sales.getName()).thenReturn("Vendite");
        when(warehouse.getName()).thenReturn("Magazzino");

        UserRole salesAssignment = mock(UserRole.class);
        when(salesAssignment.getRoleId()).thenReturn(101L);

        UserRole warehouseAssignment = mock(UserRole.class);
        when(warehouseAssignment.getRoleId()).thenReturn(102L);

        when(userRoleRepository.findByTenantIdAndUserId(
                TENANT_ID, USER_ID
        )).thenReturn(List.of(
                salesAssignment, warehouseAssignment
        ));

        when(roleRepository.findByTenantId(TENANT_ID))
                .thenReturn(List.of(sales, warehouse));

        AdminUserEditForm result = service.loadEditForm(
                TENANT_ID, USER_ID
        );

        assertEquals(
                java.util.Set.of(
                        "ROLE_SALES",
                        "ROLE_WAREHOUSE"
                ),
                new java.util.HashSet<>(result.getRoles()),
                "Il form deve utilizzare Role.code, non Role.name"
        );
    }

}
