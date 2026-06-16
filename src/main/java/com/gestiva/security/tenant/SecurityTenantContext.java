package com.gestiva.security.tenant;

import com.gestiva.security.auth.AuthenticatedUser;
import com.gestiva.security.usercontext.TenantContext;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@Primary
public class SecurityTenantContext implements TenantContext {

    @Override
    public Long getCurrentTenantId() {
        AuthenticatedUser user = getAuthenticatedUserOrNull();

        if (user == null) {
            throw new IllegalStateException("Utente non autenticato");
        }

        return user.getTenantId();
    }

    @Override
    public Long getCurrentTenantIdOrNull() {
        AuthenticatedUser user = getAuthenticatedUserOrNull();
        return user != null ? user.getTenantId() : null;
    }

    public AuthenticatedUser getCurrentUserOrNull() {
        return getAuthenticatedUserOrNull();
    }

    private AuthenticatedUser getAuthenticatedUserOrNull() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof AuthenticatedUser authenticatedUser)) {
            return null;
        }

        return authenticatedUser;
    }
}