package com.backend.tessera.versao.service;

import com.backend.tessera.auth.entity.User;
import com.backend.tessera.monografia.entity.Monografia;
import com.backend.tessera.monografia.repository.MonografiaRepository;
import com.backend.tessera.versao.dto.NovaVersaoRequest;
import com.backend.tessera.versao.dto.VersaoDto;
import com.backend.tessera.versao.entity.Versao;
import com.backend.tessera.versao.exception.ArquivoException;
import com.backend.tessera.versao.exception.VersaoNotFoundException;
import com.backend.tessera.versao.repository.VersaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VersaoService {

    private final VersaoRepository versaoRepository;
    private final MonografiaRepository monografiaRepository;
    private final ArquivoService arquivoService;

    @Value("${app.storage.versoes-dir}")
    private String versoesDir;

    @Transactional(readOnly = true)
    public List<VersaoDto> listarVersoesPorMonografia(Long monografiaId) {
        Monografia monografia = monografiaRepository.findById(monografiaId)
                .orElseThrow(() -> new RuntimeException("Monografia não encontrada com ID: " + monografiaId));

        return versaoRepository.findByMonografiaOrderByDataCriacaoDesc(monografia)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VersaoDto getVersao(Long versaoId) {
        Versao versao = versaoRepository.findById(versaoId)
                .orElseThrow(() -> new VersaoNotFoundException("Versão não encontrada com ID: " + versaoId));

        return convertToDto(versao);
    }

    @Transactional(readOnly = true)
    public String getConteudoVersao(Long versaoId) {
        Versao versao = versaoRepository.findById(versaoId)
                .orElseThrow(() -> new VersaoNotFoundException("Versão não encontrada com ID: " + versaoId));

        try {
            return arquivoService.lerConteudoArquivo(versao.getCaminhoArquivo());
        } catch (IOException e) {
            throw new ArquivoException("Erro ao ler conteúdo da versão: " + e.getMessage());
        }
    }

    @Transactional
    public VersaoDto criarVersao(NovaVersaoRequest request) {
        Monografia monografia = monografiaRepository.findById(request.getMonografiaId())
                .orElseThrow(() -> new RuntimeException("Monografia não encontrada com ID: " + request.getMonografiaId()));

        // Obter usuário atual
        User usuarioAtual = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Gerar número da versão
        String numeroVersao = gerarNumeroVersao(monografia);

        // Salvar o conteúdo em arquivo
        String nomeArquivo = "monografia_" + monografia.getId() + "_v" + numeroVersao + ".html";
        String caminhoRelativo = monografia.getId() + "/" + nomeArquivo;
        Path caminhoCompleto = Paths.get(versoesDir, caminhoRelativo);

        try {
            // Garantir que o diretório existe
            Files.createDirectories(caminhoCompleto.getParent());
            
            // Escrever o conteúdo no arquivo
            arquivoService.salvarConteudoArquivo(request.getConteudo(), caminhoRelativo);
            
            // Calcular hash do conteúdo
            String hashArquivo = calcularHashConteudo(request.getConteudo());
            
            // Criar entidade de versão
            Versao versao = new Versao();
            versao.setMonografia(monografia);
            versao.setNumeroVersao(numeroVersao);
            versao.setHashArquivo(hashArquivo);
            versao.setNomeArquivo(nomeArquivo);
            versao.setMensagemCommit(request.getMensagemCommit());
            versao.setTag(request.getTag());
            versao.setCriadoPor(usuarioAtual);
            versao.setCaminhoArquivo(caminhoRelativo);
            versao.setTamanhoArquivo((long) request.getConteudo().length());
            
            // Salvar no banco de dados
            Versao versaoSalva = versaoRepository.save(versao);
            
            return convertToDto(versaoSalva);
        } catch (IOException e) {
            throw new ArquivoException("Erro ao salvar arquivo da versão: " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            throw new ArquivoException("Erro ao calcular hash do conteúdo: " + e.getMessage());
        }
    }

    private String gerarNumeroVersao(Monografia monografia) {
        long count = versaoRepository.countByMonografia(monografia);
        return String.format("%d.0", count + 1);
    }

    private String calcularHashConteudo(String conteudo) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(conteudo.getBytes());
        
        // Converter bytes para representação hexadecimal
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        
        return hexString.toString();
    }

    private VersaoDto convertToDto(Versao versao) {
        VersaoDto dto = new VersaoDto();
        dto.setId(versao.getId());
        dto.setNumeroVersao(versao.getNumeroVersao());
        dto.setHashArquivo(versao.getHashArquivo());
        dto.setNomeArquivo(versao.getNomeArquivo());
        dto.setMensagemCommit(versao.getMensagemCommit());
        dto.setTag(versao.getTag());
        dto.setCriadoPor(versao.getCriadoPor()); // Mapear adequadamente o User para UserDto
        dto.setDataCriacao(versao.getDataCriacao());
        dto.setTamanhoArquivo(versao.getTamanhoArquivo());
        
        return dto;
    }
}