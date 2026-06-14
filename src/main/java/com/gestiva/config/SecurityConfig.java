package com.gestiva.config;

import com.gestiva.security.auth.CustomUserDetailsService;
import com.gestiva.security.auth.TenantUsernamePasswordAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;

import javax.sql.DataSource;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final DataSource dataSource;

    public SecurityConfig(CustomUserDetailsService userDetailsService, DataSource dataSource) {
        this.userDetailsService = userDetailsService;
        this.dataSource = dataSource;
    }

    @Bean
    public TenantUsernamePasswordAuthenticationFilter tenantUsernamePasswordAuthenticationFilter(
            AuthenticationManager authenticationManager,
            org.springframework.security.web.authentication.RememberMeServices rememberMeServices
    ) {
        TenantUsernamePasswordAuthenticationFilter filter = new TenantUsernamePasswordAuthenticationFilter();
        filter.setAuthenticationManager(authenticationManager);
        filter.setRequiresAuthenticationRequestMatcher(
                new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/login", "POST")
        );
        filter.setPasswordParameter("password");
        filter.setUsernameParameter("email");
        filter.setSecurityContextRepository(
                new org.springframework.security.web.context.HttpSessionSecurityContextRepository()
        );
        filter.setSessionAuthenticationStrategy(
                new org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy()
        );
        filter.setRememberMeServices(rememberMeServices);
        filter.setAuthenticationFailureHandler(
                new org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler("/login?error")
        );
        filter.setAuthenticationSuccessHandler(
                new org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler("/")
        );
        return filter;
    }
    // 🔑 Config della security
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            TenantUsernamePasswordAuthenticationFilter tenantUsernamePasswordAuthenticationFilter,
                                            AuthenticationProvider authenticationProvider
    ) throws Exception {
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/billing/paypal/webhook","/",
                                "/help/chat"))
                .authenticationProvider(authenticationProvider)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/dashboard/**").permitAll()
                                // risorse pubbliche
                                .requestMatchers(
                                        "/css/**",
                                        "/js/**",
                                        "/images/**",
                                        "/webjars/**",
                                        "/login",
                                        "/home",
                                        "/register",
                                        "/error",
                                        "/actuator/health",
                                        "/actuator/info",
                                        "/help/chat/",
                                        "/"
                                ).permitAll()

                                // dashboard
                                .requestMatchers("/dashboard/**")
                                .hasAnyRole("ADMIN", "SALES", "PURCHASING", "WAREHOUSE", "ACCOUNTING")

                                // settings / amministrazione sistema
                                .requestMatchers("/settings/**")
                                .hasRole("ADMIN")
                                .requestMatchers("/admin/users/**")
                                .hasRole("ADMIN")
                                .requestMatchers("/admin/roles/**")
                                .hasRole("ADMIN")

                                // ciclo attivo
                                .requestMatchers("/customers/**")
                                .hasAnyRole("ADMIN", "SALES", "ACCOUNTING")
                                .requestMatchers("/quotes/**")
                                .hasAnyRole("ADMIN", "SALES")
                                .requestMatchers("/orders/**")
                                .hasAnyRole("ADMIN", "SALES")
                                .requestMatchers("/sales-orders/**")
                                .hasAnyRole("ADMIN", "SALES")
                                .requestMatchers("/delivery-notes/**")
                                .hasAnyRole("ADMIN", "SALES", "WAREHOUSE")
                                .requestMatchers("/invoices/**")
                                .hasAnyRole("ADMIN", "SALES", "ACCOUNTING")

                                // ciclo passivo
                                .requestMatchers("/suppliers/**")
                                .hasAnyRole("ADMIN", "PURCHASING", "ACCOUNTING")
                                .requestMatchers("/purchase-orders/**")
                                .hasAnyRole("ADMIN", "PURCHASING")
                                .requestMatchers("/goods-receipts/**")
                                .hasAnyRole("ADMIN", "PURCHASING", "WAREHOUSE")
                                .requestMatchers("/supplier-invoices/**")
                                .hasAnyRole("ADMIN", "PURCHASING", "ACCOUNTING")

                                // magazzino
                                .requestMatchers("/items/**")
                                .hasAnyRole("ADMIN", "SALES", "PURCHASING", "WAREHOUSE", "ACCOUNTING")
                                .requestMatchers("/inventory/**")
                                .hasAnyRole("ADMIN", "WAREHOUSE")
                                .requestMatchers("/inventory-adjustments/**")
                                .hasAnyRole("ADMIN", "WAREHOUSE")
                                .requestMatchers("/inventory-movements/**")
                                .hasAnyRole("ADMIN", "WAREHOUSE")
                                .requestMatchers("/inventory-valuations/**")
                                .hasAnyRole("ADMIN", "WAREHOUSE", "ACCOUNTING")
                                .requestMatchers("/tenant-settings/inventory-valuation/**")
                                .hasAnyRole("ADMIN", "WAREHOUSE")

                                // contabilità
                                .requestMatchers("/payments/**")
                                .hasAnyRole("ADMIN", "ACCOUNTING")
                                .requestMatchers("/payment-dues/**")
                                .hasAnyRole("ADMIN", "ACCOUNTING")
                                .requestMatchers("/accounting-entries/**")
                                .hasAnyRole("ADMIN", "ACCOUNTING")
                                .requestMatchers("/accounting-dashboard/**")
                                .hasAnyRole("ADMIN", "ACCOUNTING")
                                .requestMatchers("/v2/accounts/**")
                                .hasAnyRole("ADMIN", "ACCOUNTING")
                                .requestMatchers("/v2/journal-entries/**")
                                .hasAnyRole("ADMIN", "ACCOUNTING")
                                .requestMatchers("/accounting/**")
                                .hasAnyRole("ADMIN", "ACCOUNTING")
                                .requestMatchers("/vat-registers/**")
                                .hasAnyRole("ADMIN", "ACCOUNTING")

                                // pagina 403
                                .requestMatchers("/403")
                                .authenticated()

                                // tutto il resto
                                .anyRequest().authenticated()

                        )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED)
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/", true)
                )
                // Configurazione rememberMe
                .rememberMe(rememberMe -> rememberMe
                        .rememberMeParameter("rememberMe")
                        .tokenValiditySeconds(2 * 24 * 60 * 60)
                        .key("make")
                        .userDetailsService(userDetailsService)
                        .tokenRepository(persistentTokenRepository(dataSource))
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                );
        http.addFilterAt(
                tenantUsernamePasswordAuthenticationFilter,
                org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class
        );
        return http.build();
    }
    // 🔑 Provider che usa il tuo CustomUserDetailsService
    @Bean
    public DaoAuthenticationProvider authProvider(PasswordEncoder encoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(encoder);
        return authProvider;
    }

    // 🔑 AuthenticationManager da esporre al contesto
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PersistentTokenRepository persistentTokenRepository(DataSource dataSource) {
        JdbcTokenRepositoryImpl repo = new JdbcTokenRepositoryImpl();
        repo.setDataSource(dataSource);
        repo.setCreateTableOnStartup(false);
        return repo;
    }
    @Bean
    public HttpFirewall allowPropfindHttpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        // Aggiungiamo PROPFIND alla lista dei metodi consentiti
        firewall.setAllowedHttpMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS", "PROPFIND"
        ));
        return firewall;
    }

    @Bean
    public org.springframework.security.web.authentication.RememberMeServices rememberMeServices(
            com.gestiva.security.auth.CustomUserDetailsService customUserDetailsService,
            org.springframework.security.web.authentication.rememberme.PersistentTokenRepository persistentTokenRepository
    ) {
        var services = new org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices(
                "gestiva-remember-me-key",
                customUserDetailsService,
                persistentTokenRepository
        );
        services.setParameter("rememberMe");
        services.setTokenValiditySeconds(3 * 24 * 60 * 60);
        return services;
    }

}