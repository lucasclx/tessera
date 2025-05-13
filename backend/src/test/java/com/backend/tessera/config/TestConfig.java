package com.backend.tessera.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * Configuração específica para testes
 */
@TestConfiguration
@Profile("test")
public class TestConfig {
    
    /**
     * Cria um JavaMailSender que não envia emails de verdade durante os testes
     */
    @Bean
    @Primary
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        // Configuração que não faz nada na prática
        mailSender.setHost("localhost");
        mailSender.setPort(3025);
        return mailSender;
    }
}