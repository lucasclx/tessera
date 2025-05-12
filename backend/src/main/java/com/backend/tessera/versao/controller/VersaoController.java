package com.backend.tessera.versao.controller;

import com.backend.tessera.versao.dto.DiffResponse;
import com.backend.tessera.versao.dto.NovaVersaoRequest;
import com.backend.tessera.versao.dto.VersaoDto;
import com.backend.tessera.versao.service.DiffService;
import com.backend.tessera.versao.service.VersaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid; // Import correto
import java.util.List;

@RestController
@RequestMapping("/api/versoes")
@RequiredArgsConstructor
public class VersaoController {

    private final VersaoService versaoService;
    private final DiffService diffService;

    @GetMapping("/monografia/{monografiaId}")
    public ResponseEntity<List<VersaoDto>> listarVersoesPorMonografia(@PathVariable Long monografiaId) {
        List<VersaoDto> versoes = versaoService.listarVersoesPorMonografia(monografiaId);
        return ResponseEntity.ok(versoes);
    }

    @GetMapping("/{versaoId}")
    public ResponseEntity<VersaoDto> getVersao(@PathVariable Long versaoId) {
        VersaoDto versao = versaoService.getVersao(versaoId);
        return ResponseEntity.ok(versao);
    }

    @GetMapping("/{versaoId}/conteudo")
    public ResponseEntity<String> getConteudoVersao(@PathVariable Long versaoId) {
        String conteudo = versaoService.getConteudoVersao(versaoId);
        return ResponseEntity.ok(conteudo);
    }

    @PostMapping
    public ResponseEntity<VersaoDto> criarVersao(@Valid @RequestBody NovaVersaoRequest request) {
        VersaoDto versao = versaoService.criarVersao(request);
        return ResponseEntity.ok(versao);
    }

    @GetMapping("/diff")
    public ResponseEntity<DiffResponse> compararVersoes(
            @RequestParam Long versaoBaseId,
            @RequestParam Long versaoNovaId) {
        DiffResponse diff = diffService.compararVersoes(versaoBaseId, versaoNovaId);
        return ResponseEntity.ok(diff);
    }
}