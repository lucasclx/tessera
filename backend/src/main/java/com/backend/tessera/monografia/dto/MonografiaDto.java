package com.backend.tessera.monografia.dto;

import com.backend.tessera.auth.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonografiaDto {
    private Long id;
    private String titulo;
    private String descricao;
    private User autorPrincipal;
    private List<User> coAutores = new ArrayList<>();
    private User orientadorPrincipal;
    private List<User> coOrientadores = new ArrayList<>();
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;
}