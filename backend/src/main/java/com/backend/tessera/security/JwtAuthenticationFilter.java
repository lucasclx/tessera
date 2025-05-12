package com.backend.tessera.security;

import com.backend.tessera.auth.service.UserDetailsServiceImpl;
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

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

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

        final String requestURI = request.getRequestURI();
        logger.debug("Filtrando requisição para: {}", requestURI);

        // Endpoints que não exigem JWT para acesso inicial
        if (requestURI.startsWith("/api/auth/") || 
            requestURI.startsWith("/actuator/")) {
            logger.debug("Pulando filtro JWT para endpoint público: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        final String authorizationHeader = request.getHeader("Authorization");
        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
                logger.debug("Token JWT válido para usuário: {}", username);
            } catch (ExpiredJwtException e) {
                logger.warn("Token JWT expirado para usuário: {}", 
                    e.getClaims() != null ? e.getClaims().getSubject() : "desconhecido");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"unauthorized\",\"message\":\"Token expirado. Por favor, faça login novamente.\"}");
                return;
            } catch (Exception e) {
                logger.error("Erro ao processar token JWT: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"unauthorized\",\"message\":\"Token inválido. Por favor, faça login novamente.\"}");
                return;
            }
        } else {
            logger.debug("Nenhum token Bearer encontrado no cabeçalho Authorization");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"unauthorized\",\"message\":\"Autenticação necessária. Por favor, faça login.\"}");
            return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                logger.debug("Detalhes do usuário carregados: {}", userDetails.getUsername());
                
                if (jwtUtil.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.debug("Autenticação JWT definida para usuário: {}", username);
                    
                    // Continue a cadeia de filtros
                    filterChain.doFilter(request, response);
                } else {
                    logger.warn("Token JWT inválido para usuário: {}", username);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"unauthorized\",\"message\":\"Token inválido. Por favor, faça login novamente.\"}");
                }
            } catch (Exception e) {
                logger.error("Erro ao validar token ou carregar UserDetails: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"unauthorized\",\"message\":\"Erro de autenticação: " + e.getMessage() + "\"}");
            }
        } else {
            // Continue a cadeia de filtros se autenticação já estiver presente
            filterChain.doFilter(request, response);
        }
    }
}
