package com.backend.tessera.service;

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
        
        // Verificar se o usuário está aprovado
        if (!user.isApproved()) {
            throw new LockedException("Conta aguardando aprovação do administrador");
        }
        
        // Verificar se o papel foi atribuído após aprovação
        if (user.getRole() == null) {
            throw new DisabledException("Conta com configuração incompleta. Entre em contato com o administrador.");
        }
        
        // Verificar se a conta está habilitada
        if (!user.isEnabled()) {
            throw new DisabledException("Conta desativada");
        }
        
        return user;
    }
}