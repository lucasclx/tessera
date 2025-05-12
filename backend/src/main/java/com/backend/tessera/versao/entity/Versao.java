package com.backend.tessera.versao.entity;

import com.backend.tessera.auth.entity.User;
import com.backend.tessera.monografia.entity.Monografia;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "versoes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Versao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "monografia_id", nullable = false)
    private Monografia monografia;

    @Column(nullable = false)
    private String numeroVersao; // Ex: "1.0", "1.1"

    @Column(nullable = false, length = 64)
    private String hashArquivo;

    @Column(nullable = false)
    private String nomeArquivo;

    @Column(columnDefinition = "TEXT")
    private String mensagemCommit;

    private String tag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criado_por_id", nullable = false)
    private User criadoPor;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(nullable = false)
    private String caminhoArquivo;

    private Long tamanhoArquivo;

    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
    }
}