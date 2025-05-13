package com.backend.tessera.service;

import com.backend.tessera.model.User;
import com.backend.tessera.model.VerificationToken;
import com.backend.tessera.repository.UserRepository;
import com.backend.tessera.repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.MessagingException;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class EmailVerificationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    @Value("${app.token.email.verification.duration}")
    private int emailVerificationTokenDuration;

    @Transactional
    public void sendVerificationEmail(User user) throws MessagingException {
        // Invalidar qualquer token anterior
        Optional<VerificationToken> existingToken = tokenRepository.findByUserIdAndTokenType(
                user.getId(), VerificationToken.TokenType.EMAIL_VERIFICATION);
        existingToken.ifPresent(token -> {
            token.setUsed(true);
            tokenRepository.save(token);
        });
        
        // Criar novo token
        VerificationToken token = VerificationToken.createEmailVerificationToken(user, emailVerificationTokenDuration);
        tokenRepository.save(token);
        
        // Enviar email
        emailService.sendEmailVerification(user.getEmail(), token.getToken());
    }

    @Transactional
    public boolean verifyEmail(String token) {
        Optional<VerificationToken> tokenOpt = tokenRepository.findByToken(token);
        
        if (tokenOpt.isEmpty()) {
            return false;
        }
        
        VerificationToken verificationToken = tokenOpt.get();
        
        if (verificationToken.isExpired() || verificationToken.isUsed() || 
                verificationToken.getTokenType() != VerificationToken.TokenType.EMAIL_VERIFICATION) {
            return false;
        }
        
        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        user.setEmailVerifiedAt(LocalDateTime.now());
        
        verificationToken.setUsed(true);
        
        userRepository.save(user);
        tokenRepository.save(verificationToken);
        
        return true;
    }

    @Transactional
    public void resendVerificationEmail(String email) throws MessagingException {
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            if (!user.isEmailVerified()) {
                sendVerificationEmail(user);
            }
        }
        // Não informamos ao cliente se o email existe ou não por questões de segurança
    }
}