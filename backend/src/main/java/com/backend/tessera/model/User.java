package com.backend.tessera.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
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
@Data
@NoArgsConstructor
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
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status = AccountStatus.PENDENTE;

    // Campo para a data de aprovação
    private LocalDateTime approvalDate;

    @Column(length = 500)
    private String adminComments;

    // Campos para status da conta
    private boolean accountNonExpired = true;
    private boolean accountNonLocked = true;
    private boolean credentialsNonExpired = true;

    // Campo para rastrear data de criação
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // Atributos para verificação de email
    private boolean emailVerified = false;
    private LocalDateTime emailVerifiedAt;

    // Inicializa a data de criação antes de persistir
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Construtores
    public User(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.status = AccountStatus.ATIVO; // Usuários iniciais já vêm ativos
        this.createdAt = LocalDateTime.now();
    }

    public User(String nome, String username, String email, String password, String institution, Role role) {
        this.nome = nome;
        this.username = username;
        this.email = email;
        this.password = password;
        this.institution = institution;
        this.role = role;
        this.status = AccountStatus.PENDENTE; // Novos usuários começam pendentes
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Verifica se o usuário pode fazer login
     * Um usuário pode fazer login somente se o status for ATIVO
     */
    public boolean canLogin() {
        return this.status == AccountStatus.ATIVO;
    }

    /**
     * Verifica se a conta foi aprovada
     */
    public boolean isApproved() {
        return this.status == AccountStatus.ATIVO;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Só retorna autoridades se o status for ATIVO
        if (status == AccountStatus.ATIVO) {
            return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
        }
        return Collections.emptyList();
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        // A conta está bloqueada se estiver pendente ou rejeitada
        return this.accountNonLocked && 
              (this.status != AccountStatus.PENDENTE && this.status != AccountStatus.REJEITADO);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        // A conta está habilitada somente se estiver ATIVA
        return this.status == AccountStatus.ATIVO;
    }
    
    // Para compatibilidade com código existente que chama setEnabled
    public void setEnabled(boolean enabled) {
        if (enabled) {
            // Se estiver ativando a conta, apenas mude para ATIVO se não for PENDENTE ou REJEITADO
            if (this.status == AccountStatus.INATIVO) {
                this.status = AccountStatus.ATIVO;
            }
        } else {
            // Se estiver desativando a conta, apenas mude para INATIVO se for ATIVO
            if (this.status == AccountStatus.ATIVO) {
                this.status = AccountStatus.INATIVO;
            }
        }
    }
    
    // Getters e Setters explícitos para garantir que o Lombok os gere corretamente
    
    @Override
    public String getUsername() {
        return username;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public LocalDateTime getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(LocalDateTime approvalDate) {
        this.approvalDate = approvalDate;
    }

    public String getAdminComments() {
        return adminComments;
    }

    public void setAdminComments(String adminComments) {
        this.adminComments = adminComments;
    }

    public void setAccountNonExpired(boolean accountNonExpired) {
        this.accountNonExpired = accountNonExpired;
    }

    public void setAccountNonLocked(boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    public void setCredentialsNonExpired(boolean credentialsNonExpired) {
        this.credentialsNonExpired = credentialsNonExpired;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public LocalDateTime getEmailVerifiedAt() {
        return emailVerifiedAt;
    }

    public void setEmailVerifiedAt(LocalDateTime emailVerifiedAt) {
        this.emailVerifiedAt = emailVerifiedAt;
    }
    
    // Métodos equals, hashCode e toString gerados automaticamente pelo Lombok
}