package com.backend.tessera.controller;

import com.backend.tessera.config.LoggerConfig;
import com.backend.tessera.model.AccountStatus;
import com.backend.tessera.model.Role;
import com.backend.tessera.model.User;
import com.backend.tessera.dto.SignupRequest;
import com.backend.tessera.dto.MessageResponse;
import com.backend.tessera.repository.UserRepository;
import com.backend.tessera.service.EmailVerificationService;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.Set;

@RestController
@RequestMapping("/api/auth")
public class RegisterController {
    private static final Logger logger = LoggerConfig.getLogger(RegisterController.class);

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    EmailVerificationService emailVerificationService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        try {
            logger.debug("Recebendo requisição de registro: {}", signUpRequest.getUsername());
            
            // Verificação de username já existente
            if (userRepository.existsByUsername(signUpRequest.getUsername())) {
                logger.warn("Tentativa de registro com username já existente: {}", signUpRequest.getUsername());
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Erro: Nome de usuário já está em uso!"));
            }

            // Verificação de email já existente
            if (userRepository.existsByEmail(signUpRequest.getEmail())) {
                logger.warn("Tentativa de registro com email já existente: {}", signUpRequest.getEmail());
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Erro: Email já está em uso!"));
            }

            // Determinar o papel a partir da solicitação
            Role userRole = Role.ALUNO; // Papel padrão é ALUNO
            Set<String> strRoles = signUpRequest.getRole();

            if (strRoles != null && !strRoles.isEmpty()) {
                String roleStr = strRoles.iterator().next().toUpperCase();
                try {
                    if (roleStr.equals("PROFESSOR")) {
                        userRole = Role.PROFESSOR;
                    } else if (roleStr.equals("ALUNO")) {
                        userRole = Role.ALUNO;
                    } else {
                        logger.warn("Tentativa de registro com perfil inválido: {}", roleStr);
                        return ResponseEntity
                                .badRequest()
                                .body(new MessageResponse("Erro: Perfil (Role) '" + roleStr + "' inválido."));
                    }
                } catch (IllegalArgumentException e) {
                    logger.error("Erro ao processar perfil de usuário: {}", e.getMessage());
                    return ResponseEntity
                            .badRequest()
                            .body(new MessageResponse("Erro: Perfil (Role) '" + roleStr + "' inválido."));
                }
            }

            // Construir o objeto usuário
            User user = new User();
            user.setNome(signUpRequest.getNome());
            user.setUsername(signUpRequest.getUsername());
            user.setEmail(signUpRequest.getEmail());
            user.setPassword(encoder.encode(signUpRequest.getPassword()));
            user.setInstitution(signUpRequest.getInstitution());
            user.setRole(userRole);
            
            // Definir status como PENDENTE para novos usuários
            user.setStatus(AccountStatus.PENDENTE);
            user.setEnabled(false); // Conta não habilitada até ser aprovada
            user.setAdminComments("Aguardando aprovação do administrador");
            
            // Salvar no banco de dados
            userRepository.save(user);
            
            logger.info("Usuário registrado com sucesso: {}, Papel: {}, Status: {}", 
                        user.getUsername(), user.getRole(), user.getStatus());

            // Enviar email de verificação
            boolean emailSent = false;
            try {
                emailSent = emailVerificationService.sendVerificationEmail(user);
            } catch (Exception e) {
                logger.error("Erro ao enviar email de verificação: {}", e.getMessage());
                // Não impede o registro, mas loga o erro
            }
            
            String message = "Usuário registrado com sucesso! Sua conta será analisada pelos administradores. ";
            if (!emailSent) {
                message += "Não foi possível enviar o email de verificação. Por favor, solicite um novo email de verificação mais tarde.";
            } else {
                message += "Um email de verificação foi enviado.";
            }

            return ResponseEntity.ok(new MessageResponse(message));
        } catch (Exception e) {
            logger.error("Erro ao registrar usuário: {}", e.getMessage(), e);
            return ResponseEntity
                    .internalServerError()
                    .body(new MessageResponse("Erro ao processar o registro: " + e.getMessage()));
        }
    }
}