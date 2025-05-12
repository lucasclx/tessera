package com.backend.tessera.comentario.entity;

import com.backend.tessera.auth.entity.User;
import com.backend.tessera.versao.entity.Versao;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data; // Adicionado
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "comentarios")
@Data // Garante getters, setters, toString, equals, hashCode
@NoArgsConstructor
@AllArgsConstructor
public class Comentario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "versao_id", nullable = false)
    private Versao versao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id", nullable = false)
    private User autor;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String comentario;

    @Column(updatable = false)
    private LocalDateTime dataCriacao;

    private String posicaoTexto;


    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
    }
}