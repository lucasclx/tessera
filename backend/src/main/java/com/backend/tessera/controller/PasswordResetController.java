package com.backend.tessera.controller;

import com.backend.tessera.dto.MessageResponse;
import com.backend.tessera.dto.PasswordResetRequest;
import com.backend.tessera.dto.PasswordResetTokenRequest;
import com.backend.tessera.service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth/password")
public class PasswordResetController {

    @Autowired
    private PasswordResetService passwordResetService;

    @PostMapping("/reset-request")
    public ResponseEntity<?> requestPasswordReset(@Valid @RequestBody PasswordResetTokenRequest request) {
        try {
            passwordResetService.requestPasswordReset(request.getEmail());
            return ResponseEntity.ok(new MessageResponse(
                    "Se seu email estiver cadastrado em nosso sistema, você receberá um link para redefinição de senha."));
        } catch (MessagingException e) {
            return ResponseEntity.internalServerError().body(new MessageResponse(
                    "Erro ao enviar email de redefinição de senha. Por favor, tente novamente mais tarde."));
        }
    }

    @GetMapping("/validate-token")
    public ResponseEntity<?> validatePasswordResetToken(@RequestParam String token) {
        boolean isValid = passwordResetService.validatePasswordResetToken(token);
        
        if (isValid) {
            return ResponseEntity.ok(new MessageResponse("Token válido."));
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse(
                    "Token inválido ou expirado. Por favor, solicite uma nova redefinição de senha."));
        }
    }

    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        boolean success = passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        
        if (success) {
            return ResponseEntity.ok(new MessageResponse("Senha redefinida com sucesso. Você já pode fazer login."));
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse(
                    "Não foi possível redefinir sua senha. O token pode ser inválido ou expirado."));
        }
    }
}
