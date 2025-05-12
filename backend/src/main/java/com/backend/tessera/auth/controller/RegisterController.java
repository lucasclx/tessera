package com.backend.tessera.auth.controller;

import com.backend.tessera.auth.entity.AccountStatus;
import com.backend.tessera.auth.entity.Role;
import com.backend.tessera.auth.entity.User;
import com.backend.tessera.auth.dto.SignupRequest;
import com.backend.tessera.auth.dto.MessageResponse;
import com.backend.tessera.auth.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid; // Import correto

import java.util.Set;

@RestController
@RequestMapping("/api/auth")
public class RegisterController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        try {
            System.out.println("RegisterController: Recebendo requisição de registro para: " + signUpRequest.getUsername());

            if (userRepository.existsByUsername(signUpRequest.getUsername())) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Erro: Nome de usuário já está em uso!"));
            }

            if (userRepository.existsByEmail(signUpRequest.getEmail())) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Erro: Email já está em uso!"));
            }

            Role userRole = null;
            Set<String> strRoles = signUpRequest.getRole();

            if (strRoles == null || strRoles.isEmpty()) {
                 return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Erro: Perfil (Role) não especificado."));
            }

            String roleStr = strRoles.iterator().next().toUpperCase();
            try {
                userRole = Role.valueOf(roleStr);
                 if (userRole == Role.ADMIN) {
                    return ResponseEntity
                            .badRequest()
                            .body(new MessageResponse("Erro: Registro como ADMIN não é permitido."));
                }
            } catch (IllegalArgumentException e) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Erro: Perfil (Role) '" + roleStr + "' inválido."));
            }

            // Usando o construtor completo
            User user = new User(
                signUpRequest.getNome(),
                signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()),
                signUpRequest.getInstitution(),
                userRole,
                AccountStatus.PENDENTE,
                false // enabled
            );
            user.setAdminComments("Aguardando aprovação do administrador.");

            userRepository.save(user);

            System.out.println("RegisterController: Usuário registrado com sucesso: " + user.getUsername() + ", Papel: " + user.getRole() + ", Status: " + user.getStatus());

            String message = "Usuário registrado com sucesso! Sua conta será analisada pelos administradores.";
            return ResponseEntity.ok(new MessageResponse(message));

        } catch (Exception e) {
            System.err.println("RegisterController: Erro ao registrar usuário: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity
                    .internalServerError()
                    .body(new MessageResponse("Erro ao processar o registro: " + e.getMessage()));
        }
    }
}