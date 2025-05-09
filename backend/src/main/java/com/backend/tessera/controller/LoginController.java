package com.backend.tessera.controller;

import com.backend.tessera.dto.AuthRequest;
import com.backend.tessera.dto.AuthResponse;
import com.backend.tessera.dto.MessageResponse;
import com.backend.tessera.model.User;
import com.backend.tessera.repository.UserRepository;
import com.backend.tessera.security.JwtUtil;
import com.backend.tessera.service.UserDetailsServiceImpl;

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
import org.springframework.web.server.ResponseStatusException;
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

    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody AuthRequest authRequest) {
        try {
            System.out.println("Tentando autenticar usuário: " + authRequest.getUsername());
            
            // Primeiro verificamos se o usuário existe e está aprovado
            Optional<User> userOpt = userRepository.findByUsername(authRequest.getUsername());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (!user.isApproved()) {
                    System.out.println("Conta aguardando aprovação: " + authRequest.getUsername());
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                           .body(new MessageResponse("Sua conta está aguardando aprovação do administrador. Tente novamente mais tarde."));
                }
                
                if (!user.isEnabled()) {
                    System.out.println("Conta desativada: " + authRequest.getUsername());
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                           .body(new MessageResponse("Sua conta está desativada. Entre em contato com o administrador."));
                }
            }
            
            // Tenta autenticar o usuário
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );
            
            System.out.println("Autenticação bem-sucedida para: " + authRequest.getUsername());
            
            final UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            System.out.println("Detalhes do usuário carregados: " + userDetails.getUsername());
            System.out.println("Autoridades: " + userDetails.getAuthorities());
            
            final String token = jwtUtil.generateToken(userDetails);
            System.out.println("Token gerado: " + token.substring(0, Math.min(20, token.length())) + "...");
            
            Collection<String> roles = userDetails.getAuthorities().stream()
                                     .map(GrantedAuthority::getAuthority)
                                     .map(s -> s.startsWith("ROLE_") ? s.substring(5) : s)
                                     .collect(Collectors.toList());
            
            System.out.println("Roles para resposta: " + roles);
            
            AuthResponse response = new AuthResponse(token, userDetails.getUsername(), roles);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            System.out.println("Credenciais inválidas para: " + authRequest.getUsername());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário ou senha inválidos", e);
        } catch (LockedException e) {
            System.out.println("Conta aguardando aprovação: " + authRequest.getUsername());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                   .body(new MessageResponse("Sua conta está aguardando aprovação do administrador. Tente novamente mais tarde."));
        } catch (DisabledException e) {
            System.out.println("Conta desativada: " + authRequest.getUsername());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                   .body(new MessageResponse("Sua conta está desativada. Entre em contato com o administrador."));
        } catch (AuthenticationException e) {
            System.out.println("Erro de autenticação para: " + authRequest.getUsername() + " - " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                   .body(new MessageResponse("Erro de autenticação: " + e.getMessage()));
        } catch (Exception e) {
            System.out.println("Erro inesperado na autenticação: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    @GetMapping("/check-approval/{username}")
    public ResponseEntity<?> checkApprovalStatus(@PathVariable String username) {
        try {
            // Buscar o usuário pelo username
            Optional<User> userOpt = userRepository.findByUsername(username);
            
            if (userOpt.isEmpty()) {
                System.out.println("Usuário não encontrado: " + username);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                       .body(new MessageResponse("Usuário não encontrado"));
            }
            
            User user = userOpt.get();
            
            // Verificar se o usuário está aprovado
            if (!user.isApproved()) {
                System.out.println("Usuário pendente de aprovação: " + username);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                       .body(new MessageResponse("PENDING_APPROVAL"));
            } 
            
            // Verificar se a conta está habilitada
            if (!user.isEnabled()) {
                System.out.println("Usuário com conta desativada: " + username);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                       .body(new MessageResponse("ACCOUNT_DISABLED"));
            }
            
            // Usuário está aprovado e ativo
            System.out.println("Usuário aprovado e ativo: " + username);
            return ResponseEntity.ok(new MessageResponse("APPROVED"));
        } catch (Exception e) {
            // Log do erro
            System.err.println("Erro ao verificar status de aprovação: " + e.getMessage());
            e.printStackTrace();
            
            // Retornar erro genérico para o cliente
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                   .body(new MessageResponse("Erro ao verificar status: " + e.getMessage()));
        }
    }
}