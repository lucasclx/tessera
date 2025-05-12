// src/main/java/com/backend/tessera/versao/entity/Versao.java
package com.backend.tessera.versao.entity;

import com.backend.tessera.monografia.entity.Monografia; // Se houver relação bidirecional
import com.backend.tessera.auth.entity.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "versoes")
@Data
@NoArgsConstructor
public class Versao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int numeroVersao;

    @Column(length = 500)
    private String descricaoAlteracoes;

    @Column(nullable = false)
    private String caminhoArquivo; // Ou armazene o arquivo de outra forma

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "monografia_id", nullable = false)
    private Monografia monografia;

    @ManyToOne
    @JoinColumn(name = "submetido_por_id", nullable = false)
    private User submetidoPor;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataSubmissao;

    // Exemplo de campo adicional
    private String feedbackOrientador;

    @PrePersist
    protected void onCreate() {
        dataSubmissao = LocalDateTime.now();
    }

    // Construtor, getters, setters (Lombok @Data cuida disso)
    // Adicione quaisquer outros campos e lógica necessários
}