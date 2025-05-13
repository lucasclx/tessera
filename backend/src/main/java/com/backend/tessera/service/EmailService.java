package com.backend.tessera.service;

import com.backend.tessera.config.LoggerConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class EmailService {
    private static final Logger logger = LoggerConfig.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.client.base-url}")
    private String clientBaseUrl;
    
    // Lista para armazenar emails não enviados para retry
    private final List<PendingEmail> pendingEmails = new CopyOnWriteArrayList<>();
    
    // Classe para armazenar emails pendentes
    @Data
    @AllArgsConstructor
    private static class PendingEmail {
        private String to;
        private String subject;
        private String htmlContent;
        private int retries;
        private LocalDateTime lastAttempt;
    }
    
    /**
     * Agenda o processamento de emails pendentes a cada 15 minutos
     */
    @Scheduled(fixedDelay = 900000) // 15 minutos
    public void processEmailQueue() {
        logger.info("Processando fila de emails pendentes. Tamanho da fila: {}", pendingEmails.size());
        
        if (pendingEmails.isEmpty()) {
            return;
        }
        
        Iterator<PendingEmail> iterator = pendingEmails.iterator();
        while (iterator.hasNext()) {
            PendingEmail email = iterator.next();
            
            // Esperar pelo menos 5 minutos entre tentativas
            if (email.getLastAttempt().plusMinutes(5).isAfter(LocalDateTime.now())) {
                continue;
            }
            
            // Tentar enviar o email
            boolean sent = doSendHtmlEmail(email.getTo(), email.getSubject(), email.getHtmlContent());
            
            if (sent) {
                iterator.remove();
                logger.info("Email pendente enviado com sucesso para: {}", email.getTo());
            } else {
                email.setRetries(email.getRetries() + 1);
                email.setLastAttempt(LocalDateTime.now());
                
                // Desistir após 5 tentativas
                if (email.getRetries() >= 5) {
                    logger.error("Falha permanente ao enviar email para: {} após {} tentativas", 
                                email.getTo(), email.getRetries());
                    iterator.remove();
                }
            }
        }
    }

    /**
     * Tenta enviar um email assincronamente, com retentativas em caso de falha
     */
    @Async
    public void sendEmailAsync(String to, String subject, String htmlContent) {
        logger.debug("Enviando email assíncrono para: {}", to);
        boolean sent = doSendHtmlEmail(to, subject, htmlContent);
        
        if (!sent) {
            // Adicionar à fila para tentar novamente mais tarde
            pendingEmails.add(new PendingEmail(to, subject, htmlContent, 1, LocalDateTime.now()));
            logger.warn("Email para {} adicionado à fila de retentativas", to);
        }
    }
    
    /**
     * Tenta enviar o email imediatamente, sem retentativas
     */
    private boolean doSendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            logger.info("Email enviado com sucesso para: {}", to);
            return true;
        } catch (Exception e) {
            logger.error("Falha ao enviar email para: {}: {}", to, e.getMessage());
            return false;
        }
    }

    /**
     * Envia um email de redefinição de senha
     */
    public void sendPasswordResetEmail(String to, String token) {
        try {
            String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
            String resetUrl = clientBaseUrl + "/auth/reset-password?token=" + encodedToken;
            
            String subject = "Redefinição de Senha - Sistema Acadêmico";
            String htmlContent = "<div style='font-family: Arial, sans-serif; color: #333;'>"
                    + "<h2 style='color: #1976d2;'>Recuperação de Senha</h2>"
                    + "<p>Olá,</p>"
                    + "<p>Recebemos uma solicitação para redefinir sua senha. Se você não solicitou esta redefinição, por favor ignore este email.</p>"
                    + "<p>Para redefinir sua senha, clique no botão abaixo:</p>"
                    + "<p style='text-align: center;'>"
                    + "<a href='" + resetUrl + "' style='display: inline-block; background-color: #1976d2; color: white; padding: 10px 20px; "
                    + "text-decoration: none; border-radius: 5px; font-weight: bold;'>Redefinir Senha</a>"
                    + "</p>"
                    + "<p>Ou copie e cole o seguinte link no seu navegador:</p>"
                    + "<p><a href='" + resetUrl + "'>" + resetUrl + "</a></p>"
                    + "<p>Este link expirará em 1 hora.</p>"
                    + "<p>Atenciosamente,<br>Equipe do Sistema Acadêmico</p>"
                    + "</div>";
            
            sendEmailAsync(to, subject, htmlContent);
        } catch (Exception e) {
            logger.error("Erro ao preparar email de redefinição de senha: {}", e.getMessage());
        }
    }

    /**
     * Envia um email de verificação
     */
    public void sendEmailVerification(String to, String token) {
        try {
            String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
            String verificationUrl = clientBaseUrl + "/auth/verify-email?token=" + encodedToken;
            
            String subject = "Verificação de Email - Sistema Acadêmico";
            String htmlContent = "<div style='font-family: Arial, sans-serif; color: #333;'>"
                    + "<h2 style='color: #1976d2;'>Verificação de Email</h2>"
                    + "<p>Olá,</p>"
                    + "<p>Obrigado por se cadastrar no Sistema Acadêmico. Para completar seu cadastro, precisamos verificar seu email.</p>"
                    + "<p>Por favor, clique no botão abaixo para verificar seu email:</p>"
                    + "<p style='text-align: center;'>"
                    + "<a href='" + verificationUrl + "' style='display: inline-block; background-color: #4caf50; color: white; padding: 10px 20px; "
                    + "text-decoration: none; border-radius: 5px; font-weight: bold;'>Verificar Email</a>"
                    + "</p>"
                    + "<p>Ou copie e cole o seguinte link no seu navegador:</p>"
                    + "<p><a href='" + verificationUrl + "'>" + verificationUrl + "</a></p>"
                    + "<p>Este link expirará em 24 horas.</p>"
                    + "<p>Atenciosamente,<br>Equipe do Sistema Acadêmico</p>"
                    + "</div>";
            
            sendEmailAsync(to, subject, htmlContent);
        } catch (Exception e) {
            logger.error("Erro ao preparar email de verificação: {}", e.getMessage());
        }
    }
}