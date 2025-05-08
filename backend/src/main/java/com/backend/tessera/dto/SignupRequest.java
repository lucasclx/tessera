package com.backend.tessera.dto;

import jakarta.validation.constraints.*;
import java.util.Set;

public class SignupRequest {
    @NotBlank @Size(min = 3, max = 50)
    private String nome;

    @NotBlank @Size(min = 3, max = 20)
    private String username;

    @NotBlank @Size(max = 50) @Email
    private String email;

    @NotBlank @Size(min = 6, max = 40)
    private String password;

    @NotBlank @Size(min = 3, max = 20) // Ajustado para instituição, pode ser maior se necessário
    private String institution;

    // Removido: private String cpf;

    @NotEmpty // Garante que o conjunto não seja nulo nem vazio
    private Set<String> role;

    // Getters e Setters
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getInstitution() { return institution; }
    public void setInstitution(String institution) { this.institution = institution; }
    // Removido: Getter e Setter para cpf
    public Set<String> getRole() { return role; }
    public void setRole(Set<String> role) { this.role = role; }
}