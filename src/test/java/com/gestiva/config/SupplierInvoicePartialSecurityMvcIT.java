package com.gestiva.config;

import com.gestiva.purchasing.invoice.controller.SupplierInvoicePartialPageController;
import com.gestiva.purchasing.invoice.service.SupplierInvoicePartialApplicationService;
import com.gestiva.security.auth.CustomUserDetailsService;
import com.gestiva.security.auth.TenantUsernamePasswordAuthenticationFilter;
import com.gestiva.security.usercontext.TenantContext;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SupplierInvoicePartialSecurityMvcIT {

    private StaticApplicationContext applicationContext;
    private SupplierInvoicePartialApplicationService service;
    private TenantContext tenantContext;
    private DataSource dataSource;
    private MockMvc mvc;

    @BeforeEach
    void setUp() throws Exception {
        service = mock(SupplierInvoicePartialApplicationService.class);
        tenantContext = mock(TenantContext.class);
        dataSource = mock(DataSource.class);

        CustomUserDetailsService userDetailsService =
                mock(CustomUserDetailsService.class);

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

        AuthenticationManagerBuilder authBuilder =
                new AuthenticationManagerBuilder(postProcessor);

        applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        Map<Class<?>, Object> sharedObjects = new HashMap<>();

        sharedObjects.put(
                org.springframework.context.ApplicationContext.class,
                applicationContext
        );

        HttpSecurity http = new HttpSecurity(
                postProcessor,
                authBuilder,
                sharedObjects
        );

        // Allineamento alla fixture di autorizzazione già collaudata:
        // il contesto autenticato viene conservato nella sessione
        // simulata durante il passaggio nella SecurityFilterChain.
        org.springframework.security.web.context.SecurityContextRepository
                securityContextRepository =
                new org.springframework.security.web.context
                        .HttpSessionSecurityContextRepository();

        http.securityContext(securityContext ->
                securityContext.securityContextRepository(
                        securityContextRepository
                )
        );

        SecurityFilterChain chain =
                securityConfig.securityFilterChain(
                        http,
                        loginFilter,
                        authenticationProvider
                );

        FilterChainProxy proxy = new FilterChainProxy(chain);

        SupplierInvoicePartialPageController controller =
                new SupplierInvoicePartialPageController(
                        service,
                        tenantContext
                );

        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .apply(springSecurity(proxy))
                .build();
    }

    @AfterEach
    void tearDown() {
        if (applicationContext != null) {
            applicationContext.close();
        }

        org.springframework.security.core.context
                .SecurityContextHolder.clearContext();
    }


    private static final String PATH =
            "/goods-receipts/302/invoice-partial";

    private void allowedRequest(String role)
            throws Exception {

        when(tenantContext.getCurrentTenantId())
                .thenReturn(2L);

        when(service.create(2L, 302L))
                .thenReturn(501L);

        mvc.perform(
                post(PATH)
                        .with(user("authorized").roles(role))
                        .with(csrf())
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(
                        redirectedUrl("/goods-receipts/302")
                )
                .andExpect(
                        flash().attributeExists("successMessage")
                );

        verify(tenantContext, times(1))
                .getCurrentTenantId();

        verify(service, times(1))
                .create(2L, 302L);

        verifyNoMoreInteractions(service);

        verify(dataSource, never()).getConnection();
    }

    private void forbiddenRole(String role)
            throws Exception {

        mvc.perform(
                post(PATH)
                        .with(user("forbidden").roles(role))
                        .with(csrf())
        )
                .andExpect(status().isForbidden());

        verifyNoInteractions(service, tenantContext);
        verify(dataSource, never()).getConnection();
    }

    @Test
    void adminWithCsrfReachesApplication() throws Exception {
        allowedRequest("ADMIN");
    }

    @Test
    void purchasingWithCsrfReachesApplication()
            throws Exception {

        allowedRequest("PURCHASING");
    }

    @Test
    void warehouseCannotReachApplication()
            throws Exception {

        forbiddenRole("WAREHOUSE");
    }

    @Test
    void accountingCannotReachApplication()
            throws Exception {

        forbiddenRole("ACCOUNTING");
    }

    @Test
    void salesCannotReachApplication()
            throws Exception {

        forbiddenRole("SALES");
    }

    @Test
    void adminWithoutCsrfCannotReachApplication()
            throws Exception {

        mvc.perform(
                post(PATH)
                        .with(user("admin").roles("ADMIN"))
        )
                .andExpect(status().isForbidden());

        verifyNoInteractions(service, tenantContext);
        verify(dataSource, never()).getConnection();
    }

    @Test
    void invalidCsrfCannotReachApplication()
            throws Exception {

        mvc.perform(
                post(PATH)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf().useInvalidToken())
        )
                .andExpect(status().isForbidden());

        verifyNoInteractions(service, tenantContext);
        verify(dataSource, never()).getConnection();
    }

    @Test
    void anonymousCannotReachApplication()
            throws Exception {

        mvc.perform(
                post(PATH)
                        .with(anonymous())
                        .with(csrf())
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(result -> {

                    String redirect =
                            result.getResponse().getRedirectedUrl();

                    assertNotNull(
                            redirect,
                            "Redirect login mancante."
                    );

                    assertTrue(
                            redirect.endsWith("/login"),
                            "Redirect anonimo inatteso: " + redirect
                    );
                });

        verifyNoInteractions(service, tenantContext);
        verify(dataSource, never()).getConnection();
    }
}
