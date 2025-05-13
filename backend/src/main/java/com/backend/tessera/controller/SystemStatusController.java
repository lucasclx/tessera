package com.backend.tessera.controller;

import com.backend.tessera.config.LoggerConfig;
import com.backend.tessera.repository.UserRepository;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para verificar o status do sistema
 */
@RestController
@RequestMapping("/api/system")
public class SystemStatusController {
    private static final Logger logger = LoggerConfig.getLogger(SystemStatusController.class);
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Retorna o status atual dos componentes do sistema
     */
    @GetMapping("/status")
    public ResponseEntity<?> getSystemStatus() {
        logger.info("Verificando status do sistema");
        Map<String, Object> status = new HashMap<>();
        
        // Verificar banco de dados
        try {
            long userCount = userRepository.count();
            status.put("database", Map.of(
                "status", "OK",
                "userCount", userCount
            ));
            logger.debug("Status do banco de dados: OK, {} usuários encontrados", userCount);
        } catch (Exception e) {
            logger.error("Erro ao verificar banco de dados: {}", e.getMessage());
            status.put("database", Map.of(
                "status", "ERROR",
                "message", "Não foi possível conectar ao banco de dados: " + e.getMessage()
            ));
        }
        
        // Verificar serviço de email
        try {
            mailSender.createMimeMessage();
            status.put("emailService", Map.of(
                "status", "OK"
            ));
            logger.debug("Status do serviço de email: OK");
        } catch (Exception e) {
            logger.error("Erro ao verificar serviço de email: {}", e.getMessage());
            status.put("emailService", Map.of(
                "status", "ERROR",
                "message", "Serviço de email não disponível: " + e.getMessage()
            ));
        }
        
        // Adicionar versão do sistema
        status.put("version", "1.0.0");
        status.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(status);
    }
    
    /**
     * Endpoint simples para verificação básica de saúde do sistema
     */
    @GetMapping("/health")
    public ResponseEntity<?> getHealth() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }
}