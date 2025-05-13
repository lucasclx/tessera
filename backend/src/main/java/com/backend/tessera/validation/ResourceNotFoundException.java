package com.backend.tessera.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public static ResourceNotFoundException forUser(Long id) {
        return new ResourceNotFoundException("Usuário não encontrado com ID: " + id);
    }
    
    public static ResourceNotFoundException forUser(String username) {
        return new ResourceNotFoundException("Usuário não encontrado: " + username);
    }
}