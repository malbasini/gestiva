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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

class WarehouseLocationHttpSecurityTest {

    private static final String BASE = "/warehouse/locations";

    private static final AtomicInteger reached =
            new AtomicInteger();

    private StaticApplicationContext applicationContext;
    private MockMvc mvc;

    @RestController
    @RequestMapping("/warehouse/locations")
    static class TestController {

        @GetMapping
        ResponseEntity<String> list() {
            reached.incrementAndGet();
            return ResponseEntity.ok("LOCATIONS_AUTHORIZED");
        }

        @GetMapping("/new")
        ResponseEntity<String> createForm() {
            reached.incrementAndGet();
            return ResponseEntity.ok("LOCATION_FORM_AUTHORIZED");
        }

        @org.springframework.web.bind.annotation.PostMapping
        ResponseEntity<String> createLocation() {
            reached.incrementAndGet();
            return ResponseEntity.ok("LOCATION_CREATE_AUTHORIZED");
        }
    }

    @BeforeEach
    void setUp() throws Exception {

        reached.set(0);

        DataSource dataSource = mock(DataSource.class);

        CustomUserDetailsService userDetailsService =
                mock(CustomUserDetailsService.class);

        AuthenticationProvider provider =
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

        http.securityContext(configurer ->
                configurer.securityContextRepository(
                        new org.springframework.security.web.context
                                .HttpSessionSecurityContextRepository()
                )
        );

        SecurityFilterChain chain =
                securityConfig.securityFilterChain(
                        http,
                        loginFilter,
                        provider
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
    void adminCanViewLocations() throws Exception {
        mvc.perform(get(BASE).with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string("LOCATIONS_AUTHORIZED"));

        assertEquals(1, reached.get());
    }

    @Test
    void warehouseCanViewLocations() throws Exception {
        mvc.perform(get(BASE).with(user("warehouse").roles("WAREHOUSE")))
                .andExpect(status().isOk());

        assertEquals(1, reached.get());
    }

    @Test
    void salesCannotViewLocations() throws Exception {
        mvc.perform(get(BASE).with(user("sales").roles("SALES")))
                .andExpect(status().isForbidden());

        assertEquals(0, reached.get());
    }

    @Test
    void purchasingCannotViewLocations() throws Exception {
        mvc.perform(get(BASE).with(user("purchasing").roles("PURCHASING")))
                .andExpect(status().isForbidden());

        assertEquals(0, reached.get());
    }

    @Test
    void accountingCannotViewLocations() throws Exception {
        mvc.perform(get(BASE).with(user("accounting").roles("ACCOUNTING")))
                .andExpect(status().isForbidden());

        assertEquals(0, reached.get());
    }

    @Test
    void adminCanAccessLocationSubpath() throws Exception {
        mvc.perform(get(BASE + "/new")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string("LOCATION_FORM_AUTHORIZED"));

        assertEquals(1, reached.get());
    }

    @Test
    void salesCannotAccessLocationSubpath() throws Exception {
        mvc.perform(get(BASE + "/new")
                        .with(user("sales").roles("SALES")))
                .andExpect(status().isForbidden());

        assertEquals(0, reached.get());
    }

    @Test
    void anonymousUserIsRedirectedToLogin() throws Exception {
        mvc.perform(get(BASE))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        assertEquals(0, reached.get());
    }

    @Test
    void adminWithCsrfCanPostLocation() throws Exception {
        mvc.perform(post(BASE)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("LOCATION_CREATE_AUTHORIZED"));

        assertEquals(1, reached.get());
    }

    @Test
    void warehouseWithCsrfCanPostLocation() throws Exception {
        mvc.perform(post(BASE)
                        .with(user("warehouse").roles("WAREHOUSE"))
                        .with(csrf()))
                .andExpect(status().isOk());

        assertEquals(1, reached.get());
    }

    @Test
    void salesWithCsrfCannotPostLocation() throws Exception {
        mvc.perform(post(BASE)
                        .with(user("sales").roles("SALES"))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        assertEquals(0, reached.get());
    }

    @Test
    void adminWithoutCsrfCannotPostLocation() throws Exception {
        mvc.perform(post(BASE)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden());

        assertEquals(0, reached.get());
    }

    @Test
    void anonymousWithCsrfCannotPostLocation() throws Exception {
        mvc.perform(post(BASE).with(csrf()))
                .andExpect(status().is3xxRedirection());

        assertEquals(0, reached.get());
    }

}
