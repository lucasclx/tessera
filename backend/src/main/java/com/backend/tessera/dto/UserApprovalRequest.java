// UserApprovalRequest.java
package com.backend.tessera.dto;

import jakarta.validation.constraints.NotNull;

public class UserApprovalRequest {
    @NotNull
    private boolean approved;
    
    private String role;
    
    private String adminComments;

    public UserApprovalRequest() {
    }

    public UserApprovalRequest(boolean approved, String role, String adminComments) {
        this.approved = approved;
        this.role = role;
        this.adminComments = adminComments;
    }

    // Getters e Setters
    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getAdminComments() { return adminComments; }
    public void setAdminComments(String adminComments) { this.adminComments = adminComments; }
}