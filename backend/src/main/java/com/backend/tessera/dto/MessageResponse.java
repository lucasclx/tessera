package com.backend.tessera.dto;

public class MessageResponse {
    private String message;

    // Construtores
    public MessageResponse() {
    }
    
    public MessageResponse(String message) {
        this.message = message;
    }

    // Getter e Setter
    public String getMessage() { 
        return message; 
    }
    
    public void setMessage(String message) { 
        this.message = message; 
    }
}