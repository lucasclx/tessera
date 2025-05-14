package com.backend.tessera.dto;

public class AuthRequest {
    private String username;
    private String password;

    // Adicionar este construtor
    public AuthRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Construtor padrão (pode ser mantido se necessário)
    public AuthRequest() {
    }

    // Getters e Setters existentes
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}