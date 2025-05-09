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
    private boolean enabled = false; // Alterado para false por padrão

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
        this.status = AccountStatus.ATIVO; // Usuários iniciais já vêm ativos
        this.createdAt = LocalDateTime.now();
        this.enabled = true; // Usuários iniciais já vêm habilitados
    }

    // Construtor completo para RegisterController
    public User(String nome, String username, String email, String password, String institution, Role role) {
        this.nome = nome;
        this.username = username;
        this.email = email;
        this.password = password;
        this.institution = institution;
        this.role = role;
        this.status = AccountStatus.PENDENTE; // Novos usuários começam pendentes
        this.createdAt = LocalDateTime.now();
        this.enabled = false; // Novos usuários começam desabilitados
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
        if (status == AccountStatus.ATIVO && enabled) {
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
        // A conta está bloqueada se estiver pendente
        return this.accountNonLocked && (this.status != AccountStatus.PENDENTE);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        // A conta está habilitada se estiver ativa E o campo enabled for true
        return this.enabled && (this.status == AccountStatus.ATIVO);
    }
}