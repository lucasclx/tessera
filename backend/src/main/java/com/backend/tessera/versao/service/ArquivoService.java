package com.backend.tessera.versao.service;

import com.backend.tessera.versao.exception.ArquivoException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Service
@RequiredArgsConstructor
public class ArquivoService {

    @Value("${app.storage.versoes-dir}")
    private String versoesDir;

    /**
     * Salva o conteúdo em um arquivo no caminho especificado
     * @param conteudo Conteúdo a ser salvo
     * @param caminhoRelativo Caminho relativo ao diretório de versões
     * @throws IOException Se ocorrer erro ao salvar o arquivo
     */
    public void salvarConteudoArquivo(String conteudo, String caminhoRelativo) throws IOException {
        Path caminhoCompleto = Paths.get(versoesDir, caminhoRelativo);
        
        // Garantir que o diretório existe
        Files.createDirectories(caminhoCompleto.getParent());
        
        // Escrever o conteúdo
        Files.write(
            caminhoCompleto, 
            conteudo.getBytes(StandardCharsets.UTF_8), 
            StandardOpenOption.CREATE, 
            StandardOpenOption.TRUNCATE_EXISTING
        );
    }

    /**
     * Lê o conteúdo de um arquivo no caminho especificado
     * @param caminhoRelativo Caminho relativo ao diretório de versões
     * @return Conteúdo do arquivo como String
     * @throws IOException Se ocorrer erro ao ler o arquivo
     */
    public String lerConteudoArquivo(String caminhoRelativo) throws IOException {
        Path caminhoCompleto = Paths.get(versoesDir, caminhoRelativo);
        
        if (!Files.exists(caminhoCompleto)) {
            throw new ArquivoException("Arquivo não encontrado: " + caminhoRelativo);
        }
        
        return Files.readString(caminhoCompleto, StandardCharsets.UTF_8);
    }

    /**
     * Exclui um arquivo no caminho especificado
     * @param caminhoRelativo Caminho relativo ao diretório de versões
     * @throws IOException Se ocorrer erro ao excluir o arquivo
     */
    public void excluirArquivo(String caminhoRelativo) throws IOException {
        Path caminhoCompleto = Paths.get(versoesDir, caminhoRelativo);
        
        if (Files.exists(caminhoCompleto)) {
            Files.delete(caminhoCompleto);
        }
    }
}