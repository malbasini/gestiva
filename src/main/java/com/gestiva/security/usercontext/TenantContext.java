package com.gestiva.security.usercontext;

import com.gestiva.security.auth.AuthenticatedUser;

public interface TenantContext {
    Long getCurrentTenantId();

    Long getCurrentTenantIdOrNull();

    AuthenticatedUser getCurrentUserOrNull();
}