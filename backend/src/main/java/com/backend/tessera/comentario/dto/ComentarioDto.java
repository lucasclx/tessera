package com.backend.tessera.comentario.dto; // Pacote CORRETO

import com.backend.tessera.auth.entity.User; // Ou um UserDto se preferir não expor a entidade
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComentarioDto { // Nome da classe CORRETO

    private Long id;
    private String comentario;
    private User autor; // Pode ser UserDto para evitar expor entidade completa
    private Long versaoId;
    private LocalDateTime dataCriacao;
    private String posicaoTexto; // Opcional: linha, seleção de texto, etc.
}