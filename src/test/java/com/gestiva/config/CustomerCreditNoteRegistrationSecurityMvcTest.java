
package com.gestiva.config;

import com.gestiva.creditnote.controller.CustomerCreditNoteRegistrationPageController;
import com.gestiva.creditnote.service.CustomerCreditNoteRegistrationService;
import com.gestiva.security.auth.CustomUserDetailsService;
import com.gestiva.security.auth.TenantUsernamePasswordAuthenticationFilter;
import com.gestiva.security.company.context.CompanyContext;
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
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CustomerCreditNoteRegistrationSecurityMvcTest {

    private static final String PATH =
            "/customer-credit-notes/50/register";

    private StaticApplicationContext applicationContext;

    private TenantContext tenantContext;
    private CompanyContext companyContext;
    private CustomerCreditNoteRegistrationService registrationService;

    private DataSource dataSource;
    private MockMvc mvc;

    @BeforeEach
    void setUp() throws Exception {

        tenantContext = mock(TenantContext.class);
        companyContext = mock(CompanyContext.class);

        registrationService = mock(
                CustomerCreditNoteRegistrationService.class
        );

        dataSource = mock(DataSource.class);

        when(tenantContext.getCurrentTenantId())
                .thenReturn(2L);

        when(companyContext.requireCurrentCompanyId())
                .thenReturn(3L);

        when(registrationService.register(2L, 50L))
                .thenReturn(
                        new CustomerCreditNoteRegistrationService
                                .RegistrationResult(
                                50L,
                                100L,
                                200L
                        )
                );

        CustomUserDetailsService userDetailsService =
                mock(CustomUserDetailsService.class);

        AuthenticationProvider provider =
                mock(AuthenticationProvider.class);

        RememberMeServices rememberMeServices =
                mock(RememberMeServices.class);

        SecurityConfig securityConfig =
                new SecurityConfig(
                        userDetailsService,
                        dataSource
                );

        TenantUsernamePasswordAuthenticationFilter loginFilter =
                securityConfig
                        .tenantUsernamePasswordAuthenticationFilter(
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

        applicationContext =
                new StaticApplicationContext();

        applicationContext.refresh();

        Map<Class<?>, Object> sharedObjects =
                new HashMap<>();

        sharedObjects.put(
                org.springframework.context.ApplicationContext.class,
                applicationContext
        );

        HttpSecurity http =
                new HttpSecurity(
                        postProcessor,
                        authBuilder,
                        sharedObjects
                );

        var repository =
                new org.springframework.security.web.context
                        .HttpSessionSecurityContextRepository();

        http.securityContext(configurer ->
                configurer.securityContextRepository(
                        repository
                )
        );

        SecurityFilterChain chain =
                securityConfig.securityFilterChain(
                        http,
                        loginFilter,
                        provider
                );

        CustomerCreditNoteRegistrationPageController controller =
                new CustomerCreditNoteRegistrationPageController(
                        tenantContext,
                        companyContext,
                        registrationService
                );

        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .apply(
                        springSecurity(
                                new FilterChainProxy(chain)
                        )
                )
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
    void adminCanRegisterCustomerCreditNote()
            throws Exception {

        mvc.perform(
                        post(PATH)
                                .with(user("admin").roles("ADMIN"))
                                .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(
                        redirectedUrl(
                                "/customer-credit-notes/50"
                        )
                )
                .andExpect(
                        flash().attributeExists(
                                "successMessage"
                        )
                );

        verify(registrationService, times(1))
                .register(2L, 50L);
    }

    @Test
    void accountingCanRegisterCustomerCreditNote()
            throws Exception {

        mvc.perform(
                        post(PATH)
                                .with(
                                        user("accounting")
                                                .roles("ACCOUNTING")
                                )
                                .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(
                        redirectedUrl(
                                "/customer-credit-notes/50"
                        )
                );

        verify(registrationService, times(1))
                .register(2L, 50L);
    }

    @Test
    void salesCannotRegisterCustomerCreditNote()
            throws Exception {

        mvc.perform(
                        post(PATH)
                                .with(user("sales").roles("SALES"))
                                .with(csrf())
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(registrationService);
        verifyNoInteractions(tenantContext);
        verifyNoInteractions(companyContext);
    }

    @Test
    void purchasingCannotRegisterCustomerCreditNote()
            throws Exception {

        mvc.perform(
                        post(PATH)
                                .with(
                                        user("purchasing")
                                                .roles("PURCHASING")
                                )
                                .with(csrf())
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(registrationService);
        verifyNoInteractions(tenantContext);
        verifyNoInteractions(companyContext);
    }

    @Test
    void adminWithoutCsrfCannotRegister()
            throws Exception {

        mvc.perform(
                        post(PATH)
                                .with(user("admin").roles("ADMIN"))
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(registrationService);
        verifyNoInteractions(tenantContext);
        verifyNoInteractions(companyContext);
    }

    @Test
    void anonymousUserCannotRegister()
            throws Exception {

        mvc.perform(
                        post(PATH).with(csrf())
                )
                .andExpect(status().is3xxRedirection());

        verifyNoInteractions(registrationService);
        verifyNoInteractions(tenantContext);
        verifyNoInteractions(companyContext);
    }
}
