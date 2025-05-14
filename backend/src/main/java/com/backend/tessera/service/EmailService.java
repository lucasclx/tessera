// Arquivo: backend/src/main/java/com/backend/tessera/service/EmailService.java
package com.backend.tessera.service;

import jakarta.mail.MessagingException;

public interface EmailService {
    void sendSimpleMessage(String to, String subject, String text);
    void sendHtmlMessage(String to, String subject, String htmlBody) throws MessagingException;
}