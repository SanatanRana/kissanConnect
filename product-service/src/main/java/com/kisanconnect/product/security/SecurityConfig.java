package com.kisanconnect.product.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Public: Browsing products
                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                .requestMatchers("/uploads/**").permitAll()
                
                // Internal cross-service stock management (Locked to authenticated services internally)
                // For MVP, we'll permit it or require a system token. 
                // Let's permit it for simplicity within the local network.
                .requestMatchers(HttpMethod.PUT, "/api/products/*/reduce-stock").permitAll()
                .requestMatchers(HttpMethod.PUT, "/api/products/*/increase-stock").permitAll()
                
                // Protected: Only Sellers (Farmers/Shopkeepers) can add or edit
                .requestMatchers(HttpMethod.POST, "/api/products").hasRole("SELLER")
                .requestMatchers(HttpMethod.PUT, "/api/products/*").hasRole("SELLER")
                .requestMatchers(HttpMethod.DELETE, "/api/products/*").hasRole("SELLER")
                
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
