package com.gestiva.security.auth;

import com.gestiva.security.permission.repository.PermissionRepository;
import com.gestiva.security.role.entity.Role;
import com.gestiva.security.role.entity.UserRole;
import com.gestiva.security.role.repository.RolePermissionRepository;
import com.gestiva.security.role.repository.RoleRepository;
import com.gestiva.security.role.repository.UserRoleRepository;
import com.gestiva.security.tenant.entity.AppUser;
import com.gestiva.security.tenant.entity.Tenant;
import com.gestiva.security.tenant.repository.AppUserRepository;
import com.gestiva.security.tenant.repository.TenantRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomUserDetailsServiceTenantSecurityTest {

    private static final Long TENANT_ID = 2L;
    private static final Long OTHER_TENANT_ID = 3L;
    private static final Long USER_ID = 100L;
    private static final Long ROLE_ID = 200L;

    private TenantRepository tenantRepository;
    private AppUserRepository appUserRepository;
    private UserRoleRepository userRoleRepository;
    private RoleRepository roleRepository;
    private RolePermissionRepository rolePermissionRepository;
    private PermissionRepository permissionRepository;

    private CustomUserDetailsService service;

    @BeforeEach
    void setup() {

        tenantRepository = mock(TenantRepository.class);
        appUserRepository = mock(AppUserRepository.class);
        userRoleRepository = mock(UserRoleRepository.class);
        roleRepository = mock(RoleRepository.class);
        rolePermissionRepository =
                mock(RolePermissionRepository.class);
        permissionRepository = mock(PermissionRepository.class);

        service = new CustomUserDetailsService(
                tenantRepository,
                appUserRepository,
                userRoleRepository,
                roleRepository,
                rolePermissionRepository,
                permissionRepository
        );

        Tenant tenant = mock(Tenant.class);
        when(tenant.getId()).thenReturn(TENANT_ID);
        when(tenant.getSlug()).thenReturn("demo");

        when(tenantRepository.findBySlug("demo"))
                .thenReturn(Optional.of(tenant));

        AppUser user = mock(AppUser.class);

        when(user.getId()).thenReturn(USER_ID);
        when(user.getStatus()).thenReturn("ACTIVE");
        when(user.getEmail()).thenReturn(
                "test@example.invalid"
        );
        when(user.getPasswordHash()).thenReturn(
                "HASH_TEST"
        );

        when(appUserRepository.findByTenantIdAndEmail(
                TENANT_ID,
                "test@example.invalid"
        )).thenReturn(Optional.of(user));

        UserRole userRole = mock(UserRole.class);

        when(userRole.getRoleId()).thenReturn(ROLE_ID);

        when(userRoleRepository.findByTenantIdAndUserId(
                TENANT_ID,
                USER_ID
        )).thenReturn(List.of(userRole));
    }

    private void configureRole(Long roleTenantId) {

        Role role = mock(Role.class);

        when(role.getId()).thenReturn(ROLE_ID);
        when(role.getTenantId()).thenReturn(roleTenantId);
        when(role.getCode()).thenReturn("ROLE_ACCOUNTING");

        when(roleRepository.findAllById(
                java.util.Set.of(ROLE_ID)
        )).thenReturn(List.of(role));

        when(rolePermissionRepository
                .findByTenantIdAndRoleId(
                        TENANT_ID,
                        ROLE_ID
                )).thenReturn(List.of());
    }

    private boolean hasAccountingRole(UserDetails details) {

        return details.getAuthorities().stream()
                .anyMatch(authority ->
                        "ROLE_ACCOUNTING".equals(
                                authority.getAuthority()
                        )
                );
    }

    @Test
    void shouldLoadRoleBelongingToAuthenticatedTenant() {

        configureRole(TENANT_ID);

        UserDetails details = service.loadUserByUsername(
                "demo|test@example.invalid"
        );

        assertTrue(
                hasAccountingRole(details),
                "Il ruolo del tenant corrente deve essere caricato"
        );
    }

    @Test
    void shouldNotGrantRoleBelongingToAnotherTenant() {

        configureRole(OTHER_TENANT_ID);

        UserDetails details = service.loadUserByUsername(
                "demo|test@example.invalid"
        );

        assertFalse(
                hasAccountingRole(details),
                "SICUREZZA: ruolo di un altro tenant attribuito"
        );
    }
}
