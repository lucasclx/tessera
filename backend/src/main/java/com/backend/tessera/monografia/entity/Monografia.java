// src/main/java/com/backend/tessera/monografia/entity/Monografia.java

package com.backend.tessera.monografia.entity;

import com.backend.tessera.auth.entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "monografias")
@Data
@NoArgsConstructor
public class Monografia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(length = 1000)
    private String descricao;

    @ManyToOne
    @JoinColumn(name = "autor_principal_id", nullable = false)
    private User autorPrincipal;

    @ManyToMany
    @JoinTable(
        name = "monografia_co_autores",
        joinColumns = @JoinColumn(name = "monografia_id"),
        inverseJoinColumns = @JoinColumn(name = "co_autor_id")
    )
    private List<User> coAutores = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "orientador_principal_id", nullable = false)
    private User orientadorPrincipal;

    @ManyToMany
    @JoinTable(
        name = "monografia_co_orientadores",
        joinColumns = @JoinColumn(name = "monografia_id"),
        inverseJoinColumns = @JoinColumn(name = "co_orientador_id")
    )
    private List<User> coOrientadores = new ArrayList<>();

    @OneToMany(mappedBy = "monografia", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Versao> versoes = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column
    private LocalDateTime dataAtualizacao;

    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        dataAtualizacao = LocalDateTime.now();
    }
}