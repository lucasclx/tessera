package com.backend.tessera.security;

import com.backend.tessera.auth.entity.AccountStatus; // Atualizado
import com.backend.tessera.auth.entity.User; // Atualizado
import com.backend.tessera.auth.repository.UserRepository; // Atualizado
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails; // Importar UserDetails
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private UserRepository userRepository; // Vem de auth.repository

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            System.out.println("CustomAuthenticationProvider: Usuário não encontrado '" + username + "'");
            throw new BadCredentialsException("Usuário ou senha inválidos");
        }

        User user = userOpt.get();

        if (!passwordEncoder.matches(password, user.getPassword())) {
            System.out.println("CustomAuthenticationProvider: Credenciais inválidas para: " + username);
            throw new BadCredentialsException("Usuário ou senha inválidos");
        }

        // As verificações de status são importantes e devem corresponder à lógica do UserDetailsServiceImpl
        // e User.java (método isEnabled(), isAccountNonLocked(), etc.)
        if (user.getStatus() == AccountStatus.PENDENTE) {
            System.out.println("CustomAuthenticationProvider: Conta pendente de aprovação: " + username);
            // Usar LockedException para PENDENTE para corresponder ao UserDetailsServiceImpl
            throw new LockedException("Sua conta está aguardando aprovação do administrador. Tente novamente mais tarde.");
        } else if (user.getStatus() == AccountStatus.INATIVO) {
            System.out.println("CustomAuthenticationProvider: Conta inativa: " + username);
            throw new DisabledException("Conta desativada. Entre em contato com o administrador.");
        }

        // User.isEnabled() internamente já verifica se o status é ATIVO e o campo enabled é true.
        // O UserDetailsServiceImpl também tem essa lógica.
        // Aqui, podemos verificar o campo 'enabled' da entidade User diretamente
        // e também garantir que o status é ATIVO para consistência.
        if (!user.isEnabled() || user.getStatus() != AccountStatus.ATIVO) {
             String reason = "";
             if (user.getStatus() == AccountStatus.PENDENTE) reason = " (Pendente)";
             else if (user.getStatus() == AccountStatus.INATIVO) reason = " (Inativa)";
             else if (!user.isEnabledField()) reason = " (Campo 'enabled' é false)"; // Supondo um getter isEnabledField() para o booleano puro
             else reason = " (Status não é ATIVO)";


            System.out.println("CustomAuthenticationProvider: Conta desabilitada ou não ativa para login: " + username + reason);
            throw new DisabledException("Conta desabilitada ou não ativa. Entre em contato com o administrador.");
        }

        // Se chegou até aqui, o usuário é autenticado com sucesso
        // O objeto User implementa UserDetails, então pode ser usado diretamente.
        UserDetails userDetails = user;
        return new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}