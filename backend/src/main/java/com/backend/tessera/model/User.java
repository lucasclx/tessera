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

    @Column(name = "enabled", nullable = false) // Coluna para o banco de dados
    private boolean enabled;

    // Campo para a data de aprovação
    private LocalDateTime approvalDate;

    @Column(length = 500)
    private String adminComments;

    // Campos para status da conta (mantidos para UserDetails, mas 'enabled' é o principal para o estado da conta)
    private boolean accountNonExpired = true;
    private boolean accountNonLocked = true;
    private boolean credentialsNonExpired = true;

    // Campo para rastrear data de criação
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // Atributos para verificação de email
    private boolean emailVerified = false;
    private LocalDateTime emailVerifiedAt;

    // Inicializa a data de criação e o estado 'enabled' antes de persistir
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        // Garante que 'enabled' seja definido com base no 'status' inicial
        if (this.status == null) {
            this.status = AccountStatus.PENDENTE; // Define um padrão se status for nulo
        }
        this.enabled = (this.status == AccountStatus.ATIVO);
    }

    // Construtores
    public User(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.setStatus(AccountStatus.ATIVO); // Define status e enabled
        this.createdAt = LocalDateTime.now();
    }

    public User(String nome, String username, String email, String password, String institution, Role role) {
        this.nome = nome;
        this.username = username;
        this.email = email;
        this.password = password;
        this.institution = institution;
        this.role = role;
        this.setStatus(AccountStatus.PENDENTE); // Define status e enabled
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Verifica se o usuário pode fazer login
     * Um usuário pode fazer login somente se o status for ATIVO (e, por consequência, enabled for true)
     */
    public boolean canLogin() {
        return this.status == AccountStatus.ATIVO && this.enabled;
    }

    /**
     * Verifica se a conta foi aprovada
     */
    public boolean isApproved() {
        return this.status == AccountStatus.ATIVO;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Só retorna autoridades se o status for ATIVO e a conta estiver habilitada
        if (this.status == AccountStatus.ATIVO && this.enabled) {
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
        // A conta está bloqueada se não estiver 'enabled' (que reflete status PENDENTE, REJEITADO, INATIVO)
        // ou se accountNonLocked for explicitamente false.
        return this.accountNonLocked && this.enabled;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.credentialsNonExpired;
    }

    // Implementação do método isEnabled da interface UserDetails
    @Override
    public boolean isEnabled() {
        return this.enabled; // Retorna o valor do campo persistido 'enabled'
    }

    // Setter para 'enabled' que também sincroniza 'status'
    // Este método é crucial se 'enabled' for alterado diretamente.
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            // Se está ativando, e o status era INATIVO, PENDENTE (após aprovação), ou REJEITADO (após reconsideração)
            // muda para ATIVO. A lógica de aprovação deve ser tratada separadamente.
            // Aqui, focamos em sincronizar 'status' se 'enabled' muda.
            if (this.status == AccountStatus.INATIVO || this.status == AccountStatus.PENDENTE || this.status == AccountStatus.REJEITADO) {
                // Se estamos ativando, o status deve se tornar ATIVO.
                // Casos PENDENTE/REJEITADO devem ser aprovados antes de serem ATIVO,
                // mas se setEnabled(true) for chamado, assumimos que a intenção é ATIVAR.
                this.status = AccountStatus.ATIVO;
                if (this.approvalDate == null && this.status == AccountStatus.ATIVO) {
                    this.approvalDate = LocalDateTime.now(); // Define data de aprovação se estiver ativando
                }
            }
        } else {
            // Se está desativando, e o status era ATIVO, torna INATIVO
            if (this.status == AccountStatus.ATIVO) {
                this.status = AccountStatus.INATIVO;
            }
            // Se PENDENTE ou REJEITADO, já está "desabilitado" (enabled=false).
        }
    }

    // Setter para 'status' que também sincroniza 'enabled'
    public void setStatus(AccountStatus status) {
        this.status = status;
        this.enabled = (status == AccountStatus.ATIVO);
        if (status == AccountStatus.ATIVO && this.approvalDate == null) {
            this.approvalDate = LocalDateTime.now();
        } else if (status != AccountStatus.ATIVO) {
            // Se não está ATIVO, não deve ter data de aprovação (ou pode ser mantida se já foi aprovado antes)
            // Para simplificar, não limpamos approvalDate aqui, pois um admin pode desativar uma conta aprovada.
        }
    }
    
    // Getters e Setters explícitos (Lombok @Data já os provê, mas para clareza em relação às modificações)

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

    // public void setStatus(AccountStatus status) { // Setter já modificado acima

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
    
    // O Lombok @Data gera equals, hashCode, toString.
}