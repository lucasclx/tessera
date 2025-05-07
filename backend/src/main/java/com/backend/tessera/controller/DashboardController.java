package com.backend.tessera.controller;

import org.springframework.http.ResponseEntity;
// import org.springframework.security.access.prepost.PreAuthorize; // Alternativa para controle de acesso
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal; // Para obter o usuário logado

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    // A proteção de rota já está em SecurityConfig via .hasRole()
    // Alternativamente, poderia usar @PreAuthorize("hasRole('PROFESSOR')") aqui
    @GetMapping("/professor/data")
    public ResponseEntity<String> getProfessorData(Principal principal) {
        // Lógica para buscar dados do professor
        // principal.getName() retorna o username do usuário autenticado
        return ResponseEntity.ok("Dados confidenciais do Dashboard do Professor para: " + principal.getName());
    }

    @GetMapping("/aluno/data")
    public ResponseEntity<String> getAlunoData(Principal principal) {
        // Lógica para buscar dados do aluno
        return ResponseEntity.ok("Dados específicos do Dashboard do Aluno para: " + principal.getName());
    }
}