// Arquivo: backend/src/main/java/com/backend/tessera/repository/UserRepository.java
package com.backend.tessera.repository;

import com.backend.tessera.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email); // Confirmar que este método existe
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);

    // Métodos para os novos tokens
    Optional<User> findByEmailVerificationToken(String token);
    Optional<User> findByPasswordResetToken(String token);
}