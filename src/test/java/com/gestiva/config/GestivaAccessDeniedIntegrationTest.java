package com.gestiva.config;

import com.gestiva.security.auth.CustomUserDetailsService;
import com.gestiva.security.controller.ErrorPageController;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
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

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GestivaAccessDeniedIntegrationTest {

    @Test
    void shouldRejectSalesUserWithHttp403() throws Exception {

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
                new MockHttpServletRequest(
                        "GET",
                        "/settings/test-access-denied"
                );

        request.setServletPath(
                "/settings/test-access-denied"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        System.out.println("=== DIAGNOSTICA SECURITY ===");
        System.out.println(
                "Chain matches request: "
                + securityFilterChain.matches(request)
        );

        securityFilterChain.getFilters().forEach(filter ->
                System.out.println(
                        "FILTER: "
                        + filter.getClass().getName()
                )
        );

        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken(
                        "test-sales-user",
                        "not-a-real-password",
                        "ROLE_SALES"
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

        AtomicBoolean applicationReached =
                new AtomicBoolean(false);

        FilterChain applicationChain = (req, res) -> {
            applicationReached.set(true);
            System.out.println(
                    "DIAGNOSTICA: richiesta arrivata all'applicazione"
            );
        };

        try {
            proxy.doFilter(
                    request,
                    response,
                    applicationChain
            );

            assertEquals(
                    403,
                    response.getStatus(),
                    "Un utente SALES non deve accedere alle impostazioni ADMIN"
            );

            assertEquals(
                    "/403",
                    response.getForwardedUrl(),
                    "Il gestore reale deve inoltrare alla pagina HTML 403"
            );

            assertFalse(
                    applicationReached.get(),
                    "La richiesta non deve raggiungere l'applicazione"
            );

            verify(dataSource, never()).getConnection();

        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void shouldResolveAccessDeniedHtmlViewWithMockMvc() throws Exception {

        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new ErrorPageController())
                .build();

        mockMvc.perform(get("/403"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/403"));
    }
}
