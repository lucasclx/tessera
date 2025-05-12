package com.backend.tessera.comentario.repository;

import com.backend.tessera.comentario.entity.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, Long> {
    List<Comentario> findByVersaoIdOrderByDataCriacaoDesc(Long versaoId);
}