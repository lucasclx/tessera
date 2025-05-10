package com.backend.tessera.auth.controller;

import com.backend.tessera.auth.dto.AuthRequest; // Atualizado
import com.backend.tessera.auth.dto.AuthResponse; // Atualizado
import com.backend.tessera.auth.dto.MessageResponse; // Atualizado
import com.backend.tessera.auth.entity.AccountStatus; // Atualizado
import com.backend.tessera.auth.entity.User; // Atualizado
import com.backend.tessera.auth.repository.UserRepository; // Atualizado
import com.backend.tessera.security.JwtUtil; // Atualizado
import com.backend.tessera.auth.service.UserDetailsServiceImpl; // Atualizado

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class LoginController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    // Removido: UserDetailsServiceImpl é usado pelo AuthenticationManager/CustomAuthenticationProvider
    // @Autowired
    // private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody AuthRequest authRequest) {
        try {
            System.out.println("LoginController: Tentando autenticar usuário: " + authRequest.getUsername());

            // Verificações pré-autenticação (para mensagens mais específicas antes do AuthenticationManager)
             Optional<User> userOpt = userRepository.findByUsername(authRequest.getUsername());
             if (userOpt.isPresent()) {
                 User user = userOpt.get();
                 if (user.getStatus() == AccountStatus.PENDENTE) {
                     System.out.println("LoginController: Conta PENDENTE para: " + authRequest.getUsername());
                     return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(new MessageResponse("Sua conta está aguardando aprovação do administrador. Tente novamente mais tarde."));
                 }
                 // A lógica de INATIVO e enabled=false será tratada pelo CustomAuthenticationProvider, resultando em DisabledException.
             } else {
                // Usuário não existe, BadCredentialsException será lançada pelo AuthenticationManager
                System.out.println("LoginController: Usuário não encontrado na verificação prévia: " + authRequest.getUsername());
             }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );

            System.out.println("LoginController: Autenticação bem-sucedida para: " + authRequest.getUsername());

            // O Principal agora é um UserDetails (nossa entidade User que implementa UserDetails)
            final UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            System.out.println("LoginController: Detalhes do usuário carregados: " + userDetails.getUsername());
            System.out.println("LoginController: Autoridades: " + userDetails.getAuthorities());

            final String token = jwtUtil.generateToken(userDetails);
            // System.out.println("Token gerado: " + token.substring(0, Math.min(20, token.length())) + "...");

            // Extrai roles de forma simplificada, removendo o prefixo "ROLE_"
            Collection<String> roles = userDetails.getAuthorities().stream()
                                     .map(GrantedAuthority::getAuthority)
                                     .map(s -> s.startsWith("ROLE_") ? s.substring(5) : s)
                                     .collect(Collectors.toList());

            System.out.println("LoginController: Roles para resposta: " + roles);

            AuthResponse response = new AuthResponse(token, userDetails.getUsername(), roles);
            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            System.out.println("LoginController: Credenciais inválidas para: " + authRequest.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(new MessageResponse("Usuário ou senha inválidos"));
        } catch (LockedException e) { // Captura para PENDENTE vindo do CustomAuthenticationProvider
            System.out.println("LoginController: Conta bloqueada (provavelmente PENDENTE): " + authRequest.getUsername() + " - Msg: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                   .body(new MessageResponse(e.getMessage()));
        } catch (DisabledException e) { // Captura para INATIVO ou enabled=false vindo do CustomAuthenticationProvider
            System.out.println("LoginController: Conta desativada: " + authRequest.getUsername() + " - Msg: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                   .body(new MessageResponse(e.getMessage()));
        } catch (AuthenticationException e) { // Outras exceções de autenticação
            System.out.println("LoginController: Erro de autenticação genérico para: " + authRequest.getUsername() + " - " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                   .body(new MessageResponse("Erro de autenticação: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("LoginController: Erro inesperado na autenticação para " + authRequest.getUsername() + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                   .body(new MessageResponse("Erro interno no servidor durante a autenticação."));
        }
    }

    @GetMapping("/check-approval/{username}")
    public ResponseEntity<?> checkApprovalStatus(@PathVariable String username) {
        try {
            Optional<User> userOpt = userRepository.findByUsername(username);

            if (userOpt.isEmpty()) {
                System.out.println("check-approval: Usuário não encontrado: " + username);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                       .body(new MessageResponse("Usuário não encontrado"));
            }

            User user = userOpt.get();

            if (user.getStatus() == AccountStatus.INATIVO || !user.isEnabledField()) { // Verifica isEnabled() da entidade
                System.out.println("check-approval: Usuário INATIVO ou DESABILITADO: " + username);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                       .body(new MessageResponse("ACCOUNT_DISABLED"));
            }

            if (user.getStatus() == AccountStatus.PENDENTE) {
                System.out.println("check-approval: Usuário PENDENTE: " + username);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                       .body(new MessageResponse("PENDING_APPROVAL"));
            }

            // Se chegou aqui, usuário está ATIVO e enabled=true
            if (user.getRole() == null) {
                System.out.println("check-approval: Usuário ATIVO mas sem PAPEL: " + username);
                return ResponseEntity.status(HttpStatus.FORBIDDEN) // Ou outro status apropriado
                       .body(new MessageResponse("ROLE_MISSING")); // Mensagem para o frontend tratar
            }

            System.out.println("check-approval: Usuário APROVADO e ATIVO: " + username);
            return ResponseEntity.ok(new MessageResponse("APPROVED"));

        } catch (Exception e) {
            System.err.println("check-approval: Erro ao verificar status de aprovação para " + username + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                   .body(new MessageResponse("Erro ao verificar status: " + e.getMessage()));
        }
    }
}