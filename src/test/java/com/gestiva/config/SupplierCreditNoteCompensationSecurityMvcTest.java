
package com.gestiva.config;

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

class SupplierCreditNoteCompensationSecurityMvcTest {

    private StaticApplicationContext applicationContext;
    private DataSource dataSource;
    private MockMvc mvc;

    private static final String PATH =
            "/supplier-credit-notes/10/compensations";

    @RestController
    @RequestMapping("/supplier-credit-notes")
    static class TestController {

        @PostMapping("/{id}/compensations")
        ResponseEntity<String> compensate(@PathVariable Long id) {
            return ResponseEntity.ok("AUTHORIZED");
        }
    }

    @BeforeEach
    void setUp() throws Exception {

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
                .standaloneSetup(new TestController())
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

    @Test
    void adminCanAccessCompensationEndpoint() throws Exception {
        mvc.perform(post(PATH)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("AUTHORIZED"));
    }

    @Test
    void accountingCanAccessCompensationEndpoint() throws Exception {
        mvc.perform(post(PATH)
                        .with(user("accountant").roles("ACCOUNTING"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("AUTHORIZED"));
    }

    @Test
    void purchasingCannotAccessCompensationEndpoint()
            throws Exception {
        mvc.perform(post(PATH)
                        .with(user("purchasing").roles("PURCHASING"))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminWithoutCsrfIsRejected() throws Exception {
        mvc.perform(post(PATH)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden());
    }

    @Test
    void anonymousUserCannotAccessCompensationEndpoint()
            throws Exception {
        mvc.perform(post(PATH).with(csrf()))
                .andExpect(status().is3xxRedirection());
    }
}
