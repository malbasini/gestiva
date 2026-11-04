package com.gestiva.billing.subscription.web;

import com.gestiva.security.tenant.repository.TenantRepository;
import com.gestiva.security.usercontext.TenantContext;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TenantSubscriptionAdditionalPublicRoutesTest {

    private final TenantContext tenantContext =
            mock(TenantContext.class);

    private final TenantRepository tenantRepository =
            mock(TenantRepository.class);

    private final TenantSubscriptionInterceptor interceptor =
            new TenantSubscriptionInterceptor(
                    tenantContext,
                    tenantRepository
            );

    private void assertPublic(String path) throws Exception {

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
                "Percorso pubblico bloccato: " + path
        );

        assertEquals(200, response.getStatus());
        assertNull(response.getRedirectedUrl());
    }

    @Test
    void robotsMustBePublic() throws Exception {
        assertPublic("/robots.txt");
    }

    @Test
    void sitemapMustBePublic() throws Exception {
        assertPublic("/sitemap.xml");
    }

    @Test
    void actuatorInfoMustBePublic() throws Exception {
        assertPublic("/actuator/info");
    }

    @Test
    void contactMustBePublic() throws Exception {
        assertPublic("/contact");
    }

    @Test
    void registrationMustBePublic() throws Exception {
        assertPublic("/register");
    }

    @Test
    void faviconMustBePublic() throws Exception {
        assertPublic("/favicon.svg");
    }
}
