package com.backend.tessera.auth.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor; // Adicionado
import lombok.AllArgsConstructor; // Adicionado (opcional, mas bom para construtores completos)
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
@NoArgsConstructor // Garante que um construtor sem argumentos seja gerado pelo Lombok
@AllArgsConstructor // Opcional: gera um construtor com todos os argumentos
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
        if (this.status == AccountStatus.ATIVO && this.approvalDate == null) { // Garante approvalDate se ATIVO na criação
            this.approvalDate = LocalDateTime.now();
        }
    }

    // Construtor customizado (pode ser usado ou substituído pelo @AllArgsConstructor se todos os campos estiverem incluídos)
    // Se você mantiver este, certifique-se de que ele inicialize todos os campos que @AllArgsConstructor inicializaria
    // ou remova-o se @AllArgsConstructor for suficiente.
    public User(String nome, String username, String email, String password, String institution, Role role, AccountStatus status, boolean enabled) {
        this.nome = nome;
        this.username = username;
        this.email = email;
        this.password = password; // Assume que já está encodado
        this.institution = institution;
        this.role = role;
        this.status = status;
        this.enabled = enabled;
        // this.createdAt = LocalDateTime.now(); // @PrePersist cuidará disso
        // if (status == AccountStatus.ATIVO) { // @PrePersist cuidará disso
        //     this.approvalDate = LocalDateTime.now();
        // }
        // Inicialize outros campos como accountNonExpired, etc., se necessário aqui.
        this.accountNonExpired = true;
        this.accountNonLocked = true;
        this.credentialsNonExpired = true;
    }


    // Métodos UserDetails
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role != null && status == AccountStatus.ATIVO && enabled) {
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
        return this.accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return this.status == AccountStatus.ATIVO && this.enabled;
    }

    public boolean isEnabledField() {
        return this.enabled;
    }
}
