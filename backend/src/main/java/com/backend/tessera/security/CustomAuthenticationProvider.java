package com.backend.tessera.security;

import com.backend.tessera.model.AccountStatus;
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
 * Provedor de autenticação personalizado para verificar o status dos usuários
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

        // Verificar o status da conta
        if (user.getStatus() == AccountStatus.PENDENTE) {
            System.out.println("Conta pendente de aprovação: " + username);
            throw new LockedException("Conta aguardando aprovação do administrador");
        } else if (user.getStatus() == AccountStatus.INATIVO) {
            System.out.println("Conta inativa: " + username);
            throw new DisabledException("Conta desativada. Entre em contato com o administrador.");
        }

        // Verificar se a conta está habilitada (campo enabled)
        if (!user.isEnabled()) {
            System.out.println("Conta desabilitada: " + username);
            throw new DisabledException("Conta desabilitada. Entre em contato com o administrador.");
        }

        // Autenticação bem-sucedida
        return new UsernamePasswordAuthenticationToken(user, password, user.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}