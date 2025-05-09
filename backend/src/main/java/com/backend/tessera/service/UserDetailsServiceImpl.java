package com.backend.tessera.service;

import com.backend.tessera.model.AccountStatus;
import com.backend.tessera.model.User;
import com.backend.tessera.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));
        
        // Verificar o status da conta
        if (user.getStatus() == AccountStatus.PENDENTE) {
            throw new LockedException("Conta aguardando aprovação do administrador");
        } else if (user.getStatus() == AccountStatus.INATIVO) {
            throw new DisabledException("Conta desativada");
        }
        
        // Verificar se a conta está habilitada (campo enabled)
        if (!user.isEnabled()) {
            throw new DisabledException("Conta desabilitada");
        }
        
        return user;
    }
}