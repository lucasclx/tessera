package com.backend.tessera.monografia.entity;

import com.backend.tessera.auth.entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "versoes")
@Data
@NoArgsConstructor
public class Versao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "monografia_id", nullable = false)
    private Monografia monografia;

    @Column(nullable = false)
    private String numeroVersao;

    @Column(nullable = false)
    private String hashArquivo;

    @Column(nullable = false)
    private String nomeArquivo;

    @Column(length = 2000)
    private String mensagemCommit;

    @Column(length = 100)
    private String tag;

    @ManyToOne
    @JoinColumn(name = "criado_por_id", nullable = false)
    private User criadoPor;

    @OneToMany(mappedBy = "versao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comentario> comentarios = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    // Caminho relativo do arquivo no sistema
    @Column(nullable = false)
    private String caminhoArquivo;

    // Tamanho do arquivo em bytes
    @Column(nullable = false)
    private Long tamanhoArquivo;

    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
    }
}