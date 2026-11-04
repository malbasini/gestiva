package com.gestiva.security.company.service;

import com.gestiva.platform.company.entity.Company;
import com.gestiva.platform.company.repository.CompanyRepository;
import com.gestiva.security.auth.AuthenticatedUser;
import com.gestiva.security.company.repository.UserCompanyAccessRepository;
import com.gestiva.security.tenant.entity.AppUser;
import com.gestiva.security.tenant.repository.AppUserRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CompanyAccessServiceAuthorizationTest {

    private static final Long TENANT = 2L;
    private static final Long USER = 100L;

    private final AppUserRepository users =
            mock(AppUserRepository.class);

    private final CompanyRepository companies =
            mock(CompanyRepository.class);

    private final UserCompanyAccessRepository accesses =
            mock(UserCompanyAccessRepository.class);

    private final CompanyAccessService service =
            new CompanyAccessService(
                    users,
                    companies,
                    accesses
            );

    @AfterEach
    void cleanupSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateOrdinaryUser() {

        var authorities = List.of(
                new SimpleGrantedAuthority("ROLE_SALES"),
                new SimpleGrantedAuthority("ROLE_WAREHOUSE")
        );

        AuthenticatedUser principal = new AuthenticatedUser(
                USER,
                TENANT,
                "tenant-test",
                "collaudo@example.invalid",
                "test-only",
                true,
                authorities
        );

        var authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        authorities
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }

    private Company activeCompany(Long id) {

        Company company = mock(Company.class);

        when(company.getId()).thenReturn(id);
        when(company.isActive()).thenReturn(true);

        when(companies.findByTenantIdAndId(TENANT, id))
                .thenReturn(Optional.of(company));

        return company;
    }

    private void activeUser() {

        AppUser user = mock(AppUser.class);

        when(user.getStatus()).thenReturn("ACTIVE");
        when(user.isAdmin()).thenReturn(false);

        when(users.findByTenantIdAndId(TENANT, USER))
                .thenReturn(Optional.of(user));
    }

    @Test
    void authorizedCompaniesMustBeAccessible() {

        authenticateOrdinaryUser();
        activeUser();

        Company demo = activeCompany(10L);
        Company collaudo = activeCompany(30L);

        when(accesses.existsByTenantIdAndUserIdAndCompanyId(
                TENANT, USER, 10L
        )).thenReturn(true);

        when(accesses.existsByTenantIdAndUserIdAndCompanyId(
                TENANT, USER, 30L
        )).thenReturn(true);

        assertSame(demo, service.requireAccess(10L));
        assertSame(collaudo, service.requireAccess(30L));

        System.out.println(
                "SOCIETÀ AUTORIZZATE OK: accesso consentito."
        );
    }

    @Test
    void revokedCompanyMustBeRejected() {

        authenticateOrdinaryUser();
        activeUser();

        activeCompany(20L);

        when(accesses.existsByTenantIdAndUserIdAndCompanyId(
                TENANT, USER, 20L
        )).thenReturn(false);

        assertThrows(
                AccessDeniedException.class,
                () -> service.requireAccess(20L)
        );

        verify(accesses).existsByTenantIdAndUserIdAndCompanyId(
                TENANT, USER, 20L
        );

        System.out.println(
                "SOCIETÀ REVOCATA OK: accesso negato."
        );
    }

    @Test
    void foreignTenantCompanyMustBeRejected() {

        authenticateOrdinaryUser();
        activeUser();

        // Il repository tenant-scoped non restituisce
        // una società appartenente ad altro tenant.
        when(companies.findByTenantIdAndId(TENANT, 40L))
                .thenReturn(Optional.empty());

        assertThrows(
                AccessDeniedException.class,
                () -> service.requireAccess(40L)
        );

        verify(accesses, never())
                .existsByTenantIdAndUserIdAndCompanyId(
                        anyLong(), anyLong(), eq(40L)
                );

        System.out.println(
                "ISOLAMENTO TENANT OK: accesso negato."
        );
    }

    @Test
    void disabledUserMustBeRejected() {

        authenticateOrdinaryUser();

        AppUser disabled = mock(AppUser.class);
        when(disabled.getStatus()).thenReturn("DISABLED");

        when(users.findByTenantIdAndId(TENANT, USER))
                .thenReturn(Optional.of(disabled));

        assertThrows(
                AccessDeniedException.class,
                () -> service.requireAccess(10L)
        );

        verifyNoInteractions(companies, accesses);

        System.out.println(
                "UTENTE DISABILITATO OK: accesso negato."
        );
    }
}
