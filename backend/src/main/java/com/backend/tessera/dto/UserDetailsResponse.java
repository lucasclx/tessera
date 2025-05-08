package com.backend.tessera.dto;

import java.time.LocalDateTime;

public class UserDetailsResponse {
    private Long id;
    private String nome;
    private String username;
    private String email;
    private String institution;
    private String role;
    private String requestedRole;
    private boolean approved;
    private LocalDateTime approvalDate;
    private String adminComments;
    private boolean enabled;
    private LocalDateTime createdAt;

    public UserDetailsResponse() {
    }

    public UserDetailsResponse(Long id, String nome, String username, String email, 
                               String institution, String role, String requestedRole, 
                               boolean approved, LocalDateTime approvalDate, 
                               String adminComments, boolean enabled, LocalDateTime createdAt) {
        this.id = id;
        this.nome = nome;
        this.username = username;
        this.email = email;
        this.institution = institution;
        this.role = role;
        this.requestedRole = requestedRole;
        this.approved = approved;
        this.approvalDate = approvalDate;
        this.adminComments = adminComments;
        this.enabled = enabled;
        this.createdAt = createdAt;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getInstitution() { return institution; }
    public void setInstitution(String institution) { this.institution = institution; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getRequestedRole() { return requestedRole; }
    public void setRequestedRole(String requestedRole) { this.requestedRole = requestedRole; }

    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }

    public LocalDateTime getApprovalDate() { return approvalDate; }
    public void setApprovalDate(LocalDateTime approvalDate) { this.approvalDate = approvalDate; }

    public String getAdminComments() { return adminComments; }
    public void setAdminComments(String adminComments) { this.adminComments = adminComments; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
