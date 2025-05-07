package com.backend.tessera.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection; // Alterado para Collection<String> para roles

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String username;
    private Collection<String> roles; // Para enviar as roles como strings simples
}