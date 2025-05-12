package com.backend.tessera.monografia.controller; // Pacote do controller de monografia

// Ajuste estes imports para os pacotes corretos do seu projeto:
import com.backend.tessera.monografia.entity.Monografia; // Ex: com.backend.tessera.monografia.entity.Monografia
import com.backend.tessera.monografia.service.MonografiaService; // Ex: com.backend.tessera.monografia.service.MonografiaService

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*; // Adicionado para @PathVariable, @RequestBody etc.

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/monografias")
public class MonografiaController {

    private static final Logger logger = LoggerFactory.getLogger(MonografiaController.class);

    @Autowired
    private MonografiaService monografiaService;

    @GetMapping
    // A autorização principal já deve estar no SecurityConfig.
    // @PreAuthorize pode ser usado para lógicas mais complexas se necessário.
    // Ex: @PreAuthorize("hasAnyRole('ALUNO', 'PROFESSOR', 'ADMIN')")
    public ResponseEntity<?> getMonografias() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = "ANONYMOUS";
        List<String> authorities = Collections.emptyList();

        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            currentPrincipalName = authentication.getName();
            authorities = authentication.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority)
                                .collect(Collectors.toList());
        }
        
        logger.info("MONOGRAFIA_CONTROLLER: Requisição GET /api/monografias recebida. Usuário: {}, Autoridades: {}", currentPrincipalName, authorities);

        try {
            List<Monografia> monografias;
            // A lógica de qual monografia buscar deve estar no serviço,
            // que pode usar o 'authentication' para determinar o usuário e suas roles.
            monografias = monografiaService.buscarMonografiasParaUsuarioAutenticado(authentication); // Método de exemplo
            
            if (monografias == null) {
                 logger.warn("MONOGRAFIA_CONTROLLER: O serviço retornou uma lista nula de monografias para o usuário: {}", currentPrincipalName);
                 monografias = Collections.emptyList();
            }

            if (monografias.isEmpty()) {
                logger.info("MONOGRAFIA_CONTROLLER: Nenhuma monografia encontrada para o usuário: {}", currentPrincipalName);
                return ResponseEntity.ok(Collections.emptyList()); // Retorna 200 OK com lista vazia
            }
            
            logger.info("MONOGRAFIA_CONTROLLER: Retornando {} monografia(s) para o usuário: {}", monografias.size(), currentPrincipalName);
            return ResponseEntity.ok(monografias);

        } catch (Exception e) {
            logger.error("MONOGRAFIA_CONTROLLER: Erro ao processar requisição GET /api/monografias para o usuário {}: {}", 
                         currentPrincipalName, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Erro interno ao buscar monografias. Detalhe: " + e.getMessage());
        }
    }

    // Exemplo de endpoint para buscar uma monografia por ID
    @GetMapping("/{id}")
    // @PreAuthorize("hasAnyRole('ALUNO', 'PROFESSOR', 'ADMIN') and @monografiaSecurityService.podeAcessarMonografia(authentication, #id)")
    public ResponseEntity<?> getMonografiaPorId(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication != null ? authentication.getName() : "ANONYMOUS";
        logger.info("MONOGRAFIA_CONTROLLER: Requisição GET /api/monografias/{} recebida pelo usuário: {}", id, currentPrincipalName);

        try {
            // O MonografiaService deve verificar se o usuário autenticado tem permissão para ver esta monografia específica
            Monografia monografia = monografiaService.findByIdAndUser(id, authentication); // Método de exemplo
            if (monografia != null) {
                logger.info("MONOGRAFIA_CONTROLLER: Monografia ID {} encontrada para o usuário {}", id, currentPrincipalName);
                return ResponseEntity.ok(monografia);
            } else {
                logger.warn("MONOGRAFIA_CONTROLLER: Monografia ID {} não encontrada ou acesso negado para o usuário {}", id, currentPrincipalName);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Monografia não encontrada ou acesso negado.");
            }
        } catch (Exception e) {
            logger.error("MONOGRAFIA_CONTROLLER: Erro ao buscar monografia ID {} para o usuário {}: {}", id, currentPrincipalName, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao buscar monografia.");
        }
    }

    // Exemplo de endpoint para criar uma nova monografia
    @PostMapping
    @PreAuthorize("hasAnyRole('ALUNO', 'PROFESSOR')") // Somente Alunos e Professores podem iniciar
    public ResponseEntity<?> criarMonografia(@RequestBody Monografia monografiaRequest, Authentication authentication) {
        String currentPrincipalName = authentication.getName();
        logger.info("MONOGRAFIA_CONTROLLER: Requisição POST /api/monografias recebida. Usuário: {}", currentPrincipalName);
        logger.debug("MONOGRAFIA_CONTROLLER: Payload da nova monografia: {}", monografiaRequest);

        try {
            Monografia novaMonografia = monografiaService.criarMonografia(monografiaRequest, authentication);
            logger.info("MONOGRAFIA_CONTROLLER: Monografia criada com ID {} pelo usuário {}", novaMonografia.getId(), currentPrincipalName);
            return ResponseEntity.status(HttpStatus.CREATED).body(novaMonografia);
        } catch (Exception e) {
            logger.error("MONOGRAFIA_CONTROLLER: Erro ao criar monografia para o usuário {}: {}", currentPrincipalName, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao criar monografia: " + e.getMessage());
        }
    }
}