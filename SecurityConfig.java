package com.course.management.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

/**
 * Configuration class to set up necessary beans for security (password hashing)
 * and disable default web security features since this is a CLI application.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Exposes a BCryptPasswordEncoder bean used by UserService for hashing passwords
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Disables Spring Security's default web protection.
     * This is essential for a CommandLineRunner application to function without
     * requiring web requests to be authenticated.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF protection for simplicity in a CLI context
            .csrf(AbstractHttpConfigurer::disable)
            // Allow all requests to pass without authentication
            .authorizeHttpRequests(authorize -> authorize
                .anyRequest().permitAll()
            );
        return http.build();
    }
}
