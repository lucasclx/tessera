package com.backend.tessera.service;

import com.backend.tessera.model.AccountStatus;
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
                .filter(user -> user.getStatus() == AccountStatus.PENDENTE)
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
        
        // Se estiver aprovando o usuário
        if (approved) {
            user.setStatus(AccountStatus.ATIVO);
            user.setApprovalDate(LocalDateTime.now());
            user.setEnabled(true); // Habilitar a conta quando aprovada
            
            // Atualizar o papel se foi especificado e diferente do atual
            if (role != null && role != user.getRole()) {
                user.setRole(role);
            }
        } else {
            // Se estiver rejeitando a solicitação, manter como PENDENTE
            user.setStatus(AccountStatus.PENDENTE);
            user.setEnabled(false); // Desabilitar a conta quando rejeitada
        }
        
        // Atualizar comentários do administrador
        if (adminComments != null) {
            user.setAdminComments(adminComments);
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
        
        // Atualiza o status com base no parâmetro enabled
        if (enabled) {
            // Se já está aprovado, apenas ativa
            if (user.getStatus() == AccountStatus.ATIVO || user.getStatus() == AccountStatus.INATIVO) {
                user.setStatus(AccountStatus.ATIVO);
            }
        } else {
            // Desativa o usuário
            user.setStatus(AccountStatus.INATIVO);
        }
        
        // Atualiza o campo enabled para compatibilidade
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