package com.backend.tessera.controller;

import com.backend.tessera.dto.MessageResponse;
import com.backend.tessera.dto.ResendVerificationRequest;
import com.backend.tessera.service.EmailVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth/email")
public class EmailVerificationController {

    @Autowired
    private EmailVerificationService emailVerificationService;

    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        boolean verified = emailVerificationService.verifyEmail(token);
        
        if (verified) {
            return ResponseEntity.ok(new MessageResponse("Email verificado com sucesso. Agora você pode fazer login."));
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse(
                    "Não foi possível verificar seu email. O token pode ser inválido ou expirado."));
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerificationEmail(@Valid @RequestBody ResendVerificationRequest request) {
        try {
            emailVerificationService.resendVerificationEmail(request.getEmail());
            return ResponseEntity.ok(new MessageResponse(
                    "Se seu email estiver cadastrado e não verificado, você receberá um novo link de verificação."));
        } catch (MessagingException e) {
            return ResponseEntity.internalServerError().body(new MessageResponse(
                    "Erro ao enviar email de verificação. Por favor, tente novamente mais tarde."));
        }
    }
}