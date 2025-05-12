package com.backend.tessera.versao.service;

import com.backend.tessera.versao.dto.DiffResponse;
import com.backend.tessera.versao.entity.Versao;
import com.backend.tessera.versao.exception.VersaoNotFoundException;
import com.backend.tessera.versao.repository.VersaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiffService {

    private final VersaoRepository versaoRepository;
    private final ArquivoService arquivoService;

    @Transactional(readOnly = true)
    public DiffResponse compararVersoes(Long versaoBaseId, Long versaoNovaId) {
        Versao versaoBase = versaoRepository.findById(versaoBaseId)
                .orElseThrow(() -> new VersaoNotFoundException("Versão base não encontrada com ID: " + versaoBaseId));
                
        Versao versaoNova = versaoRepository.findById(versaoNovaId)
                .orElseThrow(() -> new VersaoNotFoundException("Versão nova não encontrada com ID: " + versaoNovaId));
                
        try {
            String conteudoBase = arquivoService.lerConteudoArquivo(versaoBase.getCaminhoArquivo());
            String conteudoNovo = arquivoService.lerConteudoArquivo(versaoNova.getCaminhoArquivo());
            
            return calcularDiferencas(conteudoBase, conteudoNovo, versaoBase, versaoNova);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler arquivos para comparação: " + e.getMessage());
        }
    }
    
    private DiffResponse calcularDiferencas(String textoBase, String textoNovo, Versao versaoBase, Versao versaoNova) {
        // Implementação simplificada de diff
        // Em uma implementação real, usar uma biblioteca como java-diff-utils
        
        // Estatísticas do diff
        int added = 0;
        int removed = 0;
        int modified = 0;
        
        // Dividir os textos em linhas
        String[] linhasBase = textoBase.split("\n");
        String[] linhasNovas = textoNovo.split("\n");
        
        // Lista para armazenar as diferenças
        List<Object> diffs = new ArrayList<>();
        
        // Algoritmo de comparação simples (na implementação real, usar um algoritmo de diff mais robusto)
        int i = 0, j = 0;
        while (i < linhasBase.length || j < linhasNovas.length) {
            if (i >= linhasBase.length) {
                // Linhas adicionadas
                diffs.add(new Object() {
                    public final boolean added = true;
                    public final String value = linhasNovas[j];
                });
                added++;
                j++;
            }// Continuação do DiffService.java
            else if (j >= linhasNovas.length) {
                // Linhas removidas
                diffs.add(new Object() {
                    public final boolean removed = true;
                    public final String value = linhasBase[i];
                });
                removed++;
                i++;
            }
            else if (linhasBase[i].equals(linhasNovas[j])) {
                // Linhas iguais (contexto)
                diffs.add(new Object() {
                    public final String value = linhasBase[i];
                });
                i++;
                j++;
            }
            else {
                // Linhas diferentes (modificadas)
                diffs.add(new Object() {
                    public final boolean removed = true;
                    public final String value = linhasBase[i];
                });
                diffs.add(new Object() {
                    public final boolean added = true;
                    public final String value = linhasNovas[j];
                });
                modified++;
                i++;
                j++;
            }
        }
        
        // Gerar HTML com as diferenças
        StringBuilder htmlDiff = new StringBuilder();
        htmlDiff.append("<div class='diff-container'>");
        
        for (Object diff : diffs) {
            if (diff instanceof Object && ((Map)diff).containsKey("removed") && (boolean)((Map)diff).get("removed")) {
                htmlDiff.append("<div class='diff-line diff-removed'>")
                      .append(((Map)diff).get("value"))
                      .append("</div>");
            }
            else if (diff instanceof Object && ((Map)diff).containsKey("added") && (boolean)((Map)diff).get("added")) {
                htmlDiff.append("<div class='diff-line diff-added'>")
                      .append(((Map)diff).get("value"))
                      .append("</div>");
            }
            else {
                htmlDiff.append("<div class='diff-line diff-context'>")
                      .append(((Map)diff).get("value"))
                      .append("</div>");
            }
        }
        
        htmlDiff.append("</div>");
        
        // Retornar resposta com diferenças
        DiffResponse response = new DiffResponse();
        response.setVersaoBase(versaoBase);
        response.setVersaoNova(versaoNova);
        response.setDiffs(diffs);
        response.setHtmlDiff(htmlDiff.toString());
        response.setAdded(added);
        response.setRemoved(removed);
        response.setModified(modified);
        
        return response;
    }
}