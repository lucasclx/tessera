package com.backend.tessera.controller;

import com.backend.tessera.dto.MessageResponse;
import com.backend.tessera.dto.UserApprovalRequest;
import com.backend.tessera.dto.UserDetailsResponse;
import com.backend.tessera.model.Role;
import com.backend.tessera.model.User;
import com.backend.tessera.repository.UserRepository;
import com.backend.tessera.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")  // Restringe acesso apenas para administradores
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    /**
     * Obtém a lista de todos os usuários
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserDetailsResponse>> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserDetailsResponse> userResponses = users.stream()
                .map(this::convertToUserDetailsResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(userResponses);
    }

    /**
     * Obtém a lista de usuários pendentes de aprovação
     */
    @GetMapping("/users/pending")
    public ResponseEntity<List<UserDetailsResponse>> getPendingUsers() {
        List<User> pendingUsers = userService.findPendingApprovalUsers();
        List<UserDetailsResponse> userResponses = pendingUsers.stream()
                .map(this::convertToUserDetailsResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(userResponses);
    }

    /**
     * Obtém detalhes de um usuário específico
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserDetails(@PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(convertToUserDetailsResponse(userOpt.get()));
    }

    /**
     * Aprova ou rejeita a solicitação de um usuário
     */
    @PutMapping("/users/{id}/approval")
    public ResponseEntity<?> updateUserApproval(
            @PathVariable Long id, 
            @Valid @RequestBody UserApprovalRequest approvalRequest) {
        
        try {
            User updatedUser = userService.updateUserApprovalStatus(
                id, 
                approvalRequest.isApproved(), 
                approvalRequest.getRole() != null ? Role.valueOf(approvalRequest.getRole()) : null,
                approvalRequest.getAdminComments()
            );
            
            return ResponseEntity.ok(convertToUserDetailsResponse(updatedUser));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    /**
     * Atualiza o status de um usuário (ativação/desativação)
     */
    @PutMapping("/users/{id}/status")
    public ResponseEntity<?> updateUserStatus(
            @PathVariable Long id, 
            @RequestParam boolean enabled) {
        
        try {
            User updatedUser = userService.updateUserStatus(id, enabled);
            return ResponseEntity.ok(convertToUserDetailsResponse(updatedUser));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    /**
     * Deleta um usuário
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(new MessageResponse("Usuário deletado com sucesso"));
        } catch (IllegalArgumentException e) {
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
                user.getRole().name(),
                user.getRequestedRole() != null ? user.getRequestedRole().name() : null,
                user.isApproved(),
                user.getApprovalDate(),
                user.getAdminComments(),
                user.isEnabled(),
                user.getCreatedAt()
        );
    }
}