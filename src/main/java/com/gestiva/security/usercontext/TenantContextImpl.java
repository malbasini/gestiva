package com.gestiva.security.usercontext;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class TenantContextImpl implements TenantContext {

    private final HttpServletRequest request;

    private TenantContextImpl(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public Long getCurrentTenantId() {
        String tenantHeader = request.getHeader("X-Tenant-Id");
        if (tenantHeader == null || tenantHeader.isBlank()) {
            throw new IllegalStateException("Missing X-Tenant-Id header");
        }
        return Long.valueOf(tenantHeader);
    }
}