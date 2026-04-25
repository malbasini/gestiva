package com.gestiva.security.usercontext;

import org.springframework.stereotype.Component;

@Component
public class TenantContextImpl implements TenantContext {

    @Override
    public Long getCurrentTenantId() {
        // TODO recuperare da JWT/sessione/security context
        return 1L;
    }
}