package com.backend.tessera.controller;

import com.backend.tessera.config.LoggerConfig;
import com.backend.tessera.dto.MessageResponse;
import com.backend.tessera.dto.UserApprovalRequest;
import com.backend.tessera.dto.UserDetailsResponse;
import com.backend.tessera.exception.ResourceNotFoundException;
import com.backend.tessera.model.AccountStatus;
import com.backend.tessera.model.Role;
import com.backend.tessera.model.User;
import com.backend.tessera.repository.UserRepository;
import com.backend.tessera.service.AuditService;
import com.backend.tessera.service.UserService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private static final Logger logger = LoggerConfig.getLogger(AdminController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;
    
    @Autowired
    private AuditService auditService;

    /**
     * Obtém a lista de todos os usuários (paginada)
     */
    @GetMapping("/users")
    public ResponseEntity<Page<UserDetailsResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        logger.debug("Buscando todos os usuários - página: {}, tamanho: {}", page, size);
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<User> userPage = userRepository.findAll(pageable);
        Page<UserDetailsResponse> userResponses = userPage.map(this::convertToUserDetailsResponse);
        
        logger.debug("Retornando {} usuários (página {} de {})", 
                    userResponses.getNumberOfElements(),
                    userResponses.getNumber() + 1,
                    userResponses.getTotalPages());
                    
        return ResponseEntity.ok(userResponses);
    }

    /**
     * Obtém a lista de usuários pendentes de aprovação (paginada)
     */
    @GetMapping("/users/pending")
    public ResponseEntity<Page<UserDetailsResponse>> getPendingUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        logger.debug("Buscando usuários pendentes - página: {}, tamanho: {}", page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<User> pendingUsersPage = userRepository.findByStatus(AccountStatus.PENDENTE, pageable);
        Page<UserDetailsResponse> userResponses = pendingUsersPage.map(this::convertToUserDetailsResponse);
        
        logger.debug("Retornando {} usuários pendentes (página {} de {})", 
                    userResponses.getNumberOfElements(),
                    userResponses.getNumber() + 1,
                    userResponses.getTotalPages());
                    
        return ResponseEntity.ok(userResponses);
    }

    /**
     * Método de compatibilidade para obter todos os usuários pendentes sem paginação
     */
    @GetMapping("/users/all-pending")
    public ResponseEntity<List<UserDetailsResponse>> getAllPendingUsers() {
        logger.debug("Buscando todos os usuários pendentes (sem paginação)");
        List<User> pendingUsers = userService.findPendingApprovalUsers();
        List<UserDetailsResponse> userResponses = pendingUsers.stream()
                .map(this::convertToUserDetailsResponse)
                .toList();
        
        logger.debug("Retornando {} usuários pendentes", userResponses.size());
        return ResponseEntity.ok(userResponses);
    }

    /**
     * Obtém detalhes de um usuário específico
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserDetails(@PathVariable Long id) {
        logger.debug("Buscando detalhes do usuário ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Usuário não encontrado com ID: {}", id);
                    return new ResourceNotFoundException("Usuário não encontrado com ID: " + id);
                });
        
        logger.debug("Usuário encontrado: {}", user.getUsername());
        return ResponseEntity.ok(convertToUserDetailsResponse(user));
    }

    /**
     * Aprova ou rejeita a solicitação de um usuário
     */
    @PutMapping("/users/{id}/approval")
    public ResponseEntity<?> updateUserApproval(
            @PathVariable Long id, 
            @Valid @RequestBody UserApprovalRequest approvalRequest,
            Principal principal) {
        
        logger.debug("Atualizando aprovação do usuário ID: {}, approved: {}", id, approvalRequest.isApproved());
        
        try {
            User updatedUser = userService.updateUserApprovalStatus(
                id, 
                approvalRequest.isApproved(), 
                approvalRequest.getRole() != null ? Role.valueOf(approvalRequest.getRole()) : null,
                approvalRequest.getAdminComments()
            );
            
            // Log da operação para auditoria
            auditService.logUserApproval(
                principal.getName(),
                id,
                approvalRequest.isApproved()
            );
            
            logger.info("Usuário ID: {} {} por {}", id, 
                       (approvalRequest.isApproved() ? "aprovado" : "rejeitado"), 
                       principal.getName());
            
            return ResponseEntity.ok(convertToUserDetailsResponse(updatedUser));
        } catch (Exception e) {
            logger.error("Erro ao atualizar aprovação do usuário ID: {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    /**
     * Atualiza o status de um usuário (ativação/desativação)
     */
    @PutMapping("/users/{id}/status")
    public ResponseEntity<?> updateUserStatus(
            @PathVariable Long id, 
            @RequestParam boolean enabled,
            Principal principal) {
        
        logger.debug("Atualizando status do usuário ID: {}, enabled: {}", id, enabled);
        
        try {
            User updatedUser = userService.updateUserStatus(id, enabled);
            
            // Log da operação para auditoria
            auditService.logUserStatusChange(
                principal.getName(),
                id,
                enabled
            );
            
            logger.info("Status do usuário ID: {} alterado para {} por {}", 
                       id, (enabled ? "ativo" : "inativo"), principal.getName());
            
            return ResponseEntity.ok(convertToUserDetailsResponse(updatedUser));
        } catch (Exception e) {
            logger.error("Erro ao atualizar status do usuário ID: {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    /**
     * Deleta um usuário
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, Principal principal) {
        logger.debug("Deletando usuário ID: {}", id);
        
        try {
            // Registrar a ação antes de deletar para manter o registro de auditoria
            auditService.logUserApproval(principal.getName(), id, false);
            
            userService.deleteUser(id);
            
            logger.info("Usuário ID: {} deletado por {}", id, principal.getName());
            return ResponseEntity.ok(new MessageResponse("Usuário deletado com sucesso"));
        } catch (Exception e) {
            logger.error("Erro ao deletar usuário ID: {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    /**
     * Método auxiliar para converter User em UserDetailsResponse
     */
    private UserDetailsResponse convertToUserDetailsResponse(User user) {
        return new UserDetailsResponse(
                user.getId(),
                user.getNome(),
                user.getUsername(),
                user.getEmail(),
                user.getInstitution(),
                user.getRole() != null ? user.getRole().name() : null,
                null, // requestedRole não é mais usado
                user.isApproved(),
                user.getApprovalDate(),
                user.getAdminComments(),
                user.isEnabled(), // Agora isso é determinado pelo status
                user.getCreatedAt()
        );
    }
}