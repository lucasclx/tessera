package com.backend.tessera.security;

import com.backend.tessera.model.AccountStatus;
import com.backend.tessera.model.User;
import com.backend.tessera.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationProvider.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        logger.debug("Tentando autenticar usuário: {}", username);

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            logger.warn("Tentativa de login falhou. Usuário não encontrado: '{}'", username);
            throw new BadCredentialsException("Usuário ou senha inválidos");
        }

        User user = userOpt.get();

        if (!passwordEncoder.matches(password, user.getPassword())) {
            logger.warn("Tentativa de login falhou. Credenciais inválidas para: '{}'", username);
            throw new BadCredentialsException("Usuário ou senha inválidos");
        }

        if (user.getStatus() == AccountStatus.PENDENTE) {
            logger.info("Tentativa de login bloqueada. Conta pendente de aprovação: '{}'", username);
            throw new LockedException("Conta aguardando aprovação do administrador");
        } else if (user.getStatus() == AccountStatus.INATIVO) {
            logger.info("Tentativa de login bloqueada. Conta inativa: '{}'", username);
            throw new DisabledException("Conta desativada. Entre em contato com o administrador.");
        }

        if (!user.isEnabled()) {
            logger.info("Tentativa de login bloqueada. Conta desabilitada (enabled=false): '{}'", username);
            throw new DisabledException("Conta desabilitada. Entre em contato com o administrador.");
        }

        logger.info("Autenticação bem-sucedida para: '{}'", username);
        return new UsernamePasswordAuthenticationToken(user, password, user.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}