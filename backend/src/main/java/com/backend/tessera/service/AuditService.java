package com.backend.tessera.service;

import com.backend.tessera.config.LoggerConfig;
import com.backend.tessera.model.AuditLog;
import com.backend.tessera.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {
    private static final Logger logger = LoggerConfig.getLogger(AuditService.class);
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    @Transactional
    public void logUserApproval(String adminUsername, Long userId, boolean approved) {
        AuditLog log = AuditLog.createApprovalLog(adminUsername, userId, approved);
        auditLogRepository.save(log);
        logger.info("Audit: Usuário {} {} pelo administrador {}", 
                   userId, (approved ? "aprovado" : "rejeitado"), adminUsername);
    }
    
    @Transactional
    public void logUserStatusChange(String adminUsername, Long userId, boolean enabled) {
        AuditLog log = AuditLog.createStatusChangeLog(adminUsername, userId, enabled);
        auditLogRepository.save(log);
        logger.info("Audit: Status do usuário {} alterado para {} pelo administrador {}", 
                   userId, (enabled ? "ativo" : "inativo"), adminUsername);
    }
}