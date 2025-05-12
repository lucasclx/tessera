package com.backend.tessera.auth.entity;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = "username"),
    @UniqueConstraint(columnNames = "email")
})
@NoArgsConstructor
// @AllArgsConstructor // Lombok AllArgsConstructor será baseado nos campos atuais
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String institution;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status = AccountStatus.PENDENTE;

    private LocalDateTime approvalDate;

    @Column(length = 500)
    private String adminComments;

    private boolean accountNonExpired = true;
    private boolean accountNonLocked = true;
    private boolean credentialsNonExpired = true;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean enabled = false;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (this.status == AccountStatus.ATIVO && this.approvalDate == null) {
            this.approvalDate = LocalDateTime.now();
        }
    }

    // Construtor completo explícito (similar ao que o @AllArgsConstructor faria)
    public User(String nome, String username, String email, String password, String institution, Role role, AccountStatus status, boolean enabled) {
        this.nome = nome;
        this.username = username;
        this.email = email;
        this.password = password;
        this.institution = institution;
        this.role = role;
        this.status = status;
        this.enabled = enabled;
        this.accountNonExpired = true;
        this.accountNonLocked = true; // Assumindo que inicialmente não está bloqueada, a menos que PENDENTE
        this.credentialsNonExpired = true;
        // createdAt e approvalDate são tratados pelo @PrePersist
    }

    // Getters e Setters Explícitos
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    // username é o método de UserDetails, não precisa de getUsername() explícito se já tiver
    // public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    // password é o método de UserDetails
    // public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getInstitution() { return institution; }
    public void setInstitution(String institution) { this.institution = institution; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }

    public LocalDateTime getApprovalDate() { return approvalDate; }
    public void setApprovalDate(LocalDateTime approvalDate) { this.approvalDate = approvalDate; }

    public String getAdminComments() { return adminComments; }
    public void setAdminComments(String adminComments) { this.adminComments = adminComments; }

    public boolean isAccountNonExpiredInternal() { return accountNonExpired; } // Renomeado para evitar conflito com UserDetails se houver
    public void setAccountNonExpired(boolean accountNonExpired) { this.accountNonExpired = accountNonExpired; }

    public boolean isAccountNonLockedInternal() { return accountNonLocked; } // Renomeado
    public void setAccountNonLocked(boolean accountNonLocked) { this.accountNonLocked = accountNonLocked; }

    public boolean isCredentialsNonExpiredInternal() { return credentialsNonExpired; } // Renomeado
    public void setCredentialsNonExpired(boolean credentialsNonExpired) { this.credentialsNonExpired = credentialsNonExpired; }

    // enabled é o método de UserDetails
    // public boolean isEnabled() { return status == AccountStatus.ATIVO && enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public boolean isEnabledField() { return this.enabled; } // Para acesso direto ao campo

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }


    // Métodos UserDetails
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role != null && status == AccountStatus.ATIVO && this.enabled) {
            return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
        }
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        // Uma conta PENDENTE é efetivamente "bloqueada" para login normal
        // A lógica específica de lançamento de LockedException é feita no UserDetailsServiceImpl ou AuthenticationProvider
        return this.accountNonLocked && (this.status != AccountStatus.PENDENTE);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        // Este método é crucial para o Spring Security.
        // Define se a conta é considerada habilitada.
        return this.status == AccountStatus.ATIVO && this.enabled;
    }
}