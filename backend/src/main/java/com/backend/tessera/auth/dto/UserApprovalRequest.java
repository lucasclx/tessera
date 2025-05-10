package com.backend.tessera.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
// Adicione quaisquer outras anotações de validação necessárias

public class UserApprovalRequest {

    @NotNull(message = "O status de aprovação não pode ser nulo")
    private boolean approved;

    // O papel pode ser opcional ao rejeitar, mas obrigatório ao aprovar.
    // A lógica de validação pode estar no serviço.
    private String role; 

    private String adminComments;

    // Construtores
    public UserApprovalRequest() {
    }

    public UserApprovalRequest(boolean approved, String role, String adminComments) {
        this.approved = approved;
        this.role = role;
        this.adminComments = adminComments;
    }

    // Getters e Setters
    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAdminComments() {
        return adminComments;
    }

    public void setAdminComments(String adminComments) {
        this.adminComments = adminComments;
    }
}
