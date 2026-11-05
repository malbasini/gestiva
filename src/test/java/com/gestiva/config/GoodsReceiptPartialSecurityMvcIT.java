package com.gestiva.config;

import com.gestiva.purchasing.receipt.controller.GoodsReceiptPartialPageController;
import com.gestiva.purchasing.receipt.service.GoodsReceiptPartialApplicationService;
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

class GoodsReceiptPartialSecurityMvcIT {

    private StaticApplicationContext applicationContext;
    private GoodsReceiptPartialApplicationService service;
    private TenantContext tenantContext;
    private DataSource dataSource;
    private MockMvc mvc;

    @BeforeEach
    void setUp() throws Exception {
        service = mock(GoodsReceiptPartialApplicationService.class);
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

        GoodsReceiptPartialPageController controller =
                new GoodsReceiptPartialPageController(
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

    @Test
    void adminWithCsrfReachesController() throws Exception {
        when(tenantContext.getCurrentTenantId()).thenReturn(2L);

        when(service.create(
                eq(2L),
                eq(100L),
                eq(50L),
                anyMap()
        )).thenReturn(301L);

        mvc.perform(post("/purchase-orders/100/receive-partial")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .param("warehouseId", "50")
                        .param("quantity_200", "8.000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/goods-receipts/301"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(service).create(
                2L,
                100L,
                50L,
                Map.of(200L, new BigDecimal("8.000"))
        );

        verify(dataSource, never()).getConnection();
    }

    @Test
    void warehouseCannotReachController() throws Exception {
        mvc.perform(post("/purchase-orders/100/receive-partial")
                        .with(user("warehouse").roles("WAREHOUSE"))
                        .with(csrf())
                        .param("warehouseId", "50")
                        .param("quantity_200", "8.000"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(service, tenantContext);
        verify(dataSource, never()).getConnection();
    }

    @Test
    void adminWithoutCsrfCannotReachController() throws Exception {
        mvc.perform(post("/purchase-orders/100/receive-partial")
                        .with(user("admin").roles("ADMIN"))
                        .param("warehouseId", "50")
                        .param("quantity_200", "8.000"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(service, tenantContext);
        verify(dataSource, never()).getConnection();
    }

    @Test
    void invalidCsrfCannotReachController() throws Exception {
        mvc.perform(post("/purchase-orders/100/receive-partial")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf().useInvalidToken())
                        .param("warehouseId", "50")
                        .param("quantity_200", "8.000"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(service, tenantContext);
        verify(dataSource, never()).getConnection();
    }


    @Test
    void purchasingWithCsrfReachesController() throws Exception {
        when(tenantContext.getCurrentTenantId()).thenReturn(2L);

        when(service.create(
                eq(2L),
                eq(100L),
                eq(50L),
                anyMap()
        )).thenReturn(302L);

        mvc.perform(post("/purchase-orders/100/receive-partial")
                        .with(user("purchasing").roles("PURCHASING"))
                        .with(csrf())
                        .param("warehouseId", "50")
                        .param("quantity_200", "7.000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/goods-receipts/302"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(service).create(
                2L,
                100L,
                50L,
                Map.of(200L, new BigDecimal("7.000"))
        );

        verify(dataSource, never()).getConnection();
    }

    @Test
    void anonymousWithCsrfCannotReachController() throws Exception {
        mvc.perform(post("/purchase-orders/100/receive-partial")
                        .with(anonymous())
                        .with(csrf())
                        .param("warehouseId", "50")
                        .param("quantity_200", "8.000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(result -> {
                    String redirect =
                            result.getResponse().getRedirectedUrl();

                    assertNotNull(
                            redirect,
                            "Reindirizzamento al login mancante."
                    );

                    assertTrue(
                            redirect.endsWith("/login"),
                            "La richiesta anonima deve andare al login: "
                                    + redirect
                    );
                });

        verifyNoInteractions(service, tenantContext);
        verify(dataSource, never()).getConnection();
    }
}
