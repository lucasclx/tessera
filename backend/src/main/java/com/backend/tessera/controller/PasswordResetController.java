package com.backend.tessera.controller;

import com.backend.tessera.config.LoggerConfig; // Importar LoggerConfig
import com.backend.tessera.dto.MessageResponse;
import com.backend.tessera.dto.PasswordResetRequest;
import com.backend.tessera.dto.PasswordResetTokenRequest;
import com.backend.tessera.service.PasswordResetService;
import org.slf4j.Logger; // Importar Logger
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Removido: import jakarta.mail.MessagingException; // Esta importação não é mais necessária aqui
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth/password")
public class PasswordResetController {
    private static final Logger logger = LoggerConfig.getLogger(PasswordResetController.class); // Adicionar logger

    @Autowired
    private PasswordResetService passwordResetService;

    @PostMapping("/reset-request")
    public ResponseEntity<?> requestPasswordReset(@Valid @RequestBody PasswordResetTokenRequest request) {
        logger.debug("Recebida solicitação de redefinição de senha para email: {}", request.getEmail()); // Log adicionado
        try {
            passwordResetService.requestPasswordReset(request.getEmail());
            // A mensagem de sucesso é a mesma independentemente se o email existe ou não por segurança
            logger.info("Processada solicitação de redefinição de senha para email: {}", request.getEmail()); // Log adicionado
            return ResponseEntity.ok(new MessageResponse(
                    "Se seu email estiver cadastrado em nosso sistema, você receberá um link para redefinição de senha."));
        } catch (Exception e) { // Alterado para Exception genérica
            logger.error("Erro inesperado ao processar solicitação de redefinição de senha para {}: {}", request.getEmail(), e.getMessage(), e); // Log de erro
            return ResponseEntity.internalServerError().body(new MessageResponse(
                    "Erro ao processar a solicitação de redefinição de senha. Por favor, tente novamente mais tarde."));
        }
    }

    @GetMapping("/validate-token")
    public ResponseEntity<?> validatePasswordResetToken(@RequestParam String token) {
        logger.debug("Validando token de redefinição de senha: {}...", token.substring(0, Math.min(token.length(), 8))); // Log adicionado
        boolean isValid = passwordResetService.validatePasswordResetToken(token);

        if (isValid) {
            logger.info("Token de redefinição de senha válido: {}...", token.substring(0, Math.min(token.length(), 8))); // Log adicionado
            return ResponseEntity.ok(new MessageResponse("Token válido."));
        } else {
            logger.warn("Token de redefinição de senha inválido ou expirado: {}...", token.substring(0, Math.min(token.length(), 8))); // Log adicionado
            return ResponseEntity.badRequest().body(new MessageResponse(
                    "Token inválido ou expirado. Por favor, solicite uma nova redefinição de senha."));
        }
    }

    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        logger.debug("Tentativa de redefinição de senha com token: {}...", request.getToken().substring(0, Math.min(request.getToken().length(), 8))); // Log adicionado
        boolean success = passwordResetService.resetPassword(request.getToken(), request.getNewPassword());

        if (success) {
            logger.info("Senha redefinida com sucesso para o token: {}...", request.getToken().substring(0, Math.min(request.getToken().length(), 8))); // Log adicionado
            return ResponseEntity.ok(new MessageResponse("Senha redefinida com sucesso. Você já pode fazer login."));
        } else {
            logger.warn("Falha ao redefinir senha para o token: {}...", request.getToken().substring(0, Math.min(request.getToken().length(), 8))); // Log adicionado
            return ResponseEntity.badRequest().body(new MessageResponse(
                    "Não foi possível redefinir sua senha. O token pode ser inválido ou expirado."));
        }
    }
}