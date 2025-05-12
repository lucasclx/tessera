package com.backend.tessera.versao.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NovaVersaoRequest {
    @NotNull(message = "ID da monografia é obrigatório")
    private Long monografiaId;
    
    @NotBlank(message = "Conteúdo é obrigatório")
    private String conteudo;
    
    @NotBlank(message = "Mensagem de commit é obrigatória")
    private String mensagemCommit;
    
    private String tag;
}