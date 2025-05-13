package com.backend.tessera.service;

import com.backend.tessera.config.LoggerConfig;
import com.backend.tessera.model.AccountStatus;
import com.backend.tessera.model.User;
import com.backend.tessera.repository.UserRepository;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private static final Logger logger = LoggerConfig.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("Carregando detalhes do usuário: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("Usuário não encontrado: {}", username);
                    return new UsernameNotFoundException("Usuário não encontrado: " + username);
                });
        
        // Verificar o status da conta
        if (user.getStatus() == AccountStatus.PENDENTE) {
            logger.warn("Tentativa de acesso com conta pendente: {}", username);
            throw new LockedException("Conta aguardando aprovação do administrador");
        } else if (user.getStatus() == AccountStatus.REJEITADO) {
            logger.warn("Tentativa de acesso com conta rejeitada: {}", username);
            throw new LockedException("Conta rejeitada pelo administrador. Entre em contato para mais informações.");
        } else if (user.getStatus() == AccountStatus.INATIVO) {
            logger.warn("Tentativa de acesso com conta inativa: {}", username);
            throw new DisabledException("Conta desativada");
        }
        
        // Verificar se a conta está habilitada (campo enabled)
        if (!user.isEnabled()) {
            logger.warn("Tentativa de acesso com conta desabilitada: {}", username);
            throw new DisabledException("Conta desabilitada");
        }
        
        logger.debug("Detalhes do usuário carregados com sucesso: {}", username);
        return user;
    }
}