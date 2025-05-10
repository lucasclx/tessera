package com.backend.tessera.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/dashboard") // Mantendo o path original por enquanto
public class DashboardController {

    // Proteção de rota está em SecurityConfig
    @GetMapping("/professor/data")
    public ResponseEntity<String> getProfessorData(Principal principal) {
        return ResponseEntity.ok("Dados confidenciais do Dashboard do Professor para: " + principal.getName());
    }

    @GetMapping("/aluno/data")
    public ResponseEntity<String> getAlunoData(Principal principal) {
        return ResponseEntity.ok("Dados específicos do Dashboard do Aluno para: " + principal.getName());
    }
}