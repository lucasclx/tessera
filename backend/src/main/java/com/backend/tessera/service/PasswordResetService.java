package com.backend.tessera.service;

import com.backend.tessera.model.User;
import com.backend.tessera.model.VerificationToken;
import com.backend.tessera.repository.UserRepository;
import com.backend.tessera.repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.MessagingException;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PasswordResetService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.token.password.reset.duration}")
    private int passwordResetTokenDuration;

    @Transactional
    public void requestPasswordReset(String email) throws MessagingException {
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            // Invalidar qualquer token anterior
            Optional<VerificationToken> existingToken = tokenRepository.findByUserIdAndTokenType(
                    user.getId(), VerificationToken.TokenType.PASSWORD_RESET);
            existingToken.ifPresent(token -> {
                token.setUsed(true);
                tokenRepository.save(token);
            });
            
            // Criar novo token
            VerificationToken token = VerificationToken.createPasswordResetToken(user, passwordResetTokenDuration);
            tokenRepository.save(token);
            
            // Enviar email
            emailService.sendPasswordResetEmail(user.getEmail(), token.getToken());
        }
        // Não informamos ao cliente se o email existe ou não por questões de segurança
    }

    @Transactional
    public boolean validatePasswordResetToken(String token) {
        Optional<VerificationToken> tokenOpt = tokenRepository.findByToken(token);
        
        if (tokenOpt.isEmpty()) {
            return false;
        }
        
        VerificationToken resetToken = tokenOpt.get();
        
        if (resetToken.isExpired() || resetToken.isUsed() || 
                resetToken.getTokenType() != VerificationToken.TokenType.PASSWORD_RESET) {
            return false;
        }
        
        return true;
    }

    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        Optional<VerificationToken> tokenOpt = tokenRepository.findByToken(token);
        
        if (tokenOpt.isEmpty()) {
            return false;
        }
        
        VerificationToken resetToken = tokenOpt.get();
        
        if (resetToken.isExpired() || resetToken.isUsed() || 
                resetToken.getTokenType() != VerificationToken.TokenType.PASSWORD_RESET) {
            return false;
        }
        
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        
        resetToken.setUsed(true);
        
        userRepository.save(user);
        tokenRepository.save(resetToken);
        
        return true;
    }
}
