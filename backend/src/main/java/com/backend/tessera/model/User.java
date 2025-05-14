// Arquivo: backend/src/main/java/com/backend/tessera/model/User.java
package com.backend.tessera.model;

import jakarta.persistence.*;
import lombok.Data; // Certifique-se que esta anotação está presente e importada
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
@Data // ESSENCIAL: Gera getters, setters, toString, equals, hashCode
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

    private LocalDateTime approvalDate;

    @Column(length = 500)
    private String adminComments;

    private boolean accountNonExpired = true;
    private boolean accountNonLocked = true;
    private boolean credentialsNonExpired = true;
    private boolean enabled = false; 

    @Column(updatable = false)
    private LocalDateTime createdAt;

    // Campos para confirmação de e-mail
    @Column(name = "email_verified") // Mantendo o nome da coluna explícito
    private boolean emailVerified = false; // Lombok irá gerar isEmailVerified()

    @Column(name = "email_verification_token")
    private String emailVerificationToken; // Lombok irá gerar getEmailVerificationToken() e setEmailVerificationToken()

    @Column(name = "email_verification_token_expiry_date")
    private LocalDateTime emailVerificationTokenExpiryDate; // Lombok irá gerar getters/setters

    // Campos para recuperação de senha
    @Column(name = "password_reset_token")
    private String passwordResetToken; // Lombok irá gerar getters/setters

    @Column(name = "password_reset_token_expiry_date")
    private LocalDateTime passwordResetTokenExpiryDate; // Lombok irá gerar getters/setters

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Construtor para DataInitializer
    public User(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.status = AccountStatus.ATIVO; 
        this.createdAt = LocalDateTime.now();
        this.enabled = true; 
        this.emailVerified = true; // E-mails dos usuários iniciais são considerados verificados
    }

    // Construtor para RegisterController
    public User(String nome, String username, String email, String password, String institution, Role role) {
        this.nome = nome;
        this.username = username;
        this.email = email;
        this.password = password;
        this.institution = institution;
        this.role = role;
        this.status = AccountStatus.PENDENTE; 
        this.createdAt = LocalDateTime.now();
        this.enabled = false; 
        this.emailVerified = false; // Novos usuários precisam verificar
    }

    // O Lombok @Data já gera isApproved() se o campo fosse 'approved'.
    // Como o campo é 'status', mantemos este método customizado.
    public boolean isApproved() {
        return this.status == AccountStatus.ATIVO;
    }
    
    // O Lombok @Data gera isEnabled(), mas sobrescrevemos para lógica customizada.
    @Override
    public boolean isEnabled() {
        return this.enabled && (this.status == AccountStatus.ATIVO);
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (status == AccountStatus.ATIVO && isEnabled()) { // Usar o isEnabled() customizado
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
        return this.accountNonLocked && (this.status != AccountStatus.PENDENTE);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.credentialsNonExpired;
    }
    // O método isEmailVerified() será gerado pelo Lombok para o campo 'emailVerified'
}