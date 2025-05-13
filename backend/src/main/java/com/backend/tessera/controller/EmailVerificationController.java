package com.backend.tessera.controller;

import com.backend.tessera.dto.MessageResponse;
import com.backend.tessera.dto.ResendVerificationRequest;
import com.backend.tessera.service.EmailVerificationService;
import com.backend.tessera.config.LoggerConfig; // Importar LoggerConfig
import org.slf4j.Logger; // Importar Logger
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Removido: import jakarta.mail.MessagingException;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth/email")
public class EmailVerificationController {
    private static final Logger logger = LoggerConfig.getLogger(EmailVerificationController.class); // Adicionar logger

    @Autowired
    private EmailVerificationService emailVerificationService;

    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        logger.debug("Recebida solicitação para verificar token: {}...", token.substring(0, Math.min(token.length(), 8))); // Log adicionado
        boolean verified = emailVerificationService.verifyEmail(token);

        if (verified) {
            logger.info("Token verificado com sucesso: {}...", token.substring(0, Math.min(token.length(), 8))); // Log adicionado
            return ResponseEntity.ok(new MessageResponse("Email verificado com sucesso. Agora você pode fazer login."));
        } else {
            logger.warn("Falha ao verificar token: {}...", token.substring(0, Math.min(token.length(), 8))); // Log adicionado
            return ResponseEntity.badRequest().body(new MessageResponse(
                    "Não foi possível verificar seu email. O token pode ser inválido ou expirado."));
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerificationEmail(@Valid @RequestBody ResendVerificationRequest request) {
        logger.debug("Recebida solicitação para reenviar verificação para email: {}", request.getEmail()); // Log adicionado
        try {
            emailVerificationService.resendVerificationEmail(request.getEmail());
            logger.info("Processada solicitação de reenvio para email: {}", request.getEmail()); // Log adicionado
            // A mensagem de sucesso é a mesma, independentemente se o email existe ou não, por segurança.
            return ResponseEntity.ok(new MessageResponse(
                    "Se seu email estiver cadastrado e não verificado, você receberá um novo link de verificação."));
        } catch (Exception e) { // Captura genérica para erros inesperados
             logger.error("Erro inesperado ao processar reenvio de verificação para {}: {}", request.getEmail(), e.getMessage(), e); // Log de erro genérico
            return ResponseEntity.internalServerError().body(new MessageResponse(
                    "Erro ao processar a solicitação de reenvio de email. Por favor, tente novamente mais tarde."));
        }
    }
}