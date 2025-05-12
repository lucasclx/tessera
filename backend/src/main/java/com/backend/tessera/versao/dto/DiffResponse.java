package com.backend.tessera.versao.dto;

import com.backend.tessera.versao.entity.Versao;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiffResponse {
    private Versao versaoBase;
    private Versao versaoNova;
    private List<Map<String, Object>> diffs;
    private String htmlDiff;
    private int added;
    private int removed;
    private int modified;
}