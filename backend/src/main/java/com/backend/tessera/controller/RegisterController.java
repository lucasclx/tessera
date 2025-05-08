package com.backend.tessera.controller;

import com.backend.tessera.model.Role;
import com.backend.tessera.model.User;
import com.backend.tessera.dto.SignupRequest;
import com.backend.tessera.dto.MessageResponse;
import com.backend.tessera.repository.UserRepository;

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

        // Removida a verificação de existência de CPF
        // if (userRepository.existsByCpf(signUpRequest.getCpf())) {
        //     return ResponseEntity
        //             .badRequest()
        //             .body(new MessageResponse("Erro: CPF já cadastrado!"));
        // }

        Role assignedRole = Role.ALUNO; // Papel padrão
        Set<String> strRoles = signUpRequest.getRole();

        if (strRoles != null && !strRoles.isEmpty()) {
            String roleStr = strRoles.iterator().next().toUpperCase();
            switch (roleStr) {
                case "PROFESSOR":
                    assignedRole = Role.PROFESSOR;
                    break;
                case "ALUNO":
                    assignedRole = Role.ALUNO;
                    break;
                case "ADMIN": // Supondo que ADMIN também seja uma opção válida
                    assignedRole = Role.ADMIN;
                    break;
                default:
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
        // Removido: user.setCpf(signUpRequest.getCpf());
        user.setRole(assignedRole);

        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Usuário registrado com sucesso!"));
    }
}