package com.backend.tessera.auth.service;

import com.backend.tessera.auth.entity.AccountStatus; // Atualizado
import com.backend.tessera.auth.entity.User; // Atualizado
import com.backend.tessera.auth.repository.UserRepository; // Atualizado
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional(readOnly = true) // Boa prática para métodos de leitura
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    System.out.println("UserDetailsServiceImpl: Usuário não encontrado: " + username);
                    return new UsernameNotFoundException("Usuário não encontrado com nome: " + username);
                });

        System.out.println("UserDetailsServiceImpl: Carregando usuário: " + username + ", Status: " + user.getStatus() + ", EnabledField: " + user.isEnabledField() + ", Role: " + user.getRole());

        if (user.getStatus() == AccountStatus.PENDENTE) {
            System.out.println("UserDetailsServiceImpl: Conta PENDENTE para: " + username);
            throw new LockedException("Sua conta está aguardando aprovação do administrador. Tente novamente mais tarde.");
        }
        // A verificação de INATIVO ou !user.isEnabledField() é tratada pela implementação de User.isEnabled()
        // que o Spring Security chama. Se User.isEnabled() retorna false, DisabledException é lançada pelo framework.

        // Adicionalmente, se o papel não foi atribuído mesmo que a conta esteja ATIVA.
        if (user.getStatus() == AccountStatus.ATIVO && user.isEnabledField() && user.getRole() == null) {
            System.out.println("UserDetailsServiceImpl: Conta ATIVA e HABILITADA mas sem PAPEL para: " + username);
            throw new DisabledException("Conta aprovada mas sem papel atribuído. Entre em contato com o administrador.");
        }


        // O objeto User já implementa UserDetails.
        // As authorities são carregadas corretamente pelo getter em User.
        System.out.println("UserDetailsServiceImpl: Usuário " + username + " carregado com authorities: " + user.getAuthorities());
        return user;
    }
}