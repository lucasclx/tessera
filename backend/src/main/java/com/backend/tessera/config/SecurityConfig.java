// Arquivo: backend/src/main/java/com/backend/tessera/config/SecurityConfig.java
package com.backend.tessera.config;

import com.backend.tessera.security.CustomAuthenticationProvider;
import com.backend.tessera.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

// Imports que estavam faltando para as exceções
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.DisabledException;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Autowired
    private CustomAuthenticationProvider customAuthenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/api/auth/register").permitAll()
                .requestMatchers("/api/auth/check-approval/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/auth/confirm-email").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/resend-verification-email").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/request-password-reset").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/reset-password").permitAll()
                .requestMatchers("/actuator/**").permitAll() 
                .requestMatchers(HttpMethod.GET, "/api/dashboard/professor/**").hasRole("PROFESSOR")
                .requestMatchers(HttpMethod.GET, "/api/dashboard/aluno/**").hasRole("ALUNO")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    if (request.getRequestURI().startsWith("/api/auth/login") && authException instanceof BadCredentialsException) {
                         response.getWriter().write("{\"message\": \"Usuário ou senha inválidos\"}");
                    } else if (request.getRequestURI().startsWith("/api/auth/login") && authException instanceof LockedException) {
                         response.getWriter().write("{\"message\": \"" + authException.getMessage() + "\"}");
                    } else if (request.getRequestURI().startsWith("/api/auth/login") && authException instanceof DisabledException) {
                         response.getWriter().write("{\"message\": \"" + authException.getMessage() + "\"}");
                    }
                    else {
                         response.getWriter().write("{\"message\": \"Autenticação necessária para acessar este recurso.\"}");
                    }
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"message\": \"Acesso Proibido: Você não tem permissão para acessar este recurso.\"}");
                })
            );
        
        http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

        return http.build();
    }
    
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(customAuthenticationProvider);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200", "http://127.0.0.1:4200", "http://localhost:3000")); 
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type", "Location"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}