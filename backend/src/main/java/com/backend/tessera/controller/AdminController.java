package com.backend.tessera.controller;

import com.backend.tessera.dto.MessageResponse;
import com.backend.tessera.dto.UserApprovalRequest;
import com.backend.tessera.dto.UserDetailsResponse;
import com.backend.tessera.model.AccountStatus;
import com.backend.tessera.model.Role;
import com.backend.tessera.model.User;
import com.backend.tessera.repository.UserRepository;
import com.backend.tessera.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @GetMapping("/users")
    public ResponseEntity<List<UserDetailsResponse>> getAllUsers() {
        logger.debug("Admin solicitou a lista de todos os usuários.");
        List<User> users = userRepository.findAll();
        List<UserDetailsResponse> userResponses = users.stream()
                .map(this::convertToUserDetailsResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userResponses);
    }

    @GetMapping("/users/pending")
    public ResponseEntity<List<UserDetailsResponse>> getPendingUsers() {
        logger.debug("Admin solicitou a lista de usuários pendentes.");
        List<User> pendingUsers = userService.findPendingApprovalUsers();
        List<UserDetailsResponse> userResponses = pendingUsers.stream()
                .map(this::convertToUserDetailsResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userResponses);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserDetails(@PathVariable Long id) {
        logger.debug("Admin solicitou detalhes do usuário com ID: {}", id);
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            logger.warn("Usuário com ID: {} não encontrado para visualização de detalhes.", id);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(convertToUserDetailsResponse(userOpt.get()));
    }

    @PutMapping("/users/{id}/approval")
    public ResponseEntity<?> updateUserApproval(
            @PathVariable Long id,
            @Valid @RequestBody UserApprovalRequest approvalRequest) {
        logger.info("Admin atualizando status de aprovação para usuário ID: {}. Aprovado: {}, Role: {}",
                id, approvalRequest.isApproved(), approvalRequest.getRole());
        try {
            User updatedUser = userService.updateUserApprovalStatus(
                    id,
                    approvalRequest.isApproved(),
                    approvalRequest.getRole() != null ? Role.valueOf(approvalRequest.getRole().toUpperCase()) : null,
                    approvalRequest.getAdminComments()
            );
            logger.info("Usuário ID: {} atualizado. Aprovado: {}, Role: {}, AdminComments: {}",
                    id, updatedUser.isApproved(), updatedUser.getRole(), updatedUser.getAdminComments());
            return ResponseEntity.ok(convertToUserDetailsResponse(updatedUser));
        } catch (IllegalArgumentException e) {
            logger.warn("Falha ao atualizar aprovação para usuário ID: {}. Motivo: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<?> updateUserStatus(
            @PathVariable Long id,
            @RequestParam boolean enabled) {
        logger.info("Admin atualizando status (enabled={}) para usuário ID: {}", enabled, id);
        try {
            User updatedUser = userService.updateUserStatus(id, enabled);
            logger.info("Usuário ID: {} atualizado. Novo status enabled: {}, AccountStatus: {}", id, updatedUser.isEnabled(), updatedUser.getStatus());
            return ResponseEntity.ok(convertToUserDetailsResponse(updatedUser));
        } catch (IllegalArgumentException e) {
            logger.warn("Falha ao atualizar status para usuário ID: {}. Motivo: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        logger.info("Admin solicitou a deleção do usuário ID: {}", id);
        try {
            userService.deleteUser(id);
            logger.info("Usuário ID: {} deletado com sucesso.", id);
            return ResponseEntity.ok(new MessageResponse("Usuário deletado com sucesso"));
        } catch (IllegalArgumentException e) {
            logger.warn("Falha ao deletar usuário ID: {}. Motivo: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    private UserDetailsResponse convertToUserDetailsResponse(User user) {
        return new UserDetailsResponse(
                user.getId(),
                user.getNome(),
                user.getUsername(),
                user.getEmail(),
                user.getInstitution(),
                user.getRole() != null ? user.getRole().name() : null,
                null, 
                user.isApproved(),
                user.getApprovalDate(),
                user.getAdminComments(),
                user.isEnabled(), // Aqui usamos o isEnabled() que considera o status ATIVO
                user.getCreatedAt()
        );
    }
}