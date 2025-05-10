package com.backend.tessera.auth.controller;

import com.backend.tessera.auth.entity.AccountStatus; // Atualizado
import com.backend.tessera.auth.entity.Role; // Atualizado
import com.backend.tessera.auth.entity.User; // Atualizado
import com.backend.tessera.auth.dto.SignupRequest; // Atualizado
import com.backend.tessera.auth.dto.MessageResponse; // Atualizado
import com.backend.tessera.auth.repository.UserRepository; // Atualizado

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

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

            Role userRole = null; // Inicializa como null
            Set<String> strRoles = signUpRequest.getRole();

            if (strRoles == null || strRoles.isEmpty()) {
                 return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Erro: Perfil (Role) não especificado."));
            }

            // Assumindo que apenas um papel é enviado no Set para simplificar
            String roleStr = strRoles.iterator().next().toUpperCase();
            try {
                userRole = Role.valueOf(roleStr); // Converte string para Enum Role
                 if (userRole == Role.ADMIN) { // Não permitir auto-registro como ADMIN
                    return ResponseEntity
                            .badRequest()
                            .body(new MessageResponse("Erro: Registro como ADMIN não é permitido."));
                }
            } catch (IllegalArgumentException e) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Erro: Perfil (Role) '" + roleStr + "' inválido."));
            }


            User user = new User();
            user.setNome(signUpRequest.getNome());
            user.setUsername(signUpRequest.getUsername());
            user.setEmail(signUpRequest.getEmail());
            user.setPassword(encoder.encode(signUpRequest.getPassword()));
            user.setInstitution(signUpRequest.getInstitution());
            user.setRole(userRole); // Atribui o papel validado

            user.setStatus(AccountStatus.PENDENTE);
            user.setEnabled(false); // Novos usuários começam desabilitados
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