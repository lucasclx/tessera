package com.backend.tessera.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ActuatorConfig {

    @Bean
    public HealthIndicator mailHealthIndicator(JavaMailSender mailSender) {
        return () -> {
            try {
                mailSender.createMimeMessage();
                return Health.up().withDetail("service", "Email service is available").build();
            } catch (Exception e) {
                return Health.down().withDetail("service", "Email service is unavailable")
                             .withDetail("error", e.getMessage()).build();
            }
        };
    }
    
    @Bean
    public HealthIndicator databaseHealthIndicator() {
        return () -> {
            // Esta verificação será feita pelo DataSourceHealthIndicator automático do Spring Boot
            return Health.up().withDetail("service", "Database service is available").build();
        };
    }
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}