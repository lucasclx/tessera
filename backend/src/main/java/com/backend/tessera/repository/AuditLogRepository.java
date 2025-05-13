package com.backend.tessera.repository;

import com.backend.tessera.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByTargetUserId(Long userId);
    List<AuditLog> findByPerformedBy(String adminUsername);
}