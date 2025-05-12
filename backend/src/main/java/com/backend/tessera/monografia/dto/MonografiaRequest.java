package com.backend.tessera.monografia.dto;

import com.backend.tessera.auth.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonografiaRequest {
    @NotBlank(message = "O título é obrigatório")
    private String titulo;
    
    private String descricao;
    
    private User autorPrincipal;
    
    private List<User> coAutores = new ArrayList<>();
    
    @NotNull(message = "O orientador principal é obrigatório")
    private User orientadorPrincipal;
    
    private List<User> coOrientadores = new ArrayList<>();
}