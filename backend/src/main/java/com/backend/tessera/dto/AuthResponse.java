package com.backend.tessera.dto;

import java.util.Collection;

public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private String username;
    private Collection<String> roles;

    // Construtores
    public AuthResponse() {
    }

    public AuthResponse(String accessToken, String username, Collection<String> roles) {
        this.token = accessToken;
        this.username = username;
        this.roles = roles;
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
    
    public String getUsername() { 
        return username; 
    }
    
    public void setUsername(String username) { 
        this.username = username; 
    }
    
    public Collection<String> getRoles() { 
        return roles; 
    }
    
    public void setRoles(Collection<String> roles) { 
        this.roles = roles; 
    }
}