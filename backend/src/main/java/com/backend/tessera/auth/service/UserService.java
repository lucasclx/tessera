package com.backend.tessera.auth.service;

import com.backend.tessera.auth.entity.AccountStatus; // Atualizado
import com.backend.tessera.auth.entity.Role; // Atualizado
import com.backend.tessera.auth.entity.User; // Atualizado
import com.backend.tessera.auth.repository.UserRepository; // Atualizado
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
// Removido: import org.springframework.security.crypto.password.PasswordEncoder; se não for usado para re-setar senhas aqui

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<User> findPendingApprovalUsers() {
        return userRepository.findAll().stream()
                .filter(user -> user.getStatus() == AccountStatus.PENDENTE)
                .collect(Collectors.toList());
    }

    @Transactional
    public User updateUserApprovalStatus(Long userId, boolean approved, Role role, String adminComments) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + userId));

        if (approved) {
            if (role == null) {
                throw new IllegalArgumentException("Papel (Role) é obrigatório ao aprovar um usuário.");
            }
            user.setStatus(AccountStatus.ATIVO);
            user.setApprovalDate(LocalDateTime.now());
            user.setEnabled(true); // Habilitar a conta ao aprovar
            user.setRole(role); // Definir o papel
            System.out.println("UserService: Usuário ID " + userId + " APROVADO. Role: " + role + ", Status: ATIVO, Enabled: true");
        } else {
            // Se rejeitado, o status permanece PENDENTE, mas pode-se desabilitar explicitamente.
            // Ou mudar para INATIVO se "rejeitado" for um estado final.
            // Por ora, mantemos PENDENTE e desabilitado.
            user.setStatus(AccountStatus.PENDENTE); // Ou AccountStatus.INATIVO se "rejeitado" for final
            user.setEnabled(false); // Desabilitar a conta
            // user.setRole(null); // Opcional: remover o papel se rejeitado? Depende da lógica de negócio.
            System.out.println("UserService: Usuário ID " + userId + " NÃO APROVADO. Status: " + user.getStatus() + ", Enabled: false");
        }

        if (adminComments != null && !adminComments.trim().isEmpty()) {
            user.setAdminComments(adminComments);
        } else if (approved) {
            user.setAdminComments("Usuário aprovado pelo administrador.");
        } else {
            user.setAdminComments("Solicitação de usuário não aprovada pelo administrador.");
        }

        return userRepository.save(user);
    }

    @Transactional
    public User updateUserStatus(Long userId, boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + userId));

        user.setEnabled(enabled); // Atualiza o campo 'enabled' diretamente

        if (enabled) {
            // Se habilitando, e o usuário já estava APROVADO (ATIVO ou INATIVO anteriormente),
            // garantir que o status seja ATIVO.
            // Não mudar PENDENTE para ATIVO aqui, isso é feito em updateUserApprovalStatus.
            if (user.getStatus() != AccountStatus.PENDENTE) {
                user.setStatus(AccountStatus.ATIVO);
            }
             System.out.println("UserService: Usuário ID " + userId + " HABILITADO. Status: " + user.getStatus() + ", Enabled: true");
        } else {
            // Se desabilitando, o status deve ser INATIVO, a menos que seja PENDENTE.
            if (user.getStatus() != AccountStatus.PENDENTE) {
                user.setStatus(AccountStatus.INATIVO);
            }
            System.out.println("UserService: Usuário ID " + userId + " DESABILITADO. Status: " + user.getStatus() + ", Enabled: false");
        }

        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("Usuário não encontrado com ID: " + userId + " para deleção.");
        }
        userRepository.deleteById(userId);
        System.out.println("UserService: Usuário ID " + userId + " deletado.");
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    @Transactional(readOnly = true)
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }
}