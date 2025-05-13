package com.backend.tessera.security;

import com.backend.tessera.config.LoggerConfig;
import com.backend.tessera.service.UserDetailsServiceImpl;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
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
    private static final Logger logger = LoggerConfig.getLogger(JwtAuthenticationFilter.class);

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
        
        logger.debug("Request URI: {}", requestURI);
        if (authorizationHeader != null) {
            logger.debug("Auth Header presente: {}...", authorizationHeader.substring(0, Math.min(20, authorizationHeader.length())));
        }

        // Ignorar certas URLs (como login, registro e verificação de aprovação)
        if (requestURI.contains("/api/auth/") || requestURI.contains("/actuator/") || requestURI.contains("/api/system/")) {
            logger.debug("Pulando autenticação para endpoint público: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
                logger.debug("Usuário extraído do token: {}", username);
            } catch (ExpiredJwtException e) {
                logger.warn("JWT Token expirado");
                // Não lançar exceção, deixar a autenticação ser rejeitada normalmente
                filterChain.doFilter(request, response);
                return;
            } catch (Exception e) {
                logger.error("Erro ao processar JWT Token: {}", e.getMessage());
                // Não lançar exceção, deixar a autenticação ser rejeitada normalmente
                filterChain.doFilter(request, response);
                return;
            }
        } else {
            logger.debug("Cabeçalho Authorization não encontrado ou sem prefixo Bearer");
            filterChain.doFilter(request, response);
            return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                logger.debug("Detalhes do usuário carregados: {}", userDetails.getUsername());
                logger.debug("Autoridades: {}", userDetails.getAuthorities());
                
                boolean isValid = jwtUtil.validateToken(jwt, userDetails);
                logger.debug("Token válido? {}", isValid);
                
                if (isValid) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.debug("Autenticação definida com sucesso no SecurityContextHolder");
                } else {
                    logger.debug("Token inválido, autenticação não realizada");
                }
            } catch (Exception e) {
                logger.error("Erro durante autenticação: {}", e.getMessage());
                // Não interromper o filtro em caso de erro, permitir que a cadeia de filtros continue
            }
        }
        
        filterChain.doFilter(request, response);
    }
}