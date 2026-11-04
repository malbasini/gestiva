package com.gestiva.billing.subscription.web;

import com.gestiva.security.tenant.repository.TenantRepository;
import com.gestiva.security.usercontext.TenantContext;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TenantSubscriptionInterceptorSecurityTest {

    private final TenantContext tenantContext =
            mock(TenantContext.class);

    private final TenantRepository tenantRepository =
            mock(TenantRepository.class);

    private final TenantSubscriptionInterceptor interceptor =
            new TenantSubscriptionInterceptor(
                    tenantContext,
                    tenantRepository
            );

    @Test
    void publicLoginMustRemainAccessible() throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest("GET", "/login");

        MockHttpServletResponse response =
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
                "PAGINA PUBBLICA OK: /login accessibile."
        );
    }

    @Test
    void inactiveSubscriptionMustBlockProtectedPage()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/inventory"
                );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        when(tenantContext.getCurrentTenantIdOrNull())
                .thenReturn(2L);

        // Nessun tenant attivo trovato:
        // l'interceptor deve bloccare la richiesta.
        when(tenantRepository.findById(2L))
                .thenReturn(Optional.empty());

        boolean allowed = interceptor.preHandle(
                request,
                response,
                new Object()
        );

        assertFalse(
                allowed,
                "Un tenant senza abbonamento attivo "
                + "non deve accedere a /inventory"
        );

        assertEquals(302, response.getStatus());

        assertEquals(
                "/pricing",
                response.getRedirectedUrl()
        );

        verify(tenantRepository).findById(2L);

        System.out.println(
                "ABBONAMENTO NON ATTIVO OK: accesso bloccato."
        );
    }
}
