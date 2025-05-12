package com.backend.tessera.comentario.dto; // Pacote CORRETO

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NovoComentarioRequest { // Nome da classe CORRETO

    @NotNull(message = "ID da versão é obrigatório")
    private Long versaoId;

    @NotBlank(message = "O comentário não pode ser vazio")
    private String comentario;

    private String posicaoTexto; // Opcional
}