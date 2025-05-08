package com.backend.tessera.controller;

import com.backend.tessera.dto.AuthRequest;
import com.backend.tessera.dto.AuthResponse;
import com.backend.tessera.security.JwtUtil;
import com.backend.tessera.service.UserDetailsServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import jakarta.validation.Valid;

import java.util.Collection;
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

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody AuthRequest authRequest) {
        try {
            System.out.println("Tentando autenticar usuário: " + authRequest.getUsername());
            
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );
            
            System.out.println("Autenticação bem-sucedida para: " + authRequest.getUsername());
            
            final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getUsername());
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
        } catch (Exception e) {
            System.out.println("Erro inesperado na autenticação: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}