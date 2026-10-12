package com.gestiva.security.company.service;

import com.gestiva.platform.company.entity.Company;
import com.gestiva.platform.company.repository.CompanyRepository;
import com.gestiva.security.auth.AuthenticatedUser;
import com.gestiva.security.company.repository.UserCompanyAccessRepository;
import com.gestiva.security.tenant.entity.AppUser;
import com.gestiva.security.tenant.repository.AppUserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CompanyAccessService {

    private final AppUserRepository appUserRepository;
    private final CompanyRepository companyRepository;
    private final UserCompanyAccessRepository userCompanyAccessRepository;

    public CompanyAccessService(
            AppUserRepository appUserRepository,
            CompanyRepository companyRepository,
            UserCompanyAccessRepository userCompanyAccessRepository) {

        this.appUserRepository = appUserRepository;
        this.companyRepository = companyRepository;
        this.userCompanyAccessRepository = userCompanyAccessRepository;
    }

    public Company requireAccess(Long companyId) {

        if (companyId == null) {
            throw new AccessDeniedException("Società non specificata");
        }

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken
                || !(authentication.getPrincipal() instanceof AuthenticatedUser)) {

            throw new AccessDeniedException("Utente non autenticato");
        }

        AuthenticatedUser authenticatedUser =
                (AuthenticatedUser) authentication.getPrincipal();

        Long tenantId = authenticatedUser.getTenantId();
        Long userId = authenticatedUser.getUserId();

        if (tenantId == null || userId == null) {
            throw new AccessDeniedException("Contesto utente non valido");
        }

        AppUser user = appUserRepository
                .findByTenantIdAndId(tenantId, userId)
                .orElseThrow(() ->
                        new AccessDeniedException("Utente non autorizzato"));

        if (!"ACTIVE".equals(user.getStatus())) {
            throw new AccessDeniedException("Utente non attivo");
        }

        Company company = companyRepository
                .findByTenantIdAndId(tenantId, companyId)
                .orElseThrow(() ->
                        new AccessDeniedException("Società non autorizzata"));

        if (!company.isActive()) {
            throw new AccessDeniedException("Società non attiva");
        }

        // L'amministratore del tenant può accedere
        // a tutte le società attive del proprio tenant.
        if (user.isAdmin()) {
            return company;
        }

        // Gli altri utenti devono avere un'autorizzazione esplicita.
        boolean authorized = userCompanyAccessRepository
                .existsByTenantIdAndUserIdAndCompanyId(
                        tenantId,
                        userId,
                        companyId
                );

        if (!authorized) {
            throw new AccessDeniedException(
                    "Accesso alla società non consentito"
            );
        }

        return company;
    }
}