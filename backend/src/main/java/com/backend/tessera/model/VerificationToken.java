package com.backend.tessera.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "verification_tokens")
@Data
@NoArgsConstructor
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    private TokenType tokenType;

    private LocalDateTime expiryDate;

    private boolean used;

    // Enumeração para tipo de token
    public enum TokenType {
        PASSWORD_RESET,
        EMAIL_VERIFICATION
    }

    // Método para verificar se o token expirou
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    // Método de fábrica para criar um token de redefinição de senha
    public static VerificationToken createPasswordResetToken(User user, int expirationInMs) {
        VerificationToken token = new VerificationToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setTokenType(TokenType.PASSWORD_RESET);
        token.setExpiryDate(LocalDateTime.now().plusNanos(expirationInMs * 1000000L));
        token.setUsed(false);
        return token;
    }

    // Método de fábrica para criar um token de verificação de email
    public static VerificationToken createEmailVerificationToken(User user, int expirationInMs) {
        VerificationToken token = new VerificationToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setTokenType(TokenType.EMAIL_VERIFICATION);
        token.setExpiryDate(LocalDateTime.now().plusNanos(expirationInMs * 1000000L));
        token.setUsed(false);
        return token;
    }
}