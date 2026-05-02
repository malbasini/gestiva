package com.gestiva.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;

@Configuration
public class SecurityConfig {

    // 🔑 Config della security
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll() //
                        .requestMatchers("/quotes/**").permitAll()
                        .requestMatchers("/api/quotes/**").permitAll()
                        .requestMatchers("/api/orders/**").permitAll()
                        .requestMatchers("/orders/**").permitAll()
                        .requestMatchers("/customers/**").permitAll()
                        .requestMatchers("/delivery-notes/**").permitAll()
                        .requestMatchers("/", "/dashboard").permitAll()

                        .anyRequest().authenticated()
                )
                .formLogin(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }


}

