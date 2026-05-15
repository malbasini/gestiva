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
                new org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler("/dashboard")
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

                .authenticationProvider(authenticationProvider)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/register", "/css/**", "/js/**", "/images/**", "/error").permitAll()
                        .requestMatchers("/dashboard/**").permitAll()
                        .anyRequest().authenticated()

                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED)
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
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