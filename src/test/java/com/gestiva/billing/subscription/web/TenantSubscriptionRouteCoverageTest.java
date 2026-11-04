package com.gestiva.billing.subscription.web;

import com.gestiva.security.tenant.repository.TenantRepository;
import com.gestiva.security.usercontext.TenantContext;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TenantSubscriptionRouteCoverageTest {

    private final TenantContext tenantContext =
            mock(TenantContext.class);

    private final TenantRepository tenantRepository =
            mock(TenantRepository.class);

    private final TenantSubscriptionInterceptor interceptor =
            new TenantSubscriptionInterceptor(
                    tenantContext,
                    tenantRepository
            );

    private void assertInactiveTenantBlocked(String path)
            throws Exception {

        reset(tenantContext, tenantRepository);

        when(tenantContext.getCurrentTenantIdOrNull())
                .thenReturn(2L);

        when(tenantRepository.findById(2L))
                .thenReturn(Optional.empty());

        var request =
                new MockHttpServletRequest("GET", path);

        var response =
                new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(
                request,
                response,
                new Object()
        );

        assertFalse(allowed);
        assertEquals(302, response.getStatus());
        assertEquals("/pricing", response.getRedirectedUrl());

        verify(tenantRepository).findById(2L);

        System.out.println(
                "ABBONAMENTO INATTIVO BLOCCATO: " + path
        );
    }

    private void assertPublic(String path) throws Exception {

        reset(tenantContext, tenantRepository);

        var request =
                new MockHttpServletRequest("GET", path);

        var response =
                new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(
                request,
                response,
                new Object()
        );

        assertTrue(allowed);
        assertEquals(200, response.getStatus());

        verifyNoInteractions(
                tenantContext,
                tenantRepository
        );

        System.out.println(
                "PERCORSO PUBBLICO CONSENTITO: " + path
        );
    }

    @Test
    void inactiveTenantCannotAccessDashboard()
            throws Exception {

        assertInactiveTenantBlocked("/dashboard");
    }

    @Test
    void inactiveTenantCannotAccessInvoiceApi()
            throws Exception {

        assertInactiveTenantBlocked("/api/invoices/1/pdf");
    }

    @Test
    void inactiveTenantCannotAccessCompanySelection()
            throws Exception {

        assertInactiveTenantBlocked("/company/select");
    }

    @Test
    void loginRemainsPublic() throws Exception {

        assertPublic("/login");
    }

    @Test
    void pricingRemainsPublic() throws Exception {

        assertPublic("/pricing");
    }

    @Test
    void stylesRemainPublic() throws Exception {

        assertPublic("/css/app.css");
    }
}
