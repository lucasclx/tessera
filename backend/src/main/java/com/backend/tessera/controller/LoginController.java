package com.backend.tessera.controller;

import com.backend.tessera.dto.AuthRequest;
import com.backend.tessera.dto.AuthResponse;
import com.backend.tessera.dto.MessageResponse;
import com.backend.tessera.model.AccountStatus; // Adicionado para clareza
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
// Removido: import org.springframework.web.server.ResponseStatusException; (não mais usado aqui)
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
            
            Optional<User> userOpt = userRepository.findByUsername(authRequest.getUsername());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                if (!user.isApproved() && user.getStatus() == AccountStatus.PENDENTE) { // Verifica explicitamente PENDENTE
                    System.out.println("Conta aguardando aprovação: " + authRequest.getUsername());
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                           .body(new MessageResponse("Sua conta está aguardando aprovação do administrador. Tente novamente mais tarde."));
                }
                
                if (user.getRole() == null && user.isApproved()) { // Aprovado mas sem role
                    System.out.println("Conta aprovada mas sem papel atribuído: " + authRequest.getUsername());
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                           .body(new MessageResponse("Sua conta foi aprovada mas não possui um papel atribuído. Entre em contato com o administrador."));
                }
                
                // A verificação de conta desativada (INATIVO ou !enabled) será tratada pelo CustomAuthenticationProvider
                // e resultará em DisabledException.
            }
            
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
            // MODIFICADO: Retornar ResponseEntity<MessageResponse> em vez de ResponseStatusException
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(new MessageResponse("Usuário ou senha inválidos"));
        } catch (LockedException e) { // Tipicamente para contas PENDENTES via CustomAuthenticationProvider
            System.out.println("Conta bloqueada (pendente/etc.): " + authRequest.getUsername());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                   .body(new MessageResponse(e.getMessage())); // Usar a mensagem da exceção
        } catch (DisabledException e) { // Tipicamente para contas INATIVAS ou enabled=false via CustomAuthenticationProvider
            System.out.println("Conta desativada: " + authRequest.getUsername());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                   .body(new MessageResponse(e.getMessage())); // Usar a mensagem da exceção
        } catch (AuthenticationException e) {
            System.out.println("Erro de autenticação para: " + authRequest.getUsername() + " - " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                   .body(new MessageResponse("Erro de autenticação: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("Erro inesperado na autenticação para " + authRequest.getUsername() + ": " + e.getMessage());
            e.printStackTrace();
            // Considerar um erro mais genérico para o cliente em produção
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                   .body(new MessageResponse("Erro interno no servidor durante a autenticação."));
        }
    }
    
    @GetMapping("/check-approval/{username}")
    public ResponseEntity<?> checkApprovalStatus(@PathVariable String username) {
        try {
            Optional<User> userOpt = userRepository.findByUsername(username);
            
            if (userOpt.isEmpty()) {
                System.out.println("Usuário não encontrado (check-approval): " + username);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                       .body(new MessageResponse("Usuário não encontrado"));
            }
            
            User user = userOpt.get();
            
            // MODIFICADO: Ordem das verificações
            if (user.getStatus() == AccountStatus.INATIVO) {
                System.out.println("Usuário com conta INATIVA (via check-approval): " + username);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                       .body(new MessageResponse("ACCOUNT_DISABLED"));
            }
            
            // user.isApproved() verifica se status == AccountStatus.ATIVO
            // Se não for ATIVO (e não for INATIVO, já tratado acima), então é PENDENTE
            if (!user.isApproved()) { 
                System.out.println("Usuário pendente de aprovação (check-approval): " + username);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                       .body(new MessageResponse("PENDING_APPROVAL"));
            } 
            
            // Se chegou aqui, usuário está ATIVO (isApproved() é true)
            if (user.getRole() == null) {
                System.out.println("Usuário aprovado mas sem papel atribuído (check-approval): " + username);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                       .body(new MessageResponse("ROLE_MISSING"));
            }
            
            // Adicionalmente, mesmo que ATIVO, o campo 'enabled' pode estar false por alguma razão administrativa.
            // O CustomAuthenticationProvider já verifica user.isEnabled() para login.
            // Para check-approval, se status é ATIVO mas enabled é false, podemos considerar como desabilitado.
            if (!user.isEnabled()) {
                 System.out.println("Usuário ATIVO mas desabilitado (enabled=false) (check-approval): " + username);
                 return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new MessageResponse("ACCOUNT_DISABLED"));
            }
            
            System.out.println("Usuário aprovado e ativo (check-approval): " + username);
            return ResponseEntity.ok(new MessageResponse("APPROVED"));

        } catch (Exception e) {
            System.err.println("Erro ao verificar status de aprovação para " + username + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                   .body(new MessageResponse("Erro ao verificar status: " + e.getMessage()));
        }
    }
}