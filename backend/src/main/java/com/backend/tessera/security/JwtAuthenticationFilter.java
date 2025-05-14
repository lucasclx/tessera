package com.backend.tessera.security;

import com.backend.tessera.service.UserDetailsServiceImpl;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger customLogger = LoggerFactory.getLogger(JwtAuthenticationFilter.class); // Renomeado para evitar conflito com 'logger' herdado

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");
        final String requestURI = request.getRequestURI();

        customLogger.debug("Request URI: {}", requestURI);
        customLogger.debug("Auth Header: {}", (authorizationHeader != null ?
                authorizationHeader.substring(0, Math.min(20, authorizationHeader.length())) + "..." : "null"));

        // Ignorar certas URLs (como login, registro e verificação de aprovação)
        if (requestURI.contains("/api/auth/") || requestURI.contains("/actuator/")) {
            customLogger.debug("Pulando autenticação para endpoint público: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
                customLogger.debug("Usuário extraído do token: {}", username);
            } catch (ExpiredJwtException e) {
                customLogger.warn("JWT Token expirado para o token: {}...", (jwt != null ? jwt.substring(0, Math.min(20, jwt.length())) : "null"));
                filterChain.doFilter(request, response);
                return;
            } catch (Exception e) {
                customLogger.error("Erro ao processar JWT Token: {}...", (jwt != null ? jwt.substring(0, Math.min(20, jwt.length())) : "null"), e);
                filterChain.doFilter(request, response);
                return;
            }
        } else {
            customLogger.debug("Cabeçalho Authorization não encontrado ou sem prefixo Bearer para URI: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                customLogger.debug("Detalhes do usuário carregados: {}", userDetails.getUsername());
                customLogger.debug("Autoridades: {}", userDetails.getAuthorities());

                boolean isValid = jwtUtil.validateToken(jwt, userDetails);
                customLogger.debug("Token válido? {}", isValid);

                if (isValid) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    customLogger.debug("Autenticação definida com sucesso no SecurityContextHolder para usuário: {}", username);
                } else {
                    customLogger.warn("Token inválido, autenticação não realizada para usuário: {}", username);
                }
            } catch (Exception e) {
                customLogger.error("Erro durante autenticação para usuário {}: {}", username, e.getMessage(), e);
            }
        }

        filterChain.doFilter(request, response);
    }
}