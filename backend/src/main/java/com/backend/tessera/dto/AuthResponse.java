// Arquivo: backend/src/main/java/com/backend/tessera/dto/AuthResponse.java
package com.backend.tessera.dto;

import java.util.Collection;

public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private Long id; // Adicionado
    private String username;
    private String nome; // Adicionado
    private Collection<String> roles;
    private boolean emailVerified; // Adicionado

    public AuthResponse() {
    }

    public AuthResponse(String accessToken, String username, Collection<String> roles, boolean emailVerified, Long id, String nome) {
        this.token = accessToken;
        this.username = username;
        this.roles = roles;
        this.emailVerified = emailVerified;
        this.id = id;
        this.nome = nome;
    }

    // Getters e Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getId() { // Adicionado
        return id;
    }

    public void setId(Long id) { // Adicionado
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNome() { // Adicionado
        return nome;
    }

    public void setNome(String nome) { // Adicionado
        this.nome = nome;
    }

    public Collection<String> getRoles() {
        return roles;
    }

    public void setRoles(Collection<String> roles) {
        this.roles = roles;
    }

    public boolean isEmailVerified() { // Adicionado
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) { // Adicionado
        this.emailVerified = emailVerified;
    }
}