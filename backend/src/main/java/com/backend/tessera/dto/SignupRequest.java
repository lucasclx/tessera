package com.backend.tessera.dto;

import com.backend.tessera.validation.StrongPassword;
import jakarta.validation.constraints.*;
import java.util.Set;

public class SignupRequest {
    @NotBlank @Size(min = 3, max = 50)
    private String nome;

    @NotBlank @Size(min = 3, max = 20)
    private String username;

    @NotBlank @Size(max = 50) @Email
    private String email;

    @NotBlank
    @StrongPassword
    private String password;

    @NotBlank @Size(min = 3, max = 100)
    private String institution;

    @NotEmpty
    private Set<String> role;

    // Construtores
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
    public String getNome() { 
        return nome; 
    }
    
    public void setNome(String nome) { 
        this.nome = nome; 
    }
    
    public String getUsername() { 
        return username; 
    }
    
    public void setUsername(String username) { 
        this.username = username; 
    }
    
    public String getEmail() { 
        return email; 
    }
    
    public void setEmail(String email) { 
        this.email = email; 
    }
    
    public String getPassword() { 
        return password; 
    }
    
    public void setPassword(String password) { 
        this.password = password; 
    }
    
    public String getInstitution() { 
        return institution; 
    }
    
    public void setInstitution(String institution) { 
        this.institution = institution; 
    }
    
    public Set<String> getRole() { 
        return role; 
    }
    
    public void setRole(Set<String> role) { 
        this.role = role; 
    }
}