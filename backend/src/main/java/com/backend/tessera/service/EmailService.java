package com.backend.tessera.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.client.base-url}")
    private String clientBaseUrl;

    public void sendPasswordResetEmail(String to, String token) throws MessagingException {
        String resetUrl = clientBaseUrl + "/auth/reset-password?token=" + token;
        
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
        
        sendHtmlEmail(to, subject, htmlContent);
    }

    public void sendEmailVerification(String to, String token) throws MessagingException {
        String verificationUrl = clientBaseUrl + "/auth/verify-email?token=" + token;
        
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
        
        sendHtmlEmail(to, subject, htmlContent);
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        
        mailSender.send(message);
    }
}