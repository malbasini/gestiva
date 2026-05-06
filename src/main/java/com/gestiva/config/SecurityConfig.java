package com.gestiva.config;

import com.gestiva.security.auth.CustomUserDetailsService;
import com.gestiva.security.auth.TenantUsernamePasswordAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;


@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public UserDetailsService userDetailsService(CustomUserDetailsService customUserDetailsService) {
        return customUserDetailsService;
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
    @Bean
    public TenantUsernamePasswordAuthenticationFilter tenantUsernamePasswordAuthenticationFilter(
            AuthenticationManager authenticationManager
    ) {
        TenantUsernamePasswordAuthenticationFilter filter = new TenantUsernamePasswordAuthenticationFilter();
        filter.setAuthenticationManager(authenticationManager);
        filter.setRequiresAuthenticationRequestMatcher(
                new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/login", "POST")
        );
        filter.setPasswordParameter("password");
        filter.setSecurityContextRepository(
                new org.springframework.security.web.context.HttpSessionSecurityContextRepository()
        );
        filter.setSessionAuthenticationStrategy(
                new org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy()
        );
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
                                            AuthenticationProvider authenticationProvider,
                                            org.springframework.security.web.authentication.rememberme.PersistentTokenRepository persistentTokenRepository,
                                            com.gestiva.security.auth.CustomUserDetailsService customUserDetailsService
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
                        .permitAll()
                )
                .rememberMe(remember -> remember
                        .rememberMeParameter("rememberMe")
                        .tokenRepository(persistentTokenRepository)
                        .tokenValiditySeconds(3 * 24 * 60 * 60)
                        .userDetailsService(customUserDetailsService)
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
    @Bean
    public org.springframework.security.authentication.dao.DaoAuthenticationProvider authenticationProvider(
            CustomUserDetailsService customUserDetailsService,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder
    ) {
        var provider = new org.springframework.security.authentication.dao.DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }
    @Bean
    public org.springframework.security.web.authentication.rememberme.PersistentTokenRepository persistentTokenRepository(
            javax.sql.DataSource dataSource
    ) {
        var repository = new org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl();
        repository.setDataSource(dataSource);
        return repository;
    }
}

