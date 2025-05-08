package com.backend.tessera.dto; // Ou com.backend.tessera.payload.response

public class MessageResponse {
    private String message;

    public MessageResponse(String message) {
        this.message = message;
    }

    // Getter e Setter
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}