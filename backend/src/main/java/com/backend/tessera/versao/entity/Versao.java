package com.backend.tessera.versao.entity;

import com.backend.tessera.monografia.entity.Monografia;
import com.backend.tessera.auth.entity.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor; // Adicionado se quiser construtor com todos os campos
import java.time.LocalDateTime;

@Entity
@Table(name = "versoes")
@Data
@NoArgsConstructor
@AllArgsConstructor // Adicionado para conveniência, se aplicável
public class Versao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "monografia_id", nullable = false)
    private Monografia monografia;

    @Column(nullable = false)
    private String numeroVersao; // Ex: "1.0", "1.1"

    @Column(nullable = false, length = 64) // SHA-256 hash é 64 caracteres hex
    private String hashArquivo;

    @Column(nullable = false)
    private String nomeArquivo; // Nome do arquivo no sistema de arquivos

    @Column(columnDefinition = "TEXT")
    private String mensagemCommit;

    private String tag; // Opcional, ex: "Entrega Parcial", "Final"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criado_por_id", nullable = false)
    private User criadoPor; // Usuário que criou esta versão

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(nullable = false)
    private String caminhoArquivo; // Caminho relativo para o arquivo no storage

    private Long tamanhoArquivo; // Tamanho do arquivo em bytes


    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
    }
}