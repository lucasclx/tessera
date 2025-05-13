package com.backend.tessera.repository;

import com.backend.tessera.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);
    Optional<VerificationToken> findByUserIdAndTokenType(Long userId, VerificationToken.TokenType tokenType);
    
    // Novos métodos para operações mais eficientes
    List<VerificationToken> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}