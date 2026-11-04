package com.gestiva.billing.subscription.web;

import com.gestiva.security.tenant.entity.Tenant;
import com.gestiva.security.tenant.repository.TenantRepository;
import com.gestiva.security.usercontext.TenantContext;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TenantSubscriptionPlanSecurityTest {

    private final TenantContext tenantContext =
            mock(TenantContext.class);

    private final TenantRepository tenantRepository =
            mock(TenantRepository.class);

    private final TenantSubscriptionInterceptor interceptor =
            new TenantSubscriptionInterceptor(
                    tenantContext,
                    tenantRepository
            );

    private void configureActiveTenant(String plan) {

        Tenant tenant = mock(Tenant.class);

        when(tenant.isSubscriptionActive()).thenReturn(true);
        when(tenant.getSubscriptionPlan()).thenReturn(plan);

        when(tenantContext.getCurrentTenantIdOrNull())
                .thenReturn(2L);

        when(tenantRepository.findById(2L))
                .thenReturn(Optional.of(tenant));
    }

    private void assertAllowed(String path) throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest("GET", path);

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(
                request,
                response,
                new Object()
        );

        assertTrue(allowed);
        assertEquals(200, response.getStatus());
        assertNull(response.getRedirectedUrl());

        System.out.println("CONSENTITO: " + path);
    }

    private void assertRedirected(String path) throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest("GET", path);

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(
                request,
                response,
                new Object()
        );

        assertFalse(allowed);
        assertEquals(302, response.getStatus());
        assertEquals("/pricing", response.getRedirectedUrl());

        System.out.println("BLOCCATO: " + path + " -> /pricing");
    }

    @Test
    void starterMustNotAccessInventory() throws Exception {

        configureActiveTenant("STARTER");

        assertRedirected("/inventory");
    }

    @Test
    void starterMustNotAccessPayments() throws Exception {

        configureActiveTenant("STARTER");

        assertRedirected("/payments");
    }

    @Test
    void starterMayAccessPurchaseOrders() throws Exception {

        configureActiveTenant("STARTER");

        assertAllowed("/purchase-orders");
    }

    @Test
    void businessMayAccessInventory() throws Exception {

        configureActiveTenant("BUSINESS");

        assertAllowed("/inventory");
    }
}
