// Arquivo: src/main/java/com/backend/tessera/model/User.java
package com.backend.tessera.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
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

    // Novos campos para o sistema de aprovação
    @Enumerated(EnumType.STRING)
    private Role requestedRole; // Papel solicitado pelo usuário

    private boolean approved = false; // Status de aprovação

    private LocalDateTime approvalDate; // Data de aprovação/rejeição

    @Column(length = 500)
    private String adminComments; // Comentários do administrador

    // Campos para status da conta
    private boolean accountNonExpired = true;
    private boolean accountNonLocked = true;
    private boolean credentialsNonExpired = true;
    private boolean enabled = true;

    // Campo para rastrear data de criação
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // Inicializa a data de criação antes de persistir
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Construtor usado pelo DataInitializer
    public User(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.approved = true; // Usuários iniciais já vêm aprovados
        this.createdAt = LocalDateTime.now();
    }

    // Construtor completo para RegisterController
    public User(String nome, String username, String email, String password, String institution, Role role) {
        this.nome = nome;
        this.username = username;
        this.email = email;
        this.password = password;
        this.institution = institution;
        this.role = role;
        this.createdAt = LocalDateTime.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        // Uma conta não aprovada é considerada bloqueada
        return this.accountNonLocked && this.approved;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
}