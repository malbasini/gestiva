
package com.gestiva.config;


import com.gestiva.creditnote.controller.CustomerCreditNoteCompensationPageController;
import com.gestiva.creditnote.entity.CreditNoteFinancialPosition;
import com.gestiva.creditnote.repository.CreditNoteFinancialPositionRepository;
import com.gestiva.creditnote.service.CustomerCreditNoteCompensationService;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.security.usercontext.TenantContext;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import com.gestiva.security.auth.CustomUserDetailsService;
import com.gestiva.security.auth.TenantUsernamePasswordAuthenticationFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CustomerCreditNoteCompensationIntegratedMvcTest {

    private StaticApplicationContext applicationContext;
    private DataSource dataSource;
    private MockMvc mvc;

    private static final String PATH =
            "/customer-credit-notes/10/compensations";

    private TenantContext tenantContext;
    private CompanyContext companyContext;
    private CreditNoteFinancialPositionRepository positions;
    private CustomerCreditNoteCompensationService compensationService;

    private static final LocalDate DATE =
            LocalDate.of(2026, 11, 14);

    private void prepareValidCompensation() {
        when(tenantContext.getCurrentTenantId()).thenReturn(2L);
        when(companyContext.requireCurrentCompanyId()).thenReturn(3L);

        CreditNoteFinancialPosition position =
                mock(CreditNoteFinancialPosition.class);
        when(position.getId()).thenReturn(19L);

        when(positions.findByTenantIdAndCompanyIdAndCreditNoteId(
                2L, 3L, 10L
        )).thenReturn(Optional.of(position));

        when(compensationService.compensate(
                eq(2L),
                eq(19L),
                eq(7L),
                eq(new BigDecimal("12.50")),
                eq(DATE),
                eq("v11-integrated-op"),
                eq("test")
        )).thenReturn(501L);
    }

    @BeforeEach
    void setUp() throws Exception {

        dataSource = mock(DataSource.class);
        tenantContext = mock(TenantContext.class);
        companyContext = mock(CompanyContext.class);
        positions = mock(CreditNoteFinancialPositionRepository.class);
        compensationService =
                mock(CustomerCreditNoteCompensationService.class);

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

        var securityContextRepository =
                new org.springframework.security.web.context
                        .HttpSessionSecurityContextRepository();

        http.securityContext(configurer ->
                configurer.securityContextRepository(
                        securityContextRepository
                )
        );

        SecurityFilterChain chain =
                securityConfig.securityFilterChain(
                        http,
                        loginFilter,
                        authenticationProvider
                );

        mvc = MockMvcBuilders
                .standaloneSetup(
                        new CustomerCreditNoteCompensationPageController(
                                tenantContext,
                                companyContext,
                                positions,
                                compensationService
                        )
                )
                .apply(springSecurity(new FilterChainProxy(chain)))
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

    // GESTIVA V2 - PASSO 5.13.2395LN
    // SecurityFilterChain e controller POST reali.
    // Servizi, repository e contesti completamente simulati.

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
    validRequest() {
        return post(PATH)
                .param("paymentDueId", "7")
                .param("amount", "12.50")
                .param("operationKey", "v11-integrated-op")
                .param("compensationDate", "2026-11-14")
                .param("notes", "test");
    }

    @Test
    void adminReachesRealControllerAndService() throws Exception {
        prepareValidCompensation();

        mvc.perform(validRequest()
                        .with(user("admin").roles("ADMIN"))
                        .principal(
                                new org.springframework.security.authentication
                                        .UsernamePasswordAuthenticationToken(
                                        "admin",
                                        "unused",
                                        java.util.List.of(
                                                new org.springframework.security.core.authority
                                                        .SimpleGrantedAuthority("ROLE_ADMIN")
                                        )
                                )
                        )
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(
                        "/customer-credit-notes/10"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(positions).findByTenantIdAndCompanyIdAndCreditNoteId(
                2L, 3L, 10L);

        verify(compensationService).compensate(
                2L, 19L, 7L, new BigDecimal("12.50"),
                DATE, "v11-integrated-op", "test");
    }

    @Test
    void accountingReachesRealControllerAndService()
            throws Exception {
        prepareValidCompensation();

        mvc.perform(validRequest()
                        .with(user("accountant").roles("ACCOUNTING"))
                        .principal(
                                new org.springframework.security.authentication
                                        .UsernamePasswordAuthenticationToken(
                                        "accountant",
                                        "unused",
                                        java.util.List.of(
                                                new org.springframework.security.core.authority
                                                        .SimpleGrantedAuthority("ROLE_ACCOUNTING")
                                        )
                                )
                        )
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("successMessage"));

        verify(compensationService).compensate(
                2L, 19L, 7L, new BigDecimal("12.50"),
                DATE, "v11-integrated-op", "test");
    }

    @Test
    void salesIsDeniedBeforeController() throws Exception {
        mvc.perform(validRequest()
                        .with(user("sales").roles("SALES"))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(
                tenantContext, companyContext,
                positions, compensationService);
    }

    @Test
    void purchasingIsDeniedBeforeController()
            throws Exception {
        mvc.perform(validRequest()
                        .with(user("purchasing").roles("PURCHASING"))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(
                tenantContext, companyContext,
                positions, compensationService);
    }

    @Test
    void csrfMissingBlocksRealController() throws Exception {
        mvc.perform(validRequest()
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(
                tenantContext, companyContext,
                positions, compensationService);
    }

    @Test
    void anonymousRequestCannotReachRealController()
            throws Exception {
        mvc.perform(validRequest().with(csrf()))
                .andExpect(status().is3xxRedirection());

        verifyNoInteractions(
                tenantContext, companyContext,
                positions, compensationService);
    }

    @Test
    void browserSuppliedPositionIdIsIgnored() throws Exception {
        prepareValidCompensation();

        mvc.perform(validRequest()
                        .param("positionId", "999999")
                        .with(user("admin").roles("ADMIN"))
                        .principal(
                                new org.springframework.security.authentication
                                        .UsernamePasswordAuthenticationToken(
                                        "admin",
                                        "unused",
                                        java.util.List.of(
                                                new org.springframework.security.core.authority
                                                        .SimpleGrantedAuthority("ROLE_ADMIN")
                                        )
                                )
                        )
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("successMessage"));

        verify(positions).findByTenantIdAndCompanyIdAndCreditNoteId(
                2L, 3L, 10L);

        verify(compensationService).compensate(
                2L, 19L, 7L, new BigDecimal("12.50"),
                DATE, "v11-integrated-op", "test");
    }
}
