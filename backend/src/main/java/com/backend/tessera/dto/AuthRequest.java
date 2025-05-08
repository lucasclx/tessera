package com.backend.tessera.dto;

// Importe jakarta.validation.constraints se for usar validações aqui
// import jakarta.validation.constraints.NotBlank;

public class AuthRequest {
    // @NotBlank // Exemplo de validação
    private String username;
    // @NotBlank // Exemplo de validação
    private String password;

    // Getters e Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}