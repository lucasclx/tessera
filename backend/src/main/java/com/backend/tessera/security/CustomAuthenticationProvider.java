package com.backend.tessera.security;

import com.backend.tessera.model.User;
import com.backend.tessera.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Provedor de autenticação personalizado para verificar a aprovação dos usuários
 */
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        // Buscar usuário pelo nome de usuário
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            System.out.println("Failed to find user '" + username + "'");
            throw new BadCredentialsException("Usuário ou senha inválidos");
        }

        User user = userOpt.get();

        // Verificar a senha
        if (!passwordEncoder.matches(password, user.getPassword())) {
            System.out.println("Credenciais inválidas para: " + username);
            throw new BadCredentialsException("Usuário ou senha inválidos");
        }

        // Verificar se o usuário está aprovado
        if (!user.isApproved()) {
            System.out.println("Conta aguardando aprovação: " + username);
            throw new LockedException("Conta aguardando aprovação do administrador");
        }
        
        // Verificar se o papel foi atribuído após aprovação
        if (user.getRole() == null) {
            System.out.println("Conta aprovada mas sem papel atribuído: " + username);
            throw new DisabledException("Conta com configuração incompleta. Entre em contato com o administrador.");
        }

        // Verificar se a conta está habilitada
        if (!user.isEnabled()) {
            System.out.println("Conta desativada: " + username);
            throw new DisabledException("Conta desativada");
        }

        // Autenticação bem-sucedida
        return new UsernamePasswordAuthenticationToken(user, password, user.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}