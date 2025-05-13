package com.backend.tessera.service;

import com.backend.tessera.config.LoggerConfig;
import com.backend.tessera.model.AccountStatus;
import com.backend.tessera.model.Role;
import com.backend.tessera.model.User;
import com.backend.tessera.model.VerificationToken; // Importar VerificationToken
import com.backend.tessera.repository.UserRepository;
import com.backend.tessera.repository.VerificationTokenRepository; // Importar VerificationTokenRepository
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static final Logger logger = LoggerConfig.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired // Adicionar esta injeção
    private VerificationTokenRepository verificationTokenRepository;

    /**
     * Encontra todos os usuários pendentes de aprovação
     */
    public List<User> findPendingApprovalUsers() {
        logger.debug("Buscando usuários pendentes de aprovação");
        return userRepository.findAll().stream()
                .filter(user -> user.getStatus() == AccountStatus.PENDENTE)
                .collect(Collectors.toList());
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
                logger.debug("Atualizando papel do usuário ID: {} de {} para {}", userId, user.getRole(), role);
                user.setRole(role);
            }
        } else {
            // Se estiver rejeitando a solicitação, marcar como REJEITADO
            user.setStatus(AccountStatus.REJEITADO);
            user.setEnabled(false); // Desabilitar a conta quando rejeitada
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
            throw new IllegalArgumentException("Usuário não encontrado com ID: " + userId);
        }

        User user = userOpt.get();

        // Atualiza o status com base no parâmetro enabled
        if (enabled) {
            // Se já está aprovado ou inativo, apenas ativa (ou mantém ativo)
            // Não se deve ativar uma conta que está PENDENTE ou REJEITADA diretamente por aqui.
            if (user.getStatus() == AccountStatus.ATIVO || user.getStatus() == AccountStatus.INATIVO) {
                user.setStatus(AccountStatus.ATIVO);
            } else {
                 logger.warn("Tentativa de habilitar usuário ID: {} com status {} não permitido para esta operação.", userId, user.getStatus());
                 // Pode-se lançar uma exceção ou apenas logar, dependendo da regra de negócio.
                 // Por ora, vamos apenas logar e não alterar o status se não for ATIVO ou INATIVO.
            }
        } else {
            // Desativa o usuário, movendo para INATIVO se estiver ATIVO.
            if (user.getStatus() == AccountStatus.ATIVO) {
                user.setStatus(AccountStatus.INATIVO);
            }
             // Se já estiver INATIVO, PENDENTE ou REJEITADO, manter o status ao tentar desabilitar.
        }

        // Atualiza o campo enabled para consistência com o status
        // Se ATIVO, enabled = true. Caso contrário, enabled = false.
        user.setEnabled(user.getStatus() == AccountStatus.ATIVO);

        User savedUser = userRepository.save(user);
        logger.info("Status de ativação atualizado para usuário ID: {}, novo status: {}, enabled: {}",
                    userId, savedUser.getStatus(), savedUser.isEnabled());
        return savedUser;
    }

    /**
     * Deleta um usuário e seus tokens de verificação associados.
     */
    @Transactional
    public void deleteUser(Long userId) {
        logger.debug("Tentando deletar usuário ID: {}", userId);
        if (!userRepository.existsById(userId)) {
            logger.warn("Tentativa de deletar usuário inexistente, ID: {}", userId);
            throw new IllegalArgumentException("Usuário não encontrado com ID: " + userId);
        }

        // Buscar e deletar todos os verification tokens associados ao usuário
        // Melhoria: Adicionar um método findByUserId em VerificationTokenRepository
        // Por ora, faremos o filtro na lista retornada por findAll, o que pode não ser ideal para performance.
        // Uma abordagem mais eficiente seria criar um método no VerificationTokenRepository:
        // List<VerificationToken> findByUserId(Long userId);
        // E depois: verificationTokenRepository.deleteAll(tokens);
        // Ou até mesmo: void deleteAllByUserId(Long userId); (se o JPA provider suportar bem)

        List<VerificationToken> tokens = verificationTokenRepository.findAll().stream()
                .filter(token -> token.getUser() != null && token.getUser().getId().equals(userId))
                .collect(Collectors.toList());

        if (!tokens.isEmpty()) {
            logger.debug("Deletando {} token(s) de verificação para o usuário ID: {}", tokens.size(), userId);
            verificationTokenRepository.deleteAllInBatch(tokens); // Usar deleteAllInBatch para performance
        } else {
            logger.debug("Nenhum token de verificação encontrado para o usuário ID: {}", userId);
        }

        userRepository.deleteById(userId);
        logger.info("Usuário ID: {} deletado com sucesso", userId);
    }
}