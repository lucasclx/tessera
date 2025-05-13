package com.backend.tessera.service;

import com.backend.tessera.config.LoggerConfig;
import com.backend.tessera.model.User;
import com.backend.tessera.model.VerificationToken;
import com.backend.tessera.repository.UserRepository;
import com.backend.tessera.repository.VerificationTokenRepository;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class EmailVerificationService {
    private static final Logger logger = LoggerConfig.getLogger(EmailVerificationService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    @Value("${app.token.email.verification.duration}")
    private int emailVerificationTokenDuration;

    /**
     * Cria e envia um token de verificação de email para o usuário
     * @return true se o email foi enviado com sucesso, false caso contrário
     */
    @Transactional
    public boolean sendVerificationEmail(User user) {
        logger.debug("Gerando token de verificação de email para usuário: {}", user.getUsername());
        
        // Invalidar qualquer token anterior
        Optional<VerificationToken> existingToken = tokenRepository.findByUserIdAndTokenType(
                user.getId(), VerificationToken.TokenType.EMAIL_VERIFICATION);
        
        existingToken.ifPresent(token -> {
            token.setUsed(true);
            tokenRepository.save(token);
            logger.debug("Token anterior invalidado para usuário: {}", user.getUsername());
        });
        
        // Criar novo token
        VerificationToken token = VerificationToken.createEmailVerificationToken(user, emailVerificationTokenDuration);
        tokenRepository.save(token);
        logger.debug("Novo token de verificação criado: {}", token.getToken().substring(0, 8) + "...");
        
        // Enviar email
        boolean emailSent = emailService.sendEmailVerification(user.getEmail(), token.getToken());
        
        if (emailSent) {
            logger.info("Email de verificação enviado com sucesso para: {} <{}>", user.getUsername(), user.getEmail());
        } else {
            logger.error("Falha ao enviar email de verificação para: {} <{}>", user.getUsername(), user.getEmail());
        }
        
        return emailSent;
    }

    /**
     * Verifica um token de verificação de email
     * @return true se o token foi validado com sucesso, false caso contrário
     */
    @Transactional
    public boolean verifyEmail(String token) {
        logger.debug("Validando token de verificação de email: {}...", token.substring(0, 8));
        
        Optional<VerificationToken> tokenOpt = tokenRepository.findByToken(token);
        
        if (tokenOpt.isEmpty()) {
            logger.warn("Token de verificação não encontrado: {}...", token.substring(0, 8));
            return false;
        }
        
        VerificationToken verificationToken = tokenOpt.get();
        
        if (verificationToken.isExpired()) {
            logger.warn("Token de verificação expirado: {}...", token.substring(0, 8));
            return false;
        }
        
        if (verificationToken.isUsed()) {
            logger.warn("Token de verificação já utilizado: {}...", token.substring(0, 8));
            return false;
        }
        
        if (verificationToken.getTokenType() != VerificationToken.TokenType.EMAIL_VERIFICATION) {
            logger.warn("Tipo de token inválido: {}", verificationToken.getTokenType());
            return false;
        }
        
        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        user.setEmailVerifiedAt(LocalDateTime.now());
        
        verificationToken.setUsed(true);
        
        userRepository.save(user);
        tokenRepository.save(verificationToken);
        
        logger.info("Email verificado com sucesso para usuário: {}", user.getUsername());
        return true;
    }

    /**
     * Reenviar email de verificação para um email específico
     * Não informa ao cliente se o email existe ou não por questões de segurança
     */
    @Transactional
    public void resendVerificationEmail(String email) {
        logger.debug("Solicitação para reenviar email de verificação para: {}", email);
        
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            if (!user.isEmailVerified()) {
                logger.debug("Reenviando email de verificação para usuário: {}", user.getUsername());
                sendVerificationEmail(user);
            } else {
                logger.debug("Email já verificado para usuário: {}", user.getUsername());
            }
        } else {
            logger.debug("Email não encontrado no sistema: {}", email);
            // Não informamos ao cliente por segurança
        }
    }
}