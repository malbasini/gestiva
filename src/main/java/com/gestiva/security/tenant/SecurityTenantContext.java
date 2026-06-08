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
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            throw new IllegalStateException("Utente non autenticato");
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof AuthenticatedUser authenticatedUser)) {
            throw new IllegalStateException("Principal non valido");
        }
        return authenticatedUser.getTenantId();
    }

    @Override
    public Long getCurrentTenantIdOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof AuthenticatedUser user)) {
            return null;
        }

        return user.getTenantId();
    }
    public AuthenticatedUser getCurrentUserOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof AuthenticatedUser user)) {
            return null;
        }

        return user;
    }

}