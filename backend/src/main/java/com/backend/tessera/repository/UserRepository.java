package com.backend.tessera.repository;

import com.backend.tessera.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    // Removido: Boolean existsByCpf(String cpf);

    // Método adicionado:
    Optional<User> findByEmail(String email); // <-- Adicione esta linha
}