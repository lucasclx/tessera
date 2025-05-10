package com.backend.tessera.auth.dto;

import jakarta.validation.constraints.NotBlank; // Adicionado para exemplo

public class AuthRequest {
    @NotBlank(message = "Username não pode ser vazio")
    private String username;

    @NotBlank(message = "Password não pode ser vazio")
    private String password;

    // Getters e Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}