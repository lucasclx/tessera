// Arquivo: backend/src/main/java/com/backend/tessera/dto/PasswordResetRequest.java
package com.backend.tessera.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PasswordResetRequest {
    @NotBlank(message = "O token não pode estar em branco.")
    private String token;

    @NotBlank(message = "A nova senha não pode estar em branco.")
    @Size(min = 6, max = 40, message = "A nova senha deve ter entre 6 e 40 caracteres.")
    private String newPassword;

    // Getters
    public String getToken() {
        return token;
    }

    public String getNewPassword() {
        return newPassword;
    }

    // Setters
    public void setToken(String token) {
        this.token = token;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}