package com.backend.tessera.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String action;
    
    @Column(nullable = false)
    private String performedBy;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(length = 1000)
    private String details;
    
    @Column
    private Long targetUserId;
    
    // Factory method para criar um log de aprovação
    public static AuditLog createApprovalLog(String adminUsername, Long userId, boolean approved) {
        AuditLog log = new AuditLog();
        log.setAction(approved ? "USER_APPROVED" : "USER_REJECTED");
        log.setPerformedBy(adminUsername);
        log.setTimestamp(LocalDateTime.now());
        log.setTargetUserId(userId);
        log.setDetails("Usuário " + (approved ? "aprovado" : "rejeitado") + " pelo administrador: " + adminUsername);
        return log;
    }
    
    // Factory method para criar um log de alteração de status
    public static AuditLog createStatusChangeLog(String adminUsername, Long userId, boolean enabled) {
        AuditLog log = new AuditLog();
        log.setAction(enabled ? "USER_ENABLED" : "USER_DISABLED");
        log.setPerformedBy(adminUsername);
        log.setTimestamp(LocalDateTime.now());
        log.setTargetUserId(userId);
        log.setDetails("Status de usuário alterado para " + (enabled ? "ativo" : "inativo") + " pelo administrador: " + adminUsername);
        return log;
    }
}