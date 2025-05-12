package com.backend.tessera.comentario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NovoComentarioRequest {
    @NotNull(message = "ID da versão é obrigatório")
    private Long versaoId;

    @NotBlank(message = "O comentário não pode ser vazio")
    private String comentario;

    private String posicaoTexto; // Opcional
}