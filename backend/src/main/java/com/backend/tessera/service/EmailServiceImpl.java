// Arquivo: backend/src/main/java/com/backend/tessera/service/EmailServiceImpl.java
package com.backend.tessera.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired
    private JavaMailSender emailSender;

    @Value("${mail.from.address}")
    private String fromAddress;

    @Async
    @Override
    public void sendSimpleMessage(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            emailSender.send(message);
            logger.info("E-mail simples enviado para: {}", to);
        } catch (MailException e) {
            logger.error("Erro ao enviar e-mail simples para {}: {}", to, e.getMessage());
            // Considerar lógica de retentativa ou notificação de falha mais robusta
        }
    }

    @Async
    @Override
    public void sendHtmlMessage(String to, String subject, String htmlBody) throws MessagingException {
        try {
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8"); // true indica multipart
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true indica que o texto é HTML
            emailSender.send(mimeMessage);
            logger.info("E-mail HTML enviado para: {}", to);
        } catch (MailException | MessagingException e) { // Capturar ambas as exceções
            logger.error("Erro ao enviar e-mail HTML para {}: {}", to, e.getMessage());
            // Re-lançar MessagingException se for necessário para o chamador tratar
            if (e instanceof MessagingException) {
                throw (MessagingException) e;
            }
            // Para MailException, pode-se encapsular em uma RuntimeException ou tratar de outra forma
        }
    }
}