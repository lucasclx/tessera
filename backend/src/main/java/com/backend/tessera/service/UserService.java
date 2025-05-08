package com.backend.tessera.service;

import com.backend.tessera.model.Role;
import com.backend.tessera.model.User;
import com.backend.tessera.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Encontra todos os usuários pendentes de aprovação
     */
    public List<User> findPendingApprovalUsers() {
        return userRepository.findAll().stream()
                .filter(user -> !user.isApproved() || user.getRequestedRole() != null)
                .collect(Collectors.toList());
    }

    /**
     * Atualiza o status de aprovação de um usuário
     */
    @Transactional
    public User updateUserApprovalStatus(Long userId, boolean approved, Role role, String adminComments) {
        Optional<User> userOpt = userRepository.findById(userId);
        
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuário não encontrado com ID: " + userId);
        }
        
        User user = userOpt.get();
        
        // Atualiza o status de aprovação
        user.setApproved(approved);
        user.setApprovalDate(LocalDateTime.now());
        user.setAdminComments(adminComments);
        
        // Se aprovado e um papel foi especificado, atualiza o papel do usuário
        if (approved && role != null) {
            user.setRole(role);
            user.setRequestedRole(null); // Limpa a solicitação após aprovar
        } 
        // Se rejeitado, mantém o papel atual mas limpa a solicitação
        else if (!approved) {
            user.setRequestedRole(null);
        }
        
        return userRepository.save(user);
    }

    /**
     * Atualiza o status de ativação de um usuário
     */
    @Transactional
    public User updateUserStatus(Long userId, boolean enabled) {
        Optional<User> userOpt = userRepository.findById(userId);
        
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuário não encontrado com ID: " + userId);
        }
        
        User user = userOpt.get();
        user.setEnabled(enabled);
        
        return userRepository.save(user);
    }

    /**
     * Deleta um usuário
     */
    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("Usuário não encontrado com ID: " + userId);
        }
        
        userRepository.deleteById(userId);
    }
}