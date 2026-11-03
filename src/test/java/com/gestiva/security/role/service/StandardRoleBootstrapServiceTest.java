package com.gestiva.security.role.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.security.role.entity.Role;
import com.gestiva.security.role.repository.RoleRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class StandardRoleBootstrapServiceTest {

    private static final Long TENANT_ID = 2L;

    private RoleRepository repository;
    private StandardRoleBootstrapService service;

    @BeforeEach
    void setup() {
        repository = mock(RoleRepository.class);
        service = new StandardRoleBootstrapService(repository);
    }

    @Test
    void shouldCreateAllFiveStandardRoles() {

        when(repository.findByTenantIdAndCode(
                eq(TENANT_ID), anyString()
        )).thenReturn(Optional.empty());

        service.initializeForTenant(TENANT_ID);

        ArgumentCaptor<Role> captor =
                ArgumentCaptor.forClass(Role.class);

        verify(repository, times(5))
                .save(captor.capture());

        List<Role> saved = captor.getAllValues();

        Set<String> codes = new HashSet<>();

        for (Role role : saved) {
            assertEquals(TENANT_ID, role.getTenantId());
            assertTrue(role.isSystemRole());
            assertNotNull(role.getName());
            assertFalse(role.getName().isBlank());
            codes.add(role.getCode());
        }

        assertEquals(
                Set.of(
                        "ROLE_ADMIN",
                        "ROLE_SALES",
                        "ROLE_PURCHASING",
                        "ROLE_WAREHOUSE",
                        "ROLE_ACCOUNTING"
                ),
                codes
        );
    }

    @Test
    void shouldPreserveExistingRolesWithoutDuplicates() {

        Role existingAdmin = mock(Role.class);

        when(existingAdmin.getTenantId())
                .thenReturn(TENANT_ID);

        when(existingAdmin.getCode())
                .thenReturn("ROLE_ADMIN");

        when(repository.findByTenantIdAndCode(
                TENANT_ID, "ROLE_ADMIN"
        )).thenReturn(Optional.of(existingAdmin));

        service.initializeForTenant(TENANT_ID);

        // ROLE_ADMIN già presente: soltanto quattro
        // ruoli devono essere creati.
        verify(repository, times(4))
                .save(any(Role.class));

        ArgumentCaptor<Role> captor =
                ArgumentCaptor.forClass(Role.class);

        verify(repository, times(4))
                .save(captor.capture());

        assertFalse(
                captor.getAllValues().stream()
                        .anyMatch(role ->
                                "ROLE_ADMIN".equals(role.getCode()))
        );

        // Nessun aggiornamento del ruolo esistente.
        verify(existingAdmin, never())
                .setName(anyString());
    }

    @Test
    void shouldRejectRoleFromAnotherTenant() {

        Role foreignRole = mock(Role.class);

        when(foreignRole.getTenantId())
                .thenReturn(3L);

        when(foreignRole.getCode())
                .thenReturn("ROLE_ADMIN");

        when(repository.findByTenantIdAndCode(
                TENANT_ID, "ROLE_ADMIN"
        )).thenReturn(Optional.of(foreignRole));

        assertThrows(
                BusinessException.class,
                () -> service.initializeForTenant(TENANT_ID)
        );

        verify(repository, never())
                .save(any(Role.class));
    }
}
