package com.backend.tessera.controller;

import com.backend.tessera.model.AccountStatus;
import com.backend.tessera.model.Role;
import com.backend.tessera.model.User;
import com.backend.tessera.dto.SignupRequest;
import com.backend.tessera.dto.MessageResponse;
import com.backend.tessera.repository.UserRepository;
import com.backend.tessera.service.EmailVerificationService; // Importação adicionada

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.mail.MessagingException; // Importação adicionada

import java.util.Set;

@RestController
@RequestMapping("/api/auth")
public class RegisterController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired // Injeção adicionada
    EmailVerificationService emailVerificationService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        try {
            // Log para debug
            System.out.println("Recebendo requisição de registro: " + signUpRequest.getUsername());
            
            // Verificação de username já existente
            if (userRepository.existsByUsername(signUpRequest.getUsername())) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Erro: Nome de usuário já está em uso!"));
            }

            // Verificação de email já existente
            if (userRepository.existsByEmail(signUpRequest.getEmail())) {
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
                        return ResponseEntity
                                .badRequest()
                                .body(new MessageResponse("Erro: Perfil (Role) '" + roleStr + "' inválido."));
                    }
                } catch (IllegalArgumentException e) {
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
            
            System.out.println("Usuário registrado com sucesso: " + user.getUsername() + ", Papel: " + user.getRole() + ", Status: " + user.getStatus() + ", Enabled: " + user.isEnabled());

            // Enviar email de verificação
            try {
                emailVerificationService.sendVerificationEmail(user);
                System.out.println("Email de verificação enviado para: " + user.getEmail());
            } catch (MessagingException e) {
                System.err.println("Erro ao enviar email de verificação: " + e.getMessage());
                // Não impede o registro, mas loga o erro
            }

            String message = "Usuário registrado com sucesso! Sua conta será analisada pelos administradores. Um email de verificação foi enviado.";

            return ResponseEntity.ok(new MessageResponse(message));
        } catch (Exception e) {
            // Log de erro para debugging
            System.err.println("Erro ao registrar usuário: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity
                    .internalServerError()
                    .body(new MessageResponse("Erro ao processar o registro: " + e.getMessage()));
        }
    }
}