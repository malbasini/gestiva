package com.gestiva.security.service;

import com.gestiva.platform.tenant.repository.AppUserRepository;
import com.gestiva.security.auth.AuthenticatedUser;
import com.gestiva.security.tenant.repository.TenantRepository;
import com.gestiva.security.web.CurrentUserView;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CurrentUserService {

    private final TenantRepository tenantRepository;
    private final AppUserRepository appUserRepository;

    public CurrentUserService(TenantRepository tenantRepository,
                              AppUserRepository appUserRepository) {
        this.tenantRepository = tenantRepository;
        this.appUserRepository = appUserRepository;
    }

    public CurrentUserView getCurrentUserView() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken ||
                !(authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser)) {
            return null;
        }

        var tenant = tenantRepository.findById(authenticatedUser.getTenantId()).orElse(null);
        var user = appUserRepository.findById(authenticatedUser.getUserId()).orElse(null);

        if (tenant == null || user == null) {
            return null;
        }

        CurrentUserView view = new CurrentUserView();
        view.setTenantName(tenant.getName());
        view.setTenantSlug(tenant.getSlug());
        view.setFullName(user.getFirstName() + " " + user.getLastName());
        view.setEmail(user.getEmail());
        return view;
    }
}
