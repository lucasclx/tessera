package com.backend.tessera.repository;

import com.backend.tessera.model.AccountStatus;
import com.backend.tessera.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    
    // Novos métodos para busca por status
    List<User> findByStatus(AccountStatus status);
    Page<User> findByStatus(AccountStatus status, Pageable pageable);
}