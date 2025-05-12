package com.backend.tessera.comentario.controller;

import com.backend.tessera.comentario.dto.ComentarioDto;
import com.backend.tessera.comentario.dto.NovoComentarioRequest;
import com.backend.tessera.comentario.service.ComentarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/comentarios")
@RequiredArgsConstructor
public class ComentarioController {

    private final ComentarioService comentarioService;

    @GetMapping("/versao/{versaoId}")
    public ResponseEntity<List<ComentarioDto>> listarComentariosPorVersao(@PathVariable Long versaoId) {
        List<ComentarioDto> comentarios = comentarioService.listarComentariosPorVersao(versaoId);
        return ResponseEntity.ok(comentarios);
    }

    @PostMapping
    public ResponseEntity<ComentarioDto> criarComentario(@Valid @RequestBody NovoComentarioRequest request) {
        ComentarioDto comentario = comentarioService.criarComentario(request);
        return ResponseEntity.ok(comentario);
    }

    @DeleteMapping("/{comentarioId}")
    public ResponseEntity<Void> excluirComentario(@PathVariable Long comentarioId) {
        comentarioService.excluirComentario(comentarioId);
        return ResponseEntity.noContent().build();
    }
}