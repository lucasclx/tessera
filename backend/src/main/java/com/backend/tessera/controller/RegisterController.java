package com.backend.tessera.controller;

import com.backend.tessera.model.AccountStatus;
import com.backend.tessera.model.Role;
import com.backend.tessera.model.User;
import com.backend.tessera.dto.SignupRequest;
import com.backend.tessera.dto.MessageResponse;
import com.backend.tessera.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.Set;

@RestController
@RequestMapping("/api/auth")
public class RegisterController {

    private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        try {
            logger.debug("Recebendo requisição de registro para username: {}", signUpRequest.getUsername());

            if (userRepository.existsByUsername(signUpRequest.getUsername())) {
                logger.warn("Tentativa de registro com username já existente: {}", signUpRequest.getUsername());
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Erro: Nome de usuário já está em uso!"));
            }

            if (userRepository.existsByEmail(signUpRequest.getEmail())) {
                logger.warn("Tentativa de registro com email já existente: {}", signUpRequest.getEmail());
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Erro: Email já está em uso!"));
            }

            Role userRole = Role.ALUNO;
            Set<String> strRoles = signUpRequest.getRole();

            if (strRoles != null && !strRoles.isEmpty()) {
                String roleStr = strRoles.iterator().next().toUpperCase();
                try {
                    if (roleStr.equals("PROFESSOR")) {
                        userRole = Role.PROFESSOR;
                    } else if (roleStr.equals("ALUNO")) {
                        userRole = Role.ALUNO;
                    } else {
                        logger.warn("Tentativa de registro com perfil (Role) inválido: {}", roleStr);
                        return ResponseEntity
                                .badRequest()
                                .body(new MessageResponse("Erro: Perfil (Role) '" + roleStr + "' inválido."));
                    }
                } catch (IllegalArgumentException e) {
                    logger.warn("Tentativa de registro com perfil (Role) inválido: {}", roleStr, e);
                    return ResponseEntity
                            .badRequest()
                            .body(new MessageResponse("Erro: Perfil (Role) '" + roleStr + "' inválido."));
                }
            }

            User user = new User();
            user.setNome(signUpRequest.getNome());
            user.setUsername(signUpRequest.getUsername());
            user.setEmail(signUpRequest.getEmail());
            user.setPassword(encoder.encode(signUpRequest.getPassword()));
            user.setInstitution(signUpRequest.getInstitution());
            user.setRole(userRole);
            user.setStatus(AccountStatus.PENDENTE);
            user.setEnabled(false);
            user.setAdminComments("Aguardando aprovação do administrador");

            userRepository.save(user);

            logger.info("Usuário registrado com sucesso: {}, Papel: {}, Status: {}",
                    user.getUsername(), user.getRole(), user.getStatus());

            String message = "Usuário registrado com sucesso! Sua conta será analisada pelos administradores.";
            return ResponseEntity.ok(new MessageResponse(message));

        } catch (Exception e) {
            logger.error("Erro ao registrar usuário {}: {}", signUpRequest.getUsername(), e.getMessage(), e);
            return ResponseEntity
                    .internalServerError()
                    .body(new MessageResponse("Erro ao processar o registro: " + e.getMessage()));
        }
    }
}