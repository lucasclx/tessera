// Arquivo: backend/src/main/java/com/backend/tessera/controller/LoginController.java
package com.backend.tessera.controller;

import com.backend.tessera.dto.*;
import com.backend.tessera.model.AccountStatus;
import com.backend.tessera.model.User;
import com.backend.tessera.repository.UserRepository;
import com.backend.tessera.security.JwtUtil;
import com.backend.tessera.service.AuthTokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthTokenService authTokenService;

    @Value("${app.frontend.url}") // Adicionar esta propriedade em application.properties
    private String frontendBaseUrl;


    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody AuthRequest authRequest) {
        try {
            logger.info("Tentando autenticar usuário: {}", authRequest.getUsername());

            Optional<User> userOpt = userRepository.findByUsername(authRequest.getUsername());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.getStatus() == AccountStatus.PENDENTE) {
                    logger.warn("Tentativa de login com conta pendente de aprovação: {}", authRequest.getUsername());
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(new MessageResponse("Sua conta está aguardando aprovação do administrador. Tente novamente mais tarde."));
                }
                if (user.getRole() == null && user.getStatus() == AccountStatus.ATIVO) {
                    logger.warn("Tentativa de login com conta aprovada mas sem papel atribuído: {}", authRequest.getUsername());
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(new MessageResponse("Sua conta foi aprovada mas não possui um papel atribuído. Entre em contato com o administrador."));
                }
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );

            logger.info("Autenticação bem-sucedida para: {}", authRequest.getUsername());

            final User authenticatedUser = (User) authentication.getPrincipal();
            logger.debug("Detalhes do usuário carregados: {}", authenticatedUser.getUsername());
            logger.debug("Autoridades: {}", authenticatedUser.getAuthorities());

            final String token = jwtUtil.generateToken(authenticatedUser);
            logger.debug("Token gerado para usuário {}: {}...", authRequest.getUsername(), token.substring(0, Math.min(20, token.length())));

            Collection<String> roles = authenticatedUser.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(s -> s.startsWith("ROLE_") ? s.substring(5) : s)
                    .collect(Collectors.toList());

            logger.debug("Roles para resposta (usuário {}): {}", authRequest.getUsername(), roles);

            // Adicionando emailVerified na resposta
            AuthResponse response = new AuthResponse(
                token, 
                authenticatedUser.getUsername(), 
                roles,
                authenticatedUser.isEmailVerified(), // Novo campo
                authenticatedUser.getId(),
                authenticatedUser.getNome()
            );
            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            logger.warn("Credenciais inválidas para: {}", authRequest.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Usuário ou senha inválidos"));
        } catch (LockedException e) {
            logger.warn("Conta bloqueada (pendente/etc.) para: {}: {}", authRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse(e.getMessage()));
        } catch (DisabledException e) {
            logger.warn("Conta desativada para: {}: {}", authRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse(e.getMessage()));
        } catch (AuthenticationException e) {
            logger.warn("Erro de autenticação para: {}: {}", authRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Erro de autenticação: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Erro inesperado na autenticação para {}: {}", authRequest.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Erro interno no servidor durante a autenticação."));
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

            if (user.getStatus() == AccountStatus.INATIVO) {
                logger.info("Usuário com conta INATIVA (via check-approval): {}", username);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new MessageResponse("ACCOUNT_DISABLED"));
            }
            
            if (user.getStatus() == AccountStatus.ATIVO && !user.isEnabled()) {
                 logger.warn("Usuário ATIVO mas desabilitado (enabled=false) (check-approval): {}", username);
                 return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new MessageResponse("ACCOUNT_DISABLED_BY_ADMIN"));
            }

            if (user.getStatus() == AccountStatus.PENDENTE) {
                logger.info("Usuário pendente de aprovação (check-approval): {}", username);
                String messageKey = user.isEmailVerified() ? "PENDING_APPROVAL_EMAIL_VERIFIED" : "PENDING_APPROVAL_EMAIL_NOT_VERIFIED";
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new MessageResponse(messageKey));
            }

            if (user.getRole() == null && user.getStatus() == AccountStatus.ATIVO) {
                logger.warn("Usuário aprovado mas sem papel atribuído (check-approval): {}", username);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new MessageResponse("ROLE_MISSING"));
            }
            
            logger.info("Usuário aprovado e ativo (check-approval): {}. E-mail verificado: {}", username, user.isEmailVerified());
            return ResponseEntity.ok(new MessageResponse(user.isEmailVerified() ? "APPROVED_EMAIL_VERIFIED" : "APPROVED_EMAIL_NOT_VERIFIED"));

        } catch (Exception e) {
            logger.error("Erro ao verificar status de aprovação para {}: {}", username, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Erro ao verificar status: " + e.getMessage()));
        }
    }

    @GetMapping("/confirm-email")
    public ResponseEntity<?> confirmEmail(@RequestParam("token") String token) {
        logger.debug("Tentando confirmar e-mail com token: {}", token);
        boolean success = authTokenService.verifyEmailToken(token);
        if (success) {
            logger.info("E-mail confirmado com sucesso para o token: {}", token);
            // Redirecionar para uma página de sucesso no frontend
            return ResponseEntity.status(HttpStatus.FOUND).header("Location", frontendBaseUrl + "/email-confirmado").build();
            // Ou retornar uma mensagem JSON
            // return ResponseEntity.ok(new MessageResponse("E-mail confirmado com sucesso! Você já pode fazer login se sua conta for aprovada por um administrador."));
        } else {
            logger.warn("Falha ao confirmar e-mail. Token inválido ou expirado: {}", token);
             // Redirecionar para uma página de falha no frontend
            return ResponseEntity.status(HttpStatus.FOUND).header("Location", frontendBaseUrl + "/email-confirmacao-falhou").build();
            // Ou retornar uma mensagem JSON
            // return ResponseEntity.badRequest().body(new MessageResponse("Link de confirmação inválido ou expirado. Por favor, solicite um novo e-mail de confirmação."));
        }
    }

    @PostMapping("/resend-verification-email")
    public ResponseEntity<?> resendVerificationEmail(@Valid @RequestBody EmailRequest emailRequest, HttpServletRequest request) {
        logger.debug("Solicitação para reenviar e-mail de verificação para: {}", emailRequest.getEmail());
        String siteBaseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                        .replacePath(null)
                        .build()
                        .toUriString();
        boolean emailSent = authTokenService.resendVerificationEmail(emailRequest.getEmail(), siteBaseUrl);
        if (emailSent) {
            logger.info("E-mail de verificação reenviado para: {}", emailRequest.getEmail());
            return ResponseEntity.ok(new MessageResponse("Se o e-mail estiver registrado e não verificado, um novo link de confirmação foi enviado."));
        } else {
            logger.warn("Não foi possível reenviar e-mail de verificação para: {} (usuário não encontrado, já verificado ou erro).", emailRequest.getEmail());
            return ResponseEntity.ok(new MessageResponse("Se o e-mail estiver registrado e não verificado, um novo link de confirmação foi enviado.")); // Mensagem genérica para não revelar status
        }
    }

    @PostMapping("/request-password-reset")
    public ResponseEntity<?> requestPasswordReset(@Valid @RequestBody EmailRequest emailRequest, HttpServletRequest request) {
        logger.debug("Solicitação de redefinição de senha para e-mail: {}", emailRequest.getEmail());
        String siteBaseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(null)
                .build()
                .toUriString();
        
        // Passa a URL base do frontend para o serviço
        boolean success = authTokenService.generateAndSendPasswordResetToken(emailRequest.getEmail(), frontendBaseUrl); 
        
        if (success) { // O serviço sempre retorna true para não revelar se o email existe
             logger.info("Processo de solicitação de redefinição de senha iniciado para: {}", emailRequest.getEmail());
        } else {
            // Este bloco não deve ser alcançado se o AuthTokenService sempre retornar true
            logger.error("Falha inesperada ao processar solicitação de redefinição de senha para {}", emailRequest.getEmail());
        }
        return ResponseEntity.ok(new MessageResponse("Se o seu endereço de e-mail estiver em nosso sistema, você receberá um link para redefinir sua senha em breve."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody PasswordResetRequest passwordResetRequest) {
        logger.debug("Tentativa de redefinir senha com token: {}", passwordResetRequest.getToken().substring(0, Math.min(8, passwordResetRequest.getToken().length())) + "...");
        boolean success = authTokenService.resetPassword(passwordResetRequest.getToken(), passwordResetRequest.getNewPassword());
        if (success) {
            logger.info("Senha redefinida com sucesso para o token: {}", passwordResetRequest.getToken().substring(0, Math.min(8, passwordResetRequest.getToken().length())) + "...");
            return ResponseEntity.ok(new MessageResponse("Sua senha foi redefinida com sucesso."));
        } else {
            logger.warn("Falha ao redefinir senha. Token inválido ou expirado para: {}", passwordResetRequest.getToken().substring(0, Math.min(8, passwordResetRequest.getToken().length())) + "...");
            return ResponseEntity.badRequest().body(new MessageResponse("Token de redefinição de senha inválido ou expirado. Por favor, solicite um novo."));
        }
    }
}