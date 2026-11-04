package com.gestiva.billing.subscription.web;

import com.gestiva.security.tenant.repository.TenantRepository;
import com.gestiva.security.usercontext.TenantContext;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TenantSubscriptionPublicRoutesTest {

    private final TenantContext tenantContext =
            mock(TenantContext.class);

    private final TenantRepository tenantRepository =
            mock(TenantRepository.class);

    private final TenantSubscriptionInterceptor interceptor =
            new TenantSubscriptionInterceptor(
                    tenantContext,
                    tenantRepository
            );

    private void verifyPublicRoute(String path) throws Exception {

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

        System.out.println(
                "PUBLIC ROUTE: " + path
                + " | allowed=" + allowed
                + " | HTTP=" + response.getStatus()
        );

        assertTrue(
                allowed,
                "Il percorso pubblico deve rimanere accessibile: "
                + path
        );

        assertEquals(200, response.getStatus());
        assertNull(response.getRedirectedUrl());
    }

    @Test
    void homeMustRemainPublic() throws Exception {
        verifyPublicRoute("/");
    }

    @Test
    void productsMustRemainPublic() throws Exception {
        verifyPublicRoute("/products");
    }

    @Test
    void helpChatMustRemainPublic() throws Exception {
        verifyPublicRoute("/help/chat");
    }

    @Test
    void actuatorHealthMustRemainPublic() throws Exception {
        verifyPublicRoute("/actuator/health");
    }

    @Test
    void protectedInventoryMustRemainBlocked() throws Exception {

        reset(tenantContext, tenantRepository);

        when(tenantContext.getCurrentTenantIdOrNull())
                .thenReturn(2L);

        when(tenantRepository.findById(2L))
                .thenReturn(Optional.empty());

        var request =
                new MockHttpServletRequest("GET", "/inventory");

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

        System.out.println(
                "PROTECTED ROUTE: /inventory | HTTP=302"
        );
    }
}
