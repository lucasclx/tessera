// Arquivo: backend/src/main/java/com/backend/tessera/dto/EmailRequest.java
package com.backend.tessera.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class EmailRequest {
    @NotBlank(message = "O e-mail não pode estar em branco.")
    @Email(message = "Formato de e-mail inválido.")
    private String email;

    // Getter
    public String getEmail() {
        return email;
    }

    // Setter
    public void setEmail(String email) {
        this.email = email;
    }
}