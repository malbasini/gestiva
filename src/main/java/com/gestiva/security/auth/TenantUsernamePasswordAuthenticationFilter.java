package com.gestiva.security.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class TenantUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    @Override
    protected String obtainUsername(HttpServletRequest request) {
        String tenantSlug = request.getParameter("tenantSlug");
        String email = request.getParameter("email");

        String safeTenant = tenantSlug == null ? "" : tenantSlug.trim().toLowerCase();
        String safeEmail = email == null ? "" : email.trim().toLowerCase();

        return safeTenant + "|" + safeEmail;
    }

    public void setAuthenticationFailureUrl(String s) {
    }
}
