package com.backend.tessera.versao.repository;

import com.backend.tessera.monografia.entity.Monografia;
import com.backend.tessera.versao.entity.Versao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VersaoRepository extends JpaRepository<Versao, Long> {
    List<Versao> findByMonografiaOrderByDataCriacaoDesc(Monografia monografia);
    long countByMonografia(Monografia monografia);
}