package com.backend.tessera.monografia.controller;

import com.backend.tessera.monografia.entity.Monografia;
import com.backend.tessera.monografia.service.MonografiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/monografias")
public class MonografiaController {
    
    @Autowired
    private MonografiaService monografiaService;
    
    // Implementação dos endpoints...
}