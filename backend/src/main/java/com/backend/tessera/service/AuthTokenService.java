// Arquivo: backend/src/main/java/com/backend/tessera/service/AuthTokenService.java
package com.backend.tessera.service;

import com.backend.tessera.model.User;
import com.backend.tessera.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthTokenService {

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final int TOKEN_EXPIRATION_HOURS_EMAIL = 24;
    private static final int TOKEN_EXPIRATION_MINUTES_PASSWORD = 60; // 1 hora

    // --- Lógica de Confirmação de E-mail ---

    @Transactional
    public String generateAndSendEmailVerificationToken(User user, String backendBaseUrl) {
        String token = UUID.randomUUID().toString();
        user.setEmailVerificationToken(token);
        user.setEmailVerificationTokenExpiryDate(LocalDateTime.now().plusHours(TOKEN_EXPIRATION_HOURS_EMAIL));
        userRepository.save(user);

        // O link de confirmação aponta para o endpoint do backend
        String confirmationUrl = backendBaseUrl + "/api/auth/confirm-email?token=" + token;
        String subject = "Confirmação de E-mail - Tessera";
        String text = "Olá " + user.getNome() + ",\n\n"
                    + "Obrigado por se registrar no Tessera. Por favor, confirme seu endereço de e-mail clicando no link abaixo:\n"
                    + confirmationUrl + "\n\n"
                    + "Este link é válido por " + TOKEN_EXPIRATION_HOURS_EMAIL + " horas.\n"
                    + "Se você não se registrou, por favor ignore este e-mail.\n\n"
                    + "Atenciosamente,\nEquipe Tessera";
        
        logger.info("Enviando e-mail de confirmação para {} com URL: {}", user.getEmail(), confirmationUrl);
        emailService.sendSimpleMessage(user.getEmail(), subject, text);
        return token;
    }

    @Transactional
    public boolean verifyEmailToken(String token) {
        Optional<User> userOpt = userRepository.findByEmailVerificationToken(token);
        if (userOpt.isEmpty()) {
            logger.warn("Token de verificação de e-mail não encontrado: {}", token);
            return false; 
        }
        User user = userOpt.get();
        if (user.getEmailVerificationTokenExpiryDate().isBefore(LocalDateTime.now())) {
            logger.warn("Token de verificação de e-mail expirado para o usuário {}: {}", user.getUsername(), token);
            user.setEmailVerificationToken(null);
            user.setEmailVerificationTokenExpiryDate(null);
            userRepository.save(user);
            return false;
        }

        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationTokenExpiryDate(null);
        userRepository.save(user);
        logger.info("E-mail verificado com sucesso para o usuário {}", user.getUsername());
        return true;
    }

    @Transactional
    public boolean resendVerificationEmail(String email, String backendBaseUrl) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (!user.isEmailVerified()) {
                logger.info("Reenviando e-mail de verificação para o usuário {}", user.getUsername());
                generateAndSendEmailVerificationToken(user, backendBaseUrl);
                return true;
            } else {
                logger.info("Tentativa de reenviar e-mail de verificação para {}, mas o e-mail já está verificado.", email);
                return false; // E-mail já verificado
            }
        }
        logger.warn("Tentativa de reenviar e-mail de verificação para {}, mas o usuário não foi encontrado.", email);
        return false; // Usuário não encontrado
    }


    // --- Lógica de Recuperação de Senha ---

    @Transactional
    public boolean generateAndSendPasswordResetToken(String email, String frontendResetUrlBase) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            logger.warn("Solicitação de redefinição de senha para e-mail não registrado: {}", email);
            // Não revele se o e-mail existe ou não
            return true; 
        }
        User user = userOpt.get();

        // Opcional: Considerar se a conta precisa estar ATIVA ou e-mail verificado para permitir reset
        // if (!user.isEmailVerified()) {
        //     logger.warn("Tentativa de reset de senha para e-mail não verificado: {}", email);
        //     return true; // Ou enviar e-mail instruindo a verificar primeiro
        // }
        // if (user.getStatus() != AccountStatus.ATIVO) {
        //     logger.warn("Tentativa de reset de senha para conta não ativa: {}", email);
        //     return true;
        // }

        String token = UUID.randomUUID().toString();
        user.setPasswordResetToken(token);
        user.setPasswordResetTokenExpiryDate(LocalDateTime.now().plusMinutes(TOKEN_EXPIRATION_MINUTES_PASSWORD));
        userRepository.save(user);

        // O link de redefinição de senha aponta para uma página no frontend
        String resetUrl = frontendResetUrlBase + "/resetar-senha?token=" + token; 
        String subject = "Redefinição de Senha - Tessera";
        String text = "Olá " + user.getNome() + ",\n\n"
                    + "Recebemos uma solicitação para redefinir sua senha. Se foi você, clique no link abaixo para criar uma nova senha:\n"
                    + resetUrl + "\n\n"
                    + "Este link expirará em " + TOKEN_EXPIRATION_MINUTES_PASSWORD + " minutos.\n\n"
                    + "Se você não solicitou uma redefinição de senha, por favor ignore este e-mail.\n\n"
                    + "Atenciosamente,\nEquipe Tessera";
        
        logger.info("Enviando e-mail de redefinição de senha para {} com URL de frontend: {}", user.getEmail(), resetUrl);
        emailService.sendSimpleMessage(user.getEmail(), subject, text);
        return true;
    }

    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        Optional<User> userOpt = userRepository.findByPasswordResetToken(token);
        if (userOpt.isEmpty()) {
            logger.warn("Token de redefinição de senha inválido: {}", token);
            return false; 
        }
        User user = userOpt.get();
        if (user.getPasswordResetTokenExpiryDate().isBefore(LocalDateTime.now())) {
            logger.warn("Token de redefinição de senha expirado para o usuário {}: {}", user.getUsername(), token);
            user.setPasswordResetToken(null);
            user.setPasswordResetTokenExpiryDate(null);
            userRepository.save(user);
            return false;
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiryDate(null);
        // Opcional: Invalidar sessões JWT existentes (exigiria armazenamento de tokens ativos ou mecanismo similar)
        userRepository.save(user);
        logger.info("Senha redefinida com sucesso para o usuário {}", user.getUsername());
        return true;
    }
}