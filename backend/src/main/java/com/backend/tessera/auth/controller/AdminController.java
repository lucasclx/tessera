package com.backend.tessera.auth.controller;

import com.backend.tessera.auth.dto.MessageResponse; // Atualizado
import com.backend.tessera.auth.dto.UserApprovalRequest; // Atualizado
import com.backend.tessera.auth.dto.UserDetailsResponse; // Atualizado
import com.backend.tessera.auth.entity.AccountStatus; // Atualizado
import com.backend.tessera.auth.entity.Role; // Atualizado
import com.backend.tessera.auth.entity.User; // Atualizado
import com.backend.tessera.auth.repository.UserRepository; // Atualizado
import com.backend.tessera.auth.service.UserService; // Atualizado
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin") // Mantendo o path original por enquanto
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @GetMapping("/users")
    public ResponseEntity<List<UserDetailsResponse>> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserDetailsResponse> userResponses = users.stream()
                .map(this::convertToUserDetailsResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userResponses);
    }

    @GetMapping("/users/pending")
    public ResponseEntity<List<UserDetailsResponse>> getPendingUsers() {
        List<User> pendingUsers = userService.findPendingApprovalUsers();
        List<UserDetailsResponse> userResponses = pendingUsers.stream()
                .map(this::convertToUserDetailsResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userResponses);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserDetails(@PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(convertToUserDetailsResponse(userOpt.get()));
    }

    @PutMapping("/users/{id}/approval")
    public ResponseEntity<?> updateUserApproval(
            @PathVariable Long id,
            @Valid @RequestBody UserApprovalRequest approvalRequest) {
        try {
            User updatedUser = userService.updateUserApprovalStatus(
                id,
                approvalRequest.isApproved(),
                approvalRequest.getRole() != null ? Role.valueOf(approvalRequest.getRole().toUpperCase()) : null, // Garantir Uppercase para o Enum
                approvalRequest.getAdminComments()
            );
            return ResponseEntity.ok(convertToUserDetailsResponse(updatedUser));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

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

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(new MessageResponse("Usuário deletado com sucesso"));
        } catch (IllegalArgumentException e) {
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
                null, // requestedRole não é mais usado/preenchido aqui
                user.getStatus() == AccountStatus.ATIVO, // isApproved
                user.getApprovalDate(),
                user.getAdminComments(),
                user.isEnabledField(), // Usar o getter do campo booleano
                user.getCreatedAt()
        );
    }
}