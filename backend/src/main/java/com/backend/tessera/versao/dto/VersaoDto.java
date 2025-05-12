package com.backend.tessera.versao.dto;

import com.backend.tessera.auth.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VersaoDto {
    private Long id;
    private String numeroVersao;
    private String hashArquivo;
    private String nomeArquivo;
    private String mensagemCommit;
    private String tag;
    private User criadoPor;
    private LocalDateTime dataCriacao;
    private Long tamanhoArquivo;
}