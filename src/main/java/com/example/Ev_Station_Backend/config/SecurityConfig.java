package com.example.Ev_Station_Backend.config;

import com.example.Ev_Station_Backend.Security.JwtAuthenticationFilter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
        .csrf(csrf -> csrf.disable())

        .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )

                        .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {

                        response.setStatus(HttpStatus.UNAUTHORIZED.value());
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                        response.getWriter().write(
                        "{\"error\":\"Unauthorized\","
                        + "\"message\":\"Authentication is required or token is invalid\","
                        + "\"status\":401}"
                        );
                })

                .accessDeniedHandler((request, response, accessDeniedException) -> {

                        response.setStatus(HttpStatus.FORBIDDEN.value());
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                        response.getWriter().write(
                        "{\"error\":\"Forbidden\","
                        + "\"message\":\"You do not have permission to access this resource\","
                        + "\"status\":403}"
                        );
                })
                )
                // Public authentication endpoints
                .authorizeHttpRequests(auth -> auth

                .requestMatchers(
                        "/api/auth/register",
                        "/api/auth/login"
                ).permitAll()

                // Role-based access
                .requestMatchers("/api/users/**").hasRole("ADMIN")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/owner/**").hasRole("OWNER")

                // Admin-only station modifications
                .requestMatchers(
                        HttpMethod.POST,
                        "/api/stations",
                        "/api/stations/**"
                ).hasRole("ADMIN")

                .requestMatchers(
                        HttpMethod.DELETE,
                        "/api/stations/**"
                ).hasRole("ADMIN")
                // Admin-only charger modifications
                .requestMatchers(
                        HttpMethod.POST,
                        "/api/chargers",
                        "/api/chargers/**"
                ).hasRole("ADMIN")

                .requestMatchers(
                        HttpMethod.DELETE,
                        "/api/chargers/**"
                ).hasRole("ADMIN")

                // Admin-only connector modifications
                .requestMatchers(
                        HttpMethod.POST,
                        "/api/connectors",
                        "/api/connectors/**"
                ).hasRole("ADMIN")

                .requestMatchers(
                        HttpMethod.DELETE,
                        "/api/connectors/**"
                ).hasRole("ADMIN")

                // Admin-only update operations
                .requestMatchers(
                        HttpMethod.PUT,
                        "/api/stations/**",
                        "/api/chargers/**",
                        "/api/connectors/**"
                ).hasRole("ADMIN")

                .requestMatchers(
                        HttpMethod.PATCH,
                        "/api/stations/**",
                        "/api/chargers/**",
                        "/api/connectors/**"
                ).hasRole("ADMIN")

                // Admin-only BEE import
                .requestMatchers(
                        HttpMethod.POST,
                        "/api/import/bee"
                ).hasRole("ADMIN")

                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}