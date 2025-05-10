package com.backend.tessera.auth.dto;

import jakarta.validation.constraints.*;
import java.util.Set;

public class SignupRequest {
    @NotBlank(message = "Nome não pode ser vazio")
    @Size(min = 3, max = 50, message = "Nome deve ter entre 3 e 50 caracteres")
    private String nome;

    @NotBlank(message = "Username não pode ser vazio")
    @Size(min = 3, max = 20, message = "Username deve ter entre 3 e 20 caracteres")
    private String username;

    @NotBlank(message = "Email não pode ser vazio")
    @Size(max = 50, message = "Email deve ter no máximo 50 caracteres")
    @Email(message = "Email deve ser válido")
    private String email;

    @NotBlank(message = "Password não pode ser vazio")
    @Size(min = 6, max = 40, message = "Password deve ter entre 6 e 40 caracteres")
    private String password;

    @NotBlank(message = "Instituição não pode ser vazia")
    @Size(min = 3, max = 100, message = "Instituição deve ter entre 3 e 100 caracteres")
    private String institution;

    @NotEmpty(message = "Pelo menos um perfil (Role) deve ser especificado")
    private Set<String> role;

    public SignupRequest() {
    }

    public SignupRequest(String nome, String username, String email, String password, String institution, Set<String> role) {
        this.nome = nome;
        this.username = username;
        this.email = email;
        this.password = password;
        this.institution = institution;
        this.role = role;
    }

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
    public Set<String> getRole() { return role; }
    public void setRole(Set<String> role) { this.role = role; }
}