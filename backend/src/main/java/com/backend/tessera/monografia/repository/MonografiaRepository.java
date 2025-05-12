package com.backend.tessera.monografia.repository;

import com.backend.tessera.monografia.entity.Monografia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MonografiaRepository extends JpaRepository<Monografia, Long> {
    // Métodos de consulta personalizados, se necessário
}