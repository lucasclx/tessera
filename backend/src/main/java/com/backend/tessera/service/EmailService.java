package com.backend.tessera.service;

import com.backend.tessera.config.LoggerConfig;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class EmailService {
    private static final Logger logger = LoggerConfig.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.client.base-url}")
    private String clientBaseUrl;

    /**
     * Tenta enviar um email com retentativas em caso de falha
     * @return true se o email foi enviado com sucesso, false caso contrário
     */
    public boolean trySendHtmlEmail(String to, String subject, String htmlContent) {
        int maxRetries = 3;
        int retryCount = 0;
        
        while (retryCount < maxRetries) {
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
                retryCount++;
                logger.warn("Falha ao enviar email (tentativa {}/{}): {}", retryCount, maxRetries, e.getMessage());
                
                if (retryCount >= maxRetries) {
                    logger.error("Falha ao enviar email após {} tentativas: {}", maxRetries, e.getMessage());
                    return false;
                }
                
                // Espera antes da próxima tentativa (com backoff exponencial)
                try {
                    Thread.sleep(1000 * retryCount);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    logger.error("Thread interrompida durante espera para retry: {}", ie.getMessage());
                    return false;
                }
            }
        }
        
        return false;
    }

    /**
     * Envia um email de redefinição de senha
     * @return true se o email foi enviado com sucesso, false caso contrário
     */
    public boolean sendPasswordResetEmail(String to, String token) {
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
            
            return trySendHtmlEmail(to, subject, htmlContent);
        } catch (Exception e) {
            logger.error("Erro ao preparar email de redefinição de senha: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Envia um email de verificação
     * @return true se o email foi enviado com sucesso, false caso contrário
     */
    public boolean sendEmailVerification(String to, String token) {
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
            
            return trySendHtmlEmail(to, subject, htmlContent);
        } catch (Exception e) {
            logger.error("Erro ao preparar email de verificação: {}", e.getMessage());
            return false;
        }
    }
}