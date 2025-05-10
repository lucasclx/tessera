package com.backend.tessera.security;

import com.backend.tessera.auth.service.UserDetailsServiceImpl; // Atualizado
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsServiceImpl userDetailsService; // Vem de auth.service

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");
        final String requestURI = request.getRequestURI();

        // System.out.println("Request URI: " + requestURI);
        // System.out.println("Auth Header: " + (authorizationHeader != null ?
        //         authorizationHeader.substring(0, Math.min(20, authorizationHeader.length())) + "..." : "null"));

        // Endpoints que não exigem JWT para acesso inicial. A segurança deles é definida no SecurityConfig.
        if (requestURI.startsWith("/api/auth/") || // Login, Register, Check-Approval
            requestURI.startsWith("/actuator/")) {
            // System.out.println("Pulando filtro JWT para endpoint público: " + requestURI);
            filterChain.doFilter(request, response);
            return;
        }
        // Os demais endpoints, incluindo /api/admin/** e /api/dashboard/**, passarão pela validação do token.

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
                // System.out.println("Usuário extraído do token: " + username);
            } catch (ExpiredJwtException e) {
                System.out.println("JWT Token expirado para " + e.getClaims().getSubject());
                // response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                // response.setContentType("application/json");
                // response.getWriter().write("{\"message\": \"Token expirado.\"}");
                filterChain.doFilter(request, response); // Deixa o SecurityConfig lidar com a resposta de não autorizado
                return;
            } catch (Exception e) {
                System.out.println("Erro ao processar JWT Token: " + e.getMessage());
                // response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                // response.setContentType("application/json");
                // response.getWriter().write("{\"message\": \"Token inválido.\"}");
                filterChain.doFilter(request, response); // Deixa o SecurityConfig lidar
                return;
            }
        } else {
             //System.out.println("Cabeçalho Authorization não encontrado ou sem prefixo Bearer para: " + requestURI);
             // Para endpoints que não são /api/auth/** ou /actuator/**, isso resultará em falha de autenticação
             // que será tratada pelo authenticationEntryPoint no SecurityConfig
            filterChain.doFilter(request, response);
            return;
        }


        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                // System.out.println("Detalhes do usuário carregados para validação JWT: " + userDetails.getUsername());
                // System.out.println("Autoridades para validação JWT: " + userDetails.getAuthorities());

                boolean isValid = jwtUtil.validateToken(jwt, userDetails);
                // System.out.println("Token válido para " + username + "? " + isValid);

                if (isValid) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    // System.out.println("Autenticação JWT definida com sucesso no SecurityContextHolder para: " + username);
                } else {
                    System.out.println("Token JWT inválido, autenticação não realizada para: " + username);
                    // SecurityContextHolder.clearContext(); // Garante que não haja contexto de segurança antigo
                }
            } catch (Exception e) {
                System.out.println("Erro durante a validação do token ou carregamento do UserDetails: " + e.getMessage());
                // SecurityContextHolder.clearContext();
            }
        } else if (username == null) {
            System.out.println("Username não extraído do token, ou token não fornecido onde esperado.");
        }


        filterChain.doFilter(request, response);
    }
}