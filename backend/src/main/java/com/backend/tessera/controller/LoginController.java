package com.backend.tessera.controller;

import com.backend.tessera.config.LoggerConfig;
import com.backend.tessera.dto.*;
import com.backend.tessera.model.AccountStatus;
import com.backend.tessera.model.RefreshToken;
import com.backend.tessera.model.User;
import com.backend.tessera.repository.UserRepository;
import com.backend.tessera.security.JwtUtil;
import com.backend.tessera.service.RefreshTokenService;
import com.backend.tessera.service.UserDetailsServiceImpl;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class LoginController {
    private static final Logger logger = LoggerConfig.getLogger(LoginController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(
            @Valid @RequestBody AuthRequest authRequest,
            HttpServletRequest request) {
        try {
            logger.debug("Tentando autenticar usuário: {}", authRequest.getUsername());
            
            Optional<User> userOpt = userRepository.findByUsername(authRequest.getUsername());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                if (user.getStatus() == AccountStatus.REJEITADO) {
                    logger.info("Tentativa de login com conta rejeitada: {}", authRequest.getUsername());
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                           .body(new MessageResponse("Sua conta foi rejeitada pelo administrador. Entre em contato para mais informações."));
                }
                
                if (user.getStatus() == AccountStatus.PENDENTE) {
                    logger.info("Tentativa de login com conta pendente: {}", authRequest.getUsername());
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                           .body(new MessageResponse("Sua conta está aguardando aprovação do administrador. Tente novamente mais tarde."));
                }
                
                if (user.getRole() == null && user.isApproved()) {
                    logger.warn("Conta aprovada sem papel atribuído: {}", authRequest.getUsername());
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                           .body(new MessageResponse("Sua conta foi aprovada mas não possui um papel atribuído. Entre em contato com o administrador."));
                }
            }
            
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );
            
            logger.info("Autenticação bem-sucedida para: {}", authRequest.getUsername());
            
            final UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            logger.debug("Detalhes do usuário carregados: {}", userDetails.getUsername());
            logger.debug("Autoridades: {}", userDetails.getAuthorities());
            
            // Gerar access token
            final String accessToken = jwtUtil.generateToken(userDetails);
            logger.debug("Token gerado para usuário: {}", userDetails.getUsername());
            
            // Criar um refresh token
            String userAgent = request.getHeader("User-Agent");
            String ipAddress = request.getRemoteAddr();
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(
                    userDetails.getUsername(), userAgent, ipAddress);
            
            Collection<String> roles = userDetails.getAuthorities().stream()
                                     .map(GrantedAuthority::getAuthority)
                                     .map(s -> s.startsWith("ROLE_") ? s.substring(5) : s)
                                     .collect(Collectors.toList());
            
            logger.debug("Roles para resposta: {}", roles);
            
            // Resposta com access token e refresh token
            AuthResponse response = new AuthResponse(accessToken, refreshToken.getToken(), 
                                                   userDetails.getUsername(), roles);
            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            logger.warn("Credenciais inválidas para: {}", authRequest.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(new MessageResponse("Usuário ou senha inválidos"));
        } catch (LockedException e) {
            logger.warn("Conta bloqueada (pendente/rejeitada): {}", authRequest.getUsername());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                   .body(new MessageResponse(e.getMessage()));
        } catch (DisabledException e) {
            logger.warn("Conta desativada: {}", authRequest.getUsername());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                   .body(new MessageResponse(e.getMessage()));
        } catch (AuthenticationException e) {
            logger.warn("Erro de autenticação para: {} - {}", authRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                   .body(new MessageResponse("Erro de autenticação: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Erro inesperado na autenticação para {} - {}", authRequest.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                   .body(new MessageResponse("Erro interno no servidor durante a autenticação."));
        }
    }
    
    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();
        logger.debug("Solicitação de renovação de token com refresh token: {}...", 
                    requestRefreshToken.substring(0, Math.min(10, requestRefreshToken.length())));

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    // Gerar novo access token
                    UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
                    String accessToken = jwtUtil.generateToken(userDetails);
                    
                    logger.info("Token renovado com sucesso para usuário: {}", user.getUsername());
                    return ResponseEntity.ok(new TokenRefreshResponse(accessToken, requestRefreshToken));
                })
                .orElseThrow(() -> {
                    logger.warn("Refresh token não encontrado ou inválido: {}...", 
                               requestRefreshToken.substring(0, Math.min(10, requestRefreshToken.length())));
                    return new RuntimeException("Refresh token não encontrado ou inválido! Por favor, faça login novamente.");
                });
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(@Valid @RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        logger.debug("Solicitação de logout com refresh token: {}...", 
                    refreshToken.substring(0, Math.min(10, refreshToken.length())));
        
        try {
            refreshTokenService.revokeToken(refreshToken);
            logger.info("Logout realizado com sucesso para o token: {}...", 
                        refreshToken.substring(0, Math.min(10, refreshToken.length())));
            return ResponseEntity.ok(new MessageResponse("Logout realizado com sucesso!"));
        } catch (Exception e) {
            logger.error("Erro ao realizar logout: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse("Erro ao realizar logout: " + e.getMessage()));
        }
    }
    
    @GetMapping("/check-approval/{username}")
    public ResponseEntity<?> checkApprovalStatus(@PathVariable String username) {
        try {
            logger.debug("Verificando status de aprovação para: {}", username);
            Optional<User> userOpt = userRepository.findByUsername(username);
            
            if (userOpt.isEmpty()) {
                logger.warn("Usuário não encontrado na verificação de aprovação: {}", username);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                       .body(new MessageResponse("Usuário não encontrado"));
            }
            
            User user = userOpt.get();
            
            // MODIFICADO: Ordem das verificações e inclusão de REJEITADO
            if (user.getStatus() == AccountStatus.INATIVO) {
                logger.debug("Usuário com conta INATIVA: {}", username);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                       .body(new MessageResponse("ACCOUNT_DISABLED"));
            }
            
            if (user.getStatus() == AccountStatus.REJEITADO) {
                logger.debug("Usuário com conta REJEITADA: {}", username);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                       .body(new MessageResponse("ACCOUNT_REJECTED"));
            }
            
            if (user.getStatus() == AccountStatus.PENDENTE) { 
                logger.debug("Usuário pendente de aprovação: {}", username);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                       .body(new MessageResponse("PENDING_APPROVAL"));
            } 
            
            // Se chegou aqui, usuário está ATIVO
            if (user.getRole() == null) {
                logger.warn("Usuário aprovado mas sem papel atribuído: {}", username);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                       .body(new MessageResponse("ROLE_MISSING"));
            }
            
            logger.debug("Usuário aprovado e ativo: {}", username);
            return ResponseEntity.ok(new MessageResponse("APPROVED"));

        } catch (Exception e) {
            logger.error("Erro ao verificar status de aprovação para {}: {}", username, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                   .body(new MessageResponse("Erro ao verificar status: " + e.getMessage()));
        }
    }
}