package com.backend.tessera.versao.dto;

import com.backend.tessera.versao.entity.Versao;
import lombok.Data;

import java.util.List;

@Data
public class DiffResponse {
    private Versao versaoBase;
    private Versao versaoNova;
    private List<Object> diffs;
    private String htmlDiff;
    private int added;
    private int removed;
    private int modified;
}