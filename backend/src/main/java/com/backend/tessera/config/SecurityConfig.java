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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private CustomAuthenticationProvider customAuthenticationProvider;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/dashboard/professor/**").hasRole("PROFESSOR")
                .requestMatchers("/api/dashboard/aluno/**").hasRole("ALUNO")
                
                // --- REGRA DE AUTORIZAÇÃO PARA MONOGRAFIAS (AJUSTADA/ADICIONADA) ---
                .requestMatchers(HttpMethod.GET, "/api/monografias").hasAnyRole("ALUNO", "PROFESSOR", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/monografias").hasAnyRole("ALUNO", "PROFESSOR") // Exemplo: quem pode criar
                .requestMatchers(HttpMethod.GET, "/api/monografias/{id}/**").hasAnyRole("ALUNO", "PROFESSOR", "ADMIN") // Ex: para buscar uma específica
                .requestMatchers(HttpMethod.PUT, "/api/monografias/{id}/**").hasAnyRole("ALUNO", "PROFESSOR") // Ex: quem pode atualizar
                .requestMatchers(HttpMethod.DELETE, "/api/monografias/{id}/**").hasAnyRole("ADMIN", "PROFESSOR") // Ex: quem pode deletar
                // --- FIM DA REGRA DE MONOGRAFIAS ---

                // Regras para outros módulos (versao, comentario, etc.) devem ser adicionadas aqui
                .requestMatchers("/api/versoes/**").authenticated() // Exemplo genérico, ajuste as roles
                .requestMatchers("/api/comentarios/**").authenticated() // Exemplo genérico, ajuste as roles

                .requestMatchers("/actuator/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    String jsonErrorResponse = String.format("{\"timestamp\": \"%s\", \"status\": %d, \"error\": \"Unauthorized\", \"message\": \"%s\", \"path\": \"%s\"}",
                            java.time.LocalDateTime.now(), HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage(), request.getRequestURI());
                    response.getWriter().write(jsonErrorResponse);
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    String jsonErrorResponse = String.format("{\"timestamp\": \"%s\", \"status\": %d, \"error\": \"Forbidden\", \"message\": \"%s\", \"path\": \"%s\"}",
                            java.time.LocalDateTime.now(), HttpServletResponse.SC_FORBIDDEN, accessDeniedException.getMessage(), request.getRequestURI());
                    response.getWriter().write(jsonErrorResponse);
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
        configuration.setAllowedOrigins(List.of("http://localhost:4200", "http://127.0.0.1:4200")); // Use List.of
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}