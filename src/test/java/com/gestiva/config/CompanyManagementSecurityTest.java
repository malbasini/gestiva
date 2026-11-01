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

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CompanyManagementSecurityTest {

    @Test
    void salesUserCannotAccessCompanyManagement() throws Exception {

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

        HashMap<Class<?>, Object> sharedObjects = new HashMap<>();
        sharedObjects.put(
                org.springframework.context.ApplicationContext.class,
                applicationContext
        );

        HttpSecurity http = new HttpSecurity(
                postProcessor,
                authenticationBuilder,
                sharedObjects
        );

        SecurityContextRepository contextRepository =
                new HttpSessionSecurityContextRepository();

        http.securityContext(config -> config
                .securityContextRepository(contextRepository));

        SecurityFilterChain chain =
                securityConfig.securityFilterChain(
                        http,
                        loginFilter,
                        authenticationProvider
                );

        FilterChainProxy proxy = new FilterChainProxy(chain);

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/settings/companies"
                );

        request.setServletPath("/settings/companies");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken(
                        "test-sales-user",
                        "test-password",
                        "ROLE_SALES"
                );

        authentication.setAuthenticated(true);

        SecurityContext context =
                SecurityContextHolder.createEmptyContext();

        context.setAuthentication(authentication);

        contextRepository.saveContext(
                context,
                request,
                response
        );

        AtomicBoolean applicationReached =
                new AtomicBoolean(false);

        FilterChain applicationChain = (req, res) ->
                applicationReached.set(true);

        try {
            proxy.doFilter(request, response, applicationChain);

            assertEquals(403, response.getStatus());
            assertEquals("/403", response.getForwardedUrl());
            assertFalse(applicationReached.get());

            verify(dataSource, never()).getConnection();

        } finally {
            SecurityContextHolder.clearContext();
            applicationContext.close();
        }
    }

    @Test
    void adminUserCanAccessCompanyManagement() throws Exception {

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

        HashMap<Class<?>, Object> sharedObjects = new HashMap<>();
        sharedObjects.put(
                org.springframework.context.ApplicationContext.class,
                applicationContext
        );

        HttpSecurity http = new HttpSecurity(
                postProcessor,
                authenticationBuilder,
                sharedObjects
        );

        SecurityContextRepository contextRepository =
                new HttpSessionSecurityContextRepository();

        http.securityContext(config -> config
                .securityContextRepository(contextRepository));

        SecurityFilterChain chain =
                securityConfig.securityFilterChain(
                        http,
                        loginFilter,
                        authenticationProvider
                );

        FilterChainProxy proxy = new FilterChainProxy(chain);

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/settings/companies"
                );

        request.setServletPath("/settings/companies");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken(
                        "test-admin-user",
                        "test-password",
                        "ROLE_ADMIN"
                );

        authentication.setAuthenticated(true);

        SecurityContext context =
                SecurityContextHolder.createEmptyContext();

        context.setAuthentication(authentication);

        contextRepository.saveContext(
                context,
                request,
                response
        );

        AtomicBoolean applicationReached =
                new AtomicBoolean(false);

        FilterChain applicationChain = (req, res) ->
                applicationReached.set(true);

        try {
            proxy.doFilter(request, response, applicationChain);

            assertTrue(
                    applicationReached.get(),
                    "La richiesta ADMIN deve superare la catena HTTP"
            );

            assertNotEquals(403, response.getStatus());

            verify(dataSource, never()).getConnection();

        } finally {
            SecurityContextHolder.clearContext();
            applicationContext.close();
        }
    }


    @Test
    void adminCannotCreateCompanyWithoutCsrf() throws Exception {

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

        HashMap<Class<?>, Object> sharedObjects = new HashMap<>();
        sharedObjects.put(
                org.springframework.context.ApplicationContext.class,
                applicationContext
        );

        HttpSecurity http = new HttpSecurity(
                postProcessor,
                authenticationBuilder,
                sharedObjects
        );

        SecurityContextRepository contextRepository =
                new HttpSessionSecurityContextRepository();

        http.securityContext(config -> config
                .securityContextRepository(contextRepository));

        SecurityFilterChain chain =
                securityConfig.securityFilterChain(
                        http,
                        loginFilter,
                        authenticationProvider
                );

        FilterChainProxy proxy = new FilterChainProxy(chain);

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "POST",
                        "/settings/companies/new"
                );

        request.setServletPath("/settings/companies/new");

        request.addParameter("code", "TEST03");
        request.addParameter("legalName", "Società di prova CSRF");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken(
                        "test-admin-user",
                        "test-password",
                        "ROLE_ADMIN"
                );

        authentication.setAuthenticated(true);

        SecurityContext context =
                SecurityContextHolder.createEmptyContext();

        context.setAuthentication(authentication);

        contextRepository.saveContext(
                context,
                request,
                response
        );

        AtomicBoolean applicationReached =
                new AtomicBoolean(false);

        FilterChain applicationChain = (req, res) ->
                applicationReached.set(true);

        try {
            proxy.doFilter(request, response, applicationChain);

            assertEquals(
                    403,
                    response.getStatus(),
                    "POST senza token CSRF deve essere respinto"
            );

            assertFalse(
                    applicationReached.get(),
                    "La richiesta senza CSRF non deve raggiungere l'applicazione"
            );

            verify(dataSource, never()).getConnection();

        } finally {
            SecurityContextHolder.clearContext();
            applicationContext.close();
        }
    }

    @Test
    void adminCannotUpdateCompanyWithoutCsrf() throws Exception {

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

        HashMap<Class<?>, Object> sharedObjects = new HashMap<>();
        sharedObjects.put(
                org.springframework.context.ApplicationContext.class,
                applicationContext
        );

        HttpSecurity http = new HttpSecurity(
                postProcessor,
                authenticationBuilder,
                sharedObjects
        );

        SecurityContextRepository contextRepository =
                new HttpSessionSecurityContextRepository();

        http.securityContext(config -> config
                .securityContextRepository(contextRepository));

        SecurityFilterChain chain =
                securityConfig.securityFilterChain(
                        http,
                        loginFilter,
                        authenticationProvider
                );

        FilterChainProxy proxy = new FilterChainProxy(chain);

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "POST",
                        "/settings/companies/500/edit"
                );

        request.setServletPath("/settings/companies/500/edit");

        request.addParameter("active", "true");
        request.addParameter("legalName", "Società aggiornata CSRF");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken(
                        "test-admin-user",
                        "test-password",
                        "ROLE_ADMIN"
                );

        authentication.setAuthenticated(true);

        SecurityContext context =
                SecurityContextHolder.createEmptyContext();

        context.setAuthentication(authentication);

        contextRepository.saveContext(
                context,
                request,
                response
        );

        AtomicBoolean applicationReached =
                new AtomicBoolean(false);

        FilterChain applicationChain = (req, res) ->
                applicationReached.set(true);

        try {
            proxy.doFilter(request, response, applicationChain);

            assertEquals(
                    403,
                    response.getStatus(),
                    "POST senza token CSRF deve essere respinto"
            );

            assertFalse(
                    applicationReached.get(),
                    "La richiesta senza CSRF non deve raggiungere l'applicazione"
            );

            verify(dataSource, never()).getConnection();

        } finally {
            SecurityContextHolder.clearContext();
            applicationContext.close();
        }
    }

    @Test
    void adminCanSubmitCompanyCreationWithValidCsrf() throws Exception {

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

        HashMap<Class<?>, Object> sharedObjects = new HashMap<>();
        sharedObjects.put(
                org.springframework.context.ApplicationContext.class,
                applicationContext
        );

        HttpSecurity http = new HttpSecurity(
                postProcessor,
                authenticationBuilder,
                sharedObjects
        );

        SecurityContextRepository contextRepository =
                new HttpSessionSecurityContextRepository();

        http.securityContext(config -> config
                .securityContextRepository(contextRepository));

        SecurityFilterChain chain =
                securityConfig.securityFilterChain(
                        http,
                        loginFilter,
                        authenticationProvider
                );

        FilterChainProxy proxy = new FilterChainProxy(chain);

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "POST",
                        "/settings/companies/new"
                );

        request.setServletPath("/settings/companies/new");

        request.addParameter("code", "TEST03");
        request.addParameter("legalName", "Società di prova CSRF");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken(
                        "test-admin-user",
                        "test-password",
                        "ROLE_ADMIN"
                );

        authentication.setAuthenticated(true);

        SecurityContext context =
                SecurityContextHolder.createEmptyContext();

        context.setAuthentication(authentication);

        contextRepository.saveContext(
                context,
                request,
                response
        );

        org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository
                csrfRepository =
                new org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository();

        org.springframework.security.web.csrf.CsrfToken csrfToken =
                csrfRepository.generateToken(request);

        csrfRepository.saveToken(csrfToken, request, response);

        org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler
                csrfHandler =
                new org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler();

        csrfHandler.handle(
                request,
                response,
                () -> csrfToken
        );

        org.springframework.security.web.csrf.CsrfToken exposedToken =
                (org.springframework.security.web.csrf.CsrfToken)
                request.getAttribute(
                        org.springframework.security.web.csrf.CsrfToken.class.getName()
                );

        assertNotNull(
                exposedToken,
                "Il token CSRF codificato deve essere disponibile"
        );

        request.addParameter(
                exposedToken.getParameterName(),
                exposedToken.getToken()
        );

        assertNotNull(
                csrfRepository.loadToken(request),
                "Il token CSRF deve essere presente nella sessione"
        );

        AtomicBoolean applicationReached =
                new AtomicBoolean(false);

        FilterChain applicationChain = (req, res) ->
                applicationReached.set(true);

        try {
            proxy.doFilter(request, response, applicationChain);

            System.out.println(
                    "CSRF token in session: "
                    + (csrfRepository.loadToken(request) != null)
            );

            System.out.println(
                    "CSRF parameter matches stored token: "
                    + csrfToken.getToken().equals(
                            request.getParameter(
                                    csrfToken.getParameterName()
                            )
                    )
            );

            chain.getFilters().stream()
                    .filter(f -> f instanceof
                            org.springframework.security.web.csrf.CsrfFilter)
                    .forEach(f -> System.out.println(
                            "CSRF filter: " + f.getClass().getName()
                    ));

            System.out.println("=== DIAGNOSTICA CSRF ADMIN ===");
            System.out.println("HTTP status: " + response.getStatus());
            System.out.println("Forward: " + response.getForwardedUrl());
            System.out.println("Redirect: " + response.getRedirectedUrl());
            System.out.println("Application reached: "
                    + applicationReached.get());
            System.out.println("Session exists: "
                    + (request.getSession(false) != null));

            assertTrue(
                    applicationReached.get(),
                    "POST ADMIN con CSRF valido deve superare i filtri"
            );

            assertNotEquals(403, response.getStatus());

            verify(dataSource, never()).getConnection();

        } finally {
            SecurityContextHolder.clearContext();
            applicationContext.close();
        }
    }

    @Test
    void adminCanSubmitCompanyUpdateWithValidCsrf() throws Exception {

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

        HashMap<Class<?>, Object> sharedObjects = new HashMap<>();
        sharedObjects.put(
                org.springframework.context.ApplicationContext.class,
                applicationContext
        );

        HttpSecurity http = new HttpSecurity(
                postProcessor,
                authenticationBuilder,
                sharedObjects
        );

        SecurityContextRepository contextRepository =
                new HttpSessionSecurityContextRepository();

        http.securityContext(config -> config
                .securityContextRepository(contextRepository));

        SecurityFilterChain chain =
                securityConfig.securityFilterChain(
                        http,
                        loginFilter,
                        authenticationProvider
                );

        FilterChainProxy proxy = new FilterChainProxy(chain);

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "POST",
                        "/settings/companies/500/edit"
                );

        request.setServletPath("/settings/companies/500/edit");

        request.addParameter("active", "true");
        request.addParameter("legalName", "Società aggiornata CSRF");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken(
                        "test-admin-user",
                        "test-password",
                        "ROLE_ADMIN"
                );

        authentication.setAuthenticated(true);

        SecurityContext context =
                SecurityContextHolder.createEmptyContext();

        context.setAuthentication(authentication);

        contextRepository.saveContext(
                context,
                request,
                response
        );

        org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository
                csrfRepository =
                new org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository();

        org.springframework.security.web.csrf.CsrfToken csrfToken =
                csrfRepository.generateToken(request);

        csrfRepository.saveToken(csrfToken, request, response);

        org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler
                csrfHandler =
                new org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler();

        csrfHandler.handle(
                request,
                response,
                () -> csrfToken
        );

        org.springframework.security.web.csrf.CsrfToken exposedToken =
                (org.springframework.security.web.csrf.CsrfToken)
                request.getAttribute(
                        org.springframework.security.web.csrf.CsrfToken.class.getName()
                );

        assertNotNull(
                exposedToken,
                "Il token CSRF codificato deve essere disponibile"
        );

        request.addParameter(
                exposedToken.getParameterName(),
                exposedToken.getToken()
        );

        assertNotNull(
                csrfRepository.loadToken(request),
                "Il token CSRF deve essere presente nella sessione"
        );

        AtomicBoolean applicationReached =
                new AtomicBoolean(false);

        FilterChain applicationChain = (req, res) ->
                applicationReached.set(true);

        try {
            proxy.doFilter(request, response, applicationChain);

            System.out.println(
                    "CSRF token in session: "
                    + (csrfRepository.loadToken(request) != null)
            );

            System.out.println(
                    "CSRF parameter matches stored token: "
                    + csrfToken.getToken().equals(
                            request.getParameter(
                                    csrfToken.getParameterName()
                            )
                    )
            );

            chain.getFilters().stream()
                    .filter(f -> f instanceof
                            org.springframework.security.web.csrf.CsrfFilter)
                    .forEach(f -> System.out.println(
                            "CSRF filter: " + f.getClass().getName()
                    ));

            System.out.println("=== DIAGNOSTICA CSRF ADMIN ===");
            System.out.println("HTTP status: " + response.getStatus());
            System.out.println("Forward: " + response.getForwardedUrl());
            System.out.println("Redirect: " + response.getRedirectedUrl());
            System.out.println("Application reached: "
                    + applicationReached.get());
            System.out.println("Session exists: "
                    + (request.getSession(false) != null));

            assertTrue(
                    applicationReached.get(),
                    "POST ADMIN con CSRF valido deve superare i filtri"
            );

            assertNotEquals(403, response.getStatus());

            verify(dataSource, never()).getConnection();

        } finally {
            SecurityContextHolder.clearContext();
            applicationContext.close();
        }
    }

}
