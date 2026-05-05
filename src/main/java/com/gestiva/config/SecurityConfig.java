package com.gestiva.config;

import com.gestiva.security.auth.CustomUserDetailsService;
import com.gestiva.security.auth.TenantUsernamePasswordAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;


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
    public TenantUsernamePasswordAuthenticationFilter tenantUsernamePasswordAuthenticationFilter(AuthenticationManager authenticationManager) {
        TenantUsernamePasswordAuthenticationFilter filter = new TenantUsernamePasswordAuthenticationFilter();
        filter.setAuthenticationManager(authenticationManager);
        filter.setFilterProcessesUrl("/login");
        filter.setUsernameParameter("tenantSlug"); // non usato davvero, ma lasciato consistente
        filter.setPasswordParameter("password");
        filter.setAuthenticationFailureUrl("/login?error");
        filter.setAuthenticationSuccessHandler((request, response, authentication) -> response.sendRedirect("/dashboard"));
        return filter;

    }
    // 🔑 Config della security
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, TenantUsernamePasswordAuthenticationFilter tenantUsernamePasswordAuthenticationFilter) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll() //
                        .requestMatchers("/quotes/**").permitAll()
                        .requestMatchers("/api/quotes/**").permitAll()
                        .requestMatchers("/api/orders/**").permitAll()
                        .requestMatchers("/orders/**").permitAll()
                        .requestMatchers("/customers/**").permitAll()
                        .requestMatchers("/delivery-notes/**").permitAll()
                        .requestMatchers("/api/delivery-notes/**").permitAll()
                        .requestMatchers("/", "/dashboard").permitAll()
                        .requestMatchers("/invoices/**").permitAll()
                        .requestMatchers("/api/invoices/**").permitAll()
                        .anyRequest().authenticated()
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/register", "/css/**", "/js/**", "/images/**").permitAll()
                        .anyRequest().authenticated()

                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                );
        http.addFilterAt(tenantUsernamePasswordAuthenticationFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

}

