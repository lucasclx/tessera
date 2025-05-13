package com.backend.tessera.service;

import com.backend.tessera.config.LoggerConfig;
import com.backend.tessera.model.User;
import com.backend.tessera.model.VerificationToken;
import com.backend.tessera.repository.UserRepository;
import com.backend.tessera.repository.VerificationTokenRepository;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PasswordResetService {
    private static final Logger logger = LoggerConfig.getLogger(PasswordResetService.class);

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

    /**
     * Solicita a redefinição de senha para um email
     * @return true se o email foi enviado com sucesso, false caso contrário
     */
    @Transactional
    public boolean requestPasswordReset(String email) {
        logger.debug("Solicitação de redefinição de senha para email: {}", email);
        
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            logger.debug("Usuário encontrado: {}", user.getUsername());
            
            // Invalidar qualquer token anterior
            Optional<VerificationToken> existingToken = tokenRepository.findByUserIdAndTokenType(
                    user.getId(), VerificationToken.TokenType.PASSWORD_RESET);
            
            existingToken.ifPresent(token -> {
                token.setUsed(true);
                tokenRepository.save(token);
                logger.debug("Token anterior invalidado para usuário: {}", user.getUsername());
            });
            
            // Criar novo token
            VerificationToken token = VerificationToken.createPasswordResetToken(user, passwordResetTokenDuration);
            tokenRepository.save(token);
            logger.debug("Novo token de redefinição criado: {}", token.getToken().substring(0, 8) + "...");
            
            // Enviar email
            boolean emailSent = emailService.sendPasswordResetEmail(user.getEmail(), token.getToken());
            
            if (emailSent) {
                logger.info("Email de redefinição enviado com sucesso para: {} <{}>", user.getUsername(), user.getEmail());
            } else {
                logger.error("Falha ao enviar email de redefinição para: {} <{}>", user.getUsername(), user.getEmail());
            }
            
            return emailSent;
        } else {
            logger.debug("Email não encontrado no sistema: {}", email);
            // Não informamos ao cliente por segurança, simulamos sucesso
            return true;
        }
    }

    /**
     * Valida um token de redefinição de senha
     */
    @Transactional(readOnly = true)
    public boolean validatePasswordResetToken(String token) {
        logger.debug("Validando token de redefinição: {}...", token.substring(0, 8));
        
        Optional<VerificationToken> tokenOpt = tokenRepository.findByToken(token);
        
        if (tokenOpt.isEmpty()) {
            logger.warn("Token de redefinição não encontrado: {}...", token.substring(0, 8));
            return false;
        }
        
        VerificationToken resetToken = tokenOpt.get();
        
        if (resetToken.isExpired()) {
            logger.warn("Token de redefinição expirado: {}...", token.substring(0, 8));
            return false;
        }
        
        if (resetToken.isUsed()) {
            logger.warn("Token de redefinição já utilizado: {}...", token.substring(0, 8));
            return false;
        }
        
        if (resetToken.getTokenType() != VerificationToken.TokenType.PASSWORD_RESET) {
            logger.warn("Tipo de token inválido: {}", resetToken.getTokenType());
            return false;
        }
        
        logger.debug("Token de redefinição válido: {}...", token.substring(0, 8));
        return true;
    }

    /**
     * Redefine a senha de um usuário usando um token
     */
    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        logger.debug("Tentativa de redefinição de senha com token: {}...", token.substring(0, 8));
        
        Optional<VerificationToken> tokenOpt = tokenRepository.findByToken(token);
        
        if (tokenOpt.isEmpty()) {
            logger.warn("Token de redefinição não encontrado: {}...", token.substring(0, 8));
            return false;
        }
        
        VerificationToken resetToken = tokenOpt.get();
        
        if (resetToken.isExpired()) {
            logger.warn("Token de redefinição expirado: {}...", token.substring(0, 8));
            return false;
        }
        
        if (resetToken.isUsed()) {
            logger.warn("Token de redefinição já utilizado: {}...", token.substring(0, 8));
            return false;
        }
        
        if (resetToken.getTokenType() != VerificationToken.TokenType.PASSWORD_RESET) {
            logger.warn("Tipo de token inválido: {}", resetToken.getTokenType());
            return false;
        }
        
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        
        resetToken.setUsed(true);
        
        userRepository.save(user);
        tokenRepository.save(resetToken);
        
        logger.info("Senha redefinida com sucesso para usuário: {}", user.getUsername());
        return true;
    }
}