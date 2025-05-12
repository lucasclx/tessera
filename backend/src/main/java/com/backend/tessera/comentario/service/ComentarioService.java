package com.backend.tessera.comentario.service;

import com.backend.tessera.auth.entity.User;
import com.backend.tessera.comentario.dto.ComentarioDto;
import com.backend.tessera.comentario.dto.NovoComentarioRequest;
import com.backend.tessera.comentario.entity.Comentario;
import com.backend.tessera.comentario.exception.ComentarioNotFoundException;
import com.backend.tessera.comentario.repository.ComentarioRepository;
import com.backend.tessera.versao.entity.Versao;
import com.backend.tessera.versao.repository.VersaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final VersaoRepository versaoRepository;

    @Transactional(readOnly = true)
    public List<ComentarioDto> listarComentariosPorVersao(Long versaoId) {
        return comentarioRepository.findByVersaoIdOrderByDataCriacaoDesc(versaoId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ComentarioDto criarComentario(NovoComentarioRequest request) {
        Versao versao = versaoRepository.findById(request.getVersaoId())
                .orElseThrow(() -> new RuntimeException("Versão não encontrada com ID: " + request.getVersaoId()));

        // Obter usuário atual
        User usuarioAtual = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Comentario comentario = new Comentario();
        comentario.setVersao(versao);
        comentario.setComentario(request.getComentario());
        comentario.setAutor(usuarioAtual);
        comentario.setPosicaoTexto(request.getPosicaoTexto());

        Comentario comentarioSalvo = comentarioRepository.save(comentario);
        return convertToDto(comentarioSalvo);
    }

    @Transactional
    public void excluirComentario(Long comentarioId) {
        Comentario comentario = comentarioRepository.findById(comentarioId)
                .orElseThrow(() -> new ComentarioNotFoundException("Comentário não encontrado com ID: " + comentarioId));

        // Obter usuário atual
        User usuarioAtual = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Verificar se o usuário atual é o autor do comentário ou se é um administrador
        if (comentario.getAutor().getId().equals(usuarioAtual.getId()) || 
            usuarioAtual.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            comentarioRepository.delete(comentario);
        } else {
            throw new RuntimeException("Você não tem permissão para excluir este comentário");
        }
    }

    private ComentarioDto convertToDto(Comentario comentario) {
        ComentarioDto dto = new ComentarioDto();
        dto.setId(comentario.getId());
        dto.setComentario(comentario.getComentario());
        dto.setAutor(comentario.getAutor()); // Mapear adequadamente o User
        dto.setVersaoId(comentario.getVersao().getId());
        dto.setDataCriacao(comentario.getDataCriacao());
        dto.setPosicaoTexto(comentario.getPosicaoTexto());
        
        return dto;
    }
}