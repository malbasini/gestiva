
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
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CustomerCreditNoteHttpSecurityTest {

    private static final String BASE =
            "/customer-credit-notes";

    private static final AtomicInteger reached =
            new AtomicInteger();

    private StaticApplicationContext applicationContext;
    private DataSource dataSource;
    private MockMvc mvc;

    @RestController
    @RequestMapping("/customer-credit-notes")
    static class TestController {

        @PostMapping
        ResponseEntity<String> createFull() {
            reached.incrementAndGet();
            return ResponseEntity.ok("FULL_AUTHORIZED");
        }

        @PostMapping("/partial")
        ResponseEntity<String> createPartial() {
            reached.incrementAndGet();
            return ResponseEntity.ok("PARTIAL_AUTHORIZED");
        }

        @PostMapping("/{id}/register")
        ResponseEntity<String> register(
                @PathVariable Long id) {

            reached.incrementAndGet();
            return ResponseEntity.ok("REGISTER_AUTHORIZED");
        }

        @GetMapping("/{id}")
        ResponseEntity<String> detail(
                @PathVariable Long id) {

            reached.incrementAndGet();
            return ResponseEntity.ok("DETAIL_AUTHORIZED");
        }

        @DeleteMapping("/{id}")
        ResponseEntity<String> delete(
                @PathVariable Long id) {

            reached.incrementAndGet();
            return ResponseEntity.ok("DELETE_AUTHORIZED");
        }
    }

    @BeforeEach
    void setUp() throws Exception {

        reached.set(0);

        dataSource = mock(DataSource.class);

        CustomUserDetailsService userDetailsService =
                mock(CustomUserDetailsService.class);

        AuthenticationProvider provider =
                mock(AuthenticationProvider.class);

        RememberMeServices rememberMeServices =
                mock(RememberMeServices.class);

        SecurityConfig securityConfig =
                new SecurityConfig(
                        userDetailsService,
                        dataSource);

        TenantUsernamePasswordAuthenticationFilter loginFilter =
                securityConfig
                        .tenantUsernamePasswordAuthenticationFilter(
                                authentication -> authentication,
                                rememberMeServices);

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
                applicationContext);

        HttpSecurity http =
                new HttpSecurity(
                        postProcessor,
                        authBuilder,
                        sharedObjects);

        var repository =
                new org.springframework.security.web.context
                        .HttpSessionSecurityContextRepository();

        http.securityContext(configurer ->
                configurer.securityContextRepository(
                        repository));

        SecurityFilterChain chain =
                securityConfig.securityFilterChain(
                        http,
                        loginFilter,
                        provider);

        mvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .apply(springSecurity(
                        new FilterChainProxy(chain)))
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
    void adminCanCreateFullDraft() throws Exception {

        mvc.perform(post(BASE)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("FULL_AUTHORIZED"));

        assertEquals(1, reached.get());
    }

    @Test
    void salesCanCreatePartialDraft() throws Exception {

        mvc.perform(post(BASE + "/partial")
                        .with(user("sales").roles("SALES"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("PARTIAL_AUTHORIZED"));

        assertEquals(1, reached.get());
    }

    @Test
    void accountingCanCreateFullDraft() throws Exception {

        mvc.perform(post(BASE)
                        .with(user("accountant").roles("ACCOUNTING"))
                        .with(csrf()))
                .andExpect(status().isOk());

        assertEquals(1, reached.get());
    }

    @Test
    void salesCannotRegisterCreditNote() throws Exception {

        mvc.perform(post(BASE + "/50/register")
                        .with(user("sales").roles("SALES"))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        assertEquals(0, reached.get());
    }

    @Test
    void adminCanRegisterCreditNote() throws Exception {

        mvc.perform(post(BASE + "/50/register")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("REGISTER_AUTHORIZED"));

        assertEquals(1, reached.get());
    }

    @Test
    void accountingCanRegisterCreditNote() throws Exception {

        mvc.perform(post(BASE + "/50/register")
                        .with(user("accountant").roles("ACCOUNTING"))
                        .with(csrf()))
                .andExpect(status().isOk());

        assertEquals(1, reached.get());
    }

    @Test
    void purchasingCannotCreateCustomerCreditNote()
            throws Exception {

        mvc.perform(post(BASE)
                        .with(user("purchasing").roles("PURCHASING"))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        assertEquals(0, reached.get());
    }

    @Test
    void warehouseCannotViewCustomerCreditNote()
            throws Exception {

        mvc.perform(get(BASE + "/50")
                        .with(user("warehouse").roles("WAREHOUSE")))
                .andExpect(status().isForbidden());

        assertEquals(0, reached.get());
    }

    @Test
    void adminWithoutCsrfCannotCreateDraft()
            throws Exception {

        mvc.perform(post(BASE)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden());

        assertEquals(0, reached.get());
    }

    @Test
    void unexpectedDeleteOperationIsDenied()
            throws Exception {

        mvc.perform(delete(BASE + "/50")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        assertEquals(0, reached.get());
    }
}
