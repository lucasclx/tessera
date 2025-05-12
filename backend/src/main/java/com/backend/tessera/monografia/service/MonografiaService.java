package com.backend.tessera.monografia.service;

import com.backend.tessera.monografia.entity.Monografia;
import com.backend.tessera.monografia.repository.MonografiaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MonografiaService {
    @Autowired
    private MonografiaRepository monografiaRepository;

    public List<Monografia> buscarMonografiasParaUsuarioAutenticado(Authentication auth) {
        // Implementação
        return monografiaRepository.findAll();
    }

    public Monografia findByIdAndUser(Long id, Authentication auth) {
        // Implementação
        return monografiaRepository.findById(id).orElse(null);
    }

    public Monografia criarMonografia(Monografia monografia, Authentication auth) {
        // Implementação
        return monografiaRepository.save(monografia);
    }
}