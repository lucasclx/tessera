package com.backend.tessera.service;

import com.backend.tessera.config.LoggerConfig;
import com.backend.tessera.exception.ResourceNotFoundException;
import com.backend.tessera.model.AccountStatus;
import com.backend.tessera.model.Role;
import com.backend.tessera.model.User;
import com.backend.tessera.repository.UserRepository;
import com.backend.tessera.repository.VerificationTokenRepository;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private static final Logger logger = LoggerConfig.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    /**
     * Encontra todos os usuários pendentes de aprovação (paginados)
     */
    public Page<User> findPendingApprovalUsers(Pageable pageable) {
        logger.debug("Buscando usuários pendentes de aprovação");
        return userRepository.findByStatus(AccountStatus.PENDENTE, pageable);
    }

    /**
     * Lista não paginada de usuários pendentes (mantida para compatibilidade)
     */
    public List<User> findPendingApprovalUsers() {
        logger.debug("Buscando lista de usuários pendentes de aprovação");
        return userRepository.findByStatus(AccountStatus.PENDENTE);
    }

    /**
     * Atualiza o status de aprovação de um usuário
     */
    @Transactional
    public User updateUserApprovalStatus(Long userId, boolean approved, Role role, String adminComments) {
        logger.debug("Atualizando status de aprovação para usuário ID: {}, aprovado: {}", userId, approved);
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isEmpty()) {
            logger.warn("Usuário não encontrado com ID: {}", userId);
            throw new ResourceNotFoundException("Usuário não encontrado com ID: " + userId);
        }

        User user = userOpt.get();

        // Se estiver aprovando o usuário
        if (approved) {
            user.setStatus(AccountStatus.ATIVO);
            user.setApprovalDate(LocalDateTime.now());
            // Não é mais necessário setar enabled=true já que isso é derivado do status

            // Atualizar o papel se foi especificado e diferente do atual
            if (role != null && role != user.getRole()) {
                logger.debug("Atualizando papel do usuário ID: {} de {} para {}", userId, user.getRole(), role);
                user.setRole(role);
            }
        } else {
            // Se estiver rejeitando a solicitação, marcar como REJEITADO
            user.setStatus(AccountStatus.REJEITADO);
            // Não é mais necessário setar enabled=false já que isso é derivado do status
        }

        // Atualizar comentários do administrador
        if (adminComments != null) {
            user.setAdminComments(adminComments);
        }

        User savedUser = userRepository.save(user);
        logger.info("Status de aprovação atualizado para usuário ID: {}, novo status: {}", userId, savedUser.getStatus());
        return savedUser;
    }

    /**
     * Atualiza o status de ativação de um usuário
     */
    @Transactional
    public User updateUserStatus(Long userId, boolean enabled) {
        logger.debug("Atualizando status de ativação para usuário ID: {}, enabled: {}", userId, enabled);
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isEmpty()) {
            logger.warn("Usuário não encontrado com ID: {}", userId);
            throw new ResourceNotFoundException("Usuário não encontrado com ID: " + userId);
        }

        User user = userOpt.get();

        // Atualizar o status diretamente
        if (enabled) {
            // Só pode ativar contas que não estejam PENDENTE ou REJEITADO
            if (user.getStatus() != AccountStatus.PENDENTE && user.getStatus() != AccountStatus.REJEITADO) {
                user.setStatus(AccountStatus.ATIVO);
                logger.debug("Usuário ID: {} ativado com sucesso", userId);
            } else {
                logger.warn("Tentativa de habilitar usuário ID: {} com status {} não permitido para esta operação.", 
                           userId, user.getStatus());
                throw new IllegalStateException(
                    "Não é possível ativar um usuário com status " + user.getStatus() + 
                    ". É necessário aprovar o usuário primeiro.");
            }
        } else {
            // Desativa o usuário, movendo para INATIVO se estiver ATIVO
            if (user.getStatus() == AccountStatus.ATIVO) {
                user.setStatus(AccountStatus.INATIVO);
                logger.debug("Usuário ID: {} desativado com sucesso", userId);
            } else {
                logger.debug("Status do usuário ID: {} já estava como não-ativo: {}", userId, user.getStatus());
            }
        }

        User savedUser = userRepository.save(user);
        logger.info("Status atualizado para usuário ID: {}, novo status: {}", userId, savedUser.getStatus());
        return savedUser;
    }

    /**
     * Deleta um usuário e seus tokens associados
     */
    @Transactional
    public void deleteUser(Long userId) {
        logger.debug("Tentando deletar usuário ID: {}", userId);
        if (!userRepository.existsById(userId)) {
            logger.warn("Tentativa de deletar usuário inexistente, ID: {}", userId);
            throw new ResourceNotFoundException("Usuário não encontrado com ID: " + userId);
        }

        // Deletar todos os tokens associados ao usuário
        verificationTokenRepository.deleteByUserId(userId);
        logger.debug("Tokens de verificação deletados para o usuário ID: {}", userId);

        // Deletar o usuário
        userRepository.deleteById(userId);
        logger.info("Usuário ID: {} deletado com sucesso", userId);
    }
}