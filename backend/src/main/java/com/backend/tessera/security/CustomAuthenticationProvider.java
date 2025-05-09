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
            throw new BadCredentialsException("Usuário ou senha inválidos");
        }

        User user = userOpt.get();

        // Verificar se o usuário está aprovado
        if (!user.isApproved()) {
            throw new LockedException("Conta aguardando aprovação do administrador");
        }

        // Verificar se a conta está habilitada
        if (!user.isEnabled()) {
            throw new DisabledException("Conta desativada");
        }

        // Verificar a senha
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Usuário ou senha inválidos");
        }

        // Autenticação bem-sucedida
        return new UsernamePasswordAuthenticationToken(user, password, user.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}