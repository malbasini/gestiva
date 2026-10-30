package com.gestiva.config;

import com.gestiva.security.auth.CustomUserDetailsService;
import com.gestiva.security.auth.TenantUsernamePasswordAuthenticationFilter;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SupplierInvoiceEndpointAuthorizationTest {

    private static final String INVOICE_PATH =
            "/goods-receipts/1/invoice";

    private static final String RECEIPT_PATH =
            "/goods-receipts/1";

    private void verifyRequest(
            String role,
            String method,
            String path,
            boolean includeCsrf,
            boolean expectedAllowed
    ) throws Exception {

        CustomUserDetailsService userDetailsService =
                mock(CustomUserDetailsService.class);

        DataSource dataSource = mock(DataSource.class);

        AuthenticationProvider authenticationProvider =
                mock(AuthenticationProvider.class);

        RememberMeServices rememberMeServices =
                mock(RememberMeServices.class);

        SecurityConfig securityConfig =
                new SecurityConfig(userDetailsService, dataSource);

        TenantUsernamePasswordAuthenticationFilter loginFilter =
                securityConfig.tenantUsernamePasswordAuthenticationFilter(
                        authentication -> authentication,
                        rememberMeServices
                );

        ObjectPostProcessor<Object> postProcessor =
                new ObjectPostProcessor<>() {
                    @Override
                    public <O> O postProcess(O object) {
                        return object;
                    }
                };

        AuthenticationManagerBuilder authenticationBuilder =
                new AuthenticationManagerBuilder(postProcessor);

        StaticApplicationContext applicationContext =
                new StaticApplicationContext();

        applicationContext.refresh();

        try {
            HashMap<Class<?>, Object> sharedObjects =
                    new HashMap<>();

            sharedObjects.put(
                    org.springframework.context.ApplicationContext.class,
                    applicationContext
            );

            HttpSecurity http = new HttpSecurity(
                    postProcessor,
                    authenticationBuilder,
                    sharedObjects
            );

            SecurityContextRepository securityContextRepository =
                    new HttpSessionSecurityContextRepository();

            http.securityContext(securityContext ->
                    securityContext.securityContextRepository(
                            securityContextRepository
                    )
            );

            SecurityFilterChain securityFilterChain =
                    securityConfig.securityFilterChain(
                            http,
                            loginFilter,
                            authenticationProvider
                    );

            FilterChainProxy proxy =
                    new FilterChainProxy(securityFilterChain);

            MockHttpServletRequest request =
                    new MockHttpServletRequest(method, path);

            request.setServletPath(path);

            MockHttpServletResponse response =
                    new MockHttpServletResponse();

            TestingAuthenticationToken authentication =
                    new TestingAuthenticationToken(
                            "test-" + role.toLowerCase(),
                            "not-a-real-password",
                            "ROLE_" + role
                    );

            authentication.setAuthenticated(true);

            SecurityContext context =
                    SecurityContextHolder.createEmptyContext();

            context.setAuthentication(authentication);

            securityContextRepository.saveContext(
                    context,
                    request,
                    response
            );

            if (includeCsrf) {
                HttpSessionCsrfTokenRepository csrfRepository =
                        new HttpSessionCsrfTokenRepository();

                CsrfToken csrfToken =
                        csrfRepository.generateToken(request);

                csrfRepository.saveToken(
                        csrfToken,
                        request,
                        response
                );

                org.springframework.security.web.csrf
                        .XorCsrfTokenRequestAttributeHandler csrfHandler =
                        new org.springframework.security.web.csrf
                                .XorCsrfTokenRequestAttributeHandler();

                csrfHandler.handle(
                        request,
                        response,
                        () -> csrfToken
                );

                CsrfToken encodedToken =
                        (CsrfToken) request.getAttribute(
                                CsrfToken.class.getName()
                        );

                if (encodedToken == null) {
                    throw new IllegalStateException(
                            "Token CSRF codificato non disponibile"
                    );
                }

                request.setParameter(
                        encodedToken.getParameterName(),
                        encodedToken.getToken()
                );
            }

            AtomicBoolean applicationReached =
                    new AtomicBoolean(false);

            FilterChain applicationChain = (req, res) ->
                    applicationReached.set(true);

            try {
                proxy.doFilter(
                        request,
                        response,
                        applicationChain
                );

                if (expectedAllowed) {

                    assertTrue(
                            applicationReached.get(),
                            "La richiesta autorizzata deve superare i filtri: "
                                    + role + " " + method + " " + path
                    );
                } else {
                    assertEquals(
                            403,
                            response.getStatus(),
                            "La richiesta deve essere rifiutata: "
                                    + role + " " + method + " " + path
                    );

                    assertFalse(
                            applicationReached.get(),
                            "La richiesta rifiutata non deve raggiungere "
                                    + "la catena applicativa"
                    );
                }

                verify(dataSource, never()).getConnection();

            } finally {
                SecurityContextHolder.clearContext();
            }

        } finally {
            applicationContext.close();
        }
    }

    @Test
    void adminCanPassInvoiceEndpointSecurity() throws Exception {
        verifyRequest("ADMIN", "POST", INVOICE_PATH, true, true);
    }

    @Test
    void purchasingCanPassInvoiceEndpointSecurity() throws Exception {
        verifyRequest("PURCHASING", "POST", INVOICE_PATH, true, true);
    }

    @Test
    void warehouseCannotGenerateSupplierInvoice() throws Exception {
        verifyRequest("WAREHOUSE", "POST", INVOICE_PATH, true, false);
    }

    @Test
    void accountingCannotGenerateSupplierInvoice() throws Exception {
        verifyRequest("ACCOUNTING", "POST", INVOICE_PATH, true, false);
    }

    @Test
    void salesCannotGenerateSupplierInvoice() throws Exception {
        verifyRequest("SALES", "POST", INVOICE_PATH, true, false);
    }

    @Test
    void postWithoutCsrfMustBeRejected() throws Exception {
        verifyRequest("ADMIN", "POST", INVOICE_PATH, false, false);
    }

    @Test
    void warehouseCanAccessGoodsReceiptDetail() throws Exception {
        verifyRequest("WAREHOUSE", "GET", RECEIPT_PATH, false, true);
    }
}
