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
import java.util.Map; // IMPORT ADICIONADO

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
        int added = 0;
        int removed = 0;
        int modified = 0; // Pode não ser usado diretamente se contarmos apenas adições/remoções de linhas inteiras

        String[] linhasBase = textoBase.split("\n");
        String[] linhasNovas = textoNovo.split("\n");

        List<Map<String, Object>> diffs = new ArrayList<>(); // Alterado para List<Map<String, Object>>

        int i = 0, j = 0;
        while (i < linhasBase.length || j < linhasNovas.length) {
            if (i >= linhasBase.length) {
                diffs.add(Map.of("added", true, "value", linhasNovas[j]));
                added++;
                j++;
            } else if (j >= linhasNovas.length) {
                diffs.add(Map.of("removed", true, "value", linhasBase[i]));
                removed++;
                i++;
            } else if (linhasBase[i].equals(linhasNovas[j])) {
                diffs.add(Map.of("context", true, "value", linhasBase[i]));
                i++;
                j++;
            } else {
                // Considera como uma linha removida e uma adicionada
                diffs.add(Map.of("removed", true, "value", linhasBase[i]));
                removed++;
                i++;
                // Não incrementar j aqui se a linha nova correspondente ainda não foi processada
                // No entanto, para uma comparação linha a linha simples, geralmente se avança ambos
                // ou se busca a próxima sincronização. Para simplificar, avançamos ambos após adicionar
                // a linha nova como "added". Uma biblioteca de diff faria isso de forma mais inteligente.
                if (j < linhasNovas.length) {
                     diffs.add(Map.of("added", true, "value", linhasNovas[j]));
                     added++;
                     j++;
                }
                modified++; // Conta como uma modificação de linha (1 removida, 1 adicionada)
            }
        }

        StringBuilder htmlDiff = new StringBuilder();
        htmlDiff.append("<div class='diff-container'>");

        for (Map<String, Object> diffLine : diffs) { // Alterado o tipo do loop variable
            if (Boolean.TRUE.equals(diffLine.get("removed"))) {
                htmlDiff.append("<div class='diff-line diff-removed'>- ")
                      .append(escapeHtml(diffLine.get("value").toString()))
                      .append("</div>");
            } else if (Boolean.TRUE.equals(diffLine.get("added"))) {
                htmlDiff.append("<div class='diff-line diff-added'>+ ")
                      .append(escapeHtml(diffLine.get("value").toString()))
                      .append("</div>");
            } else { // context
                htmlDiff.append("<div class='diff-line diff-context'>  ")
                      .append(escapeHtml(diffLine.get("value").toString()))
                      .append("</div>");
            }
        }
        htmlDiff.append("</div>");

        DiffResponse response = new DiffResponse();
        response.setVersaoBase(versaoBase); // Idealmente, envie DTOs aqui também
        response.setVersaoNova(versaoNova); // Idealmente, envie DTOs aqui também
        response.setDiffs(new ArrayList<>(diffs)); // Converte para List<Object> se necessário pelo DTO
        response.setHtmlDiff(htmlDiff.toString());
        response.setAdded(added);
        response.setRemoved(removed);
        response.setModified(modified); // Número de linhas que foram substituídas (1 del + 1 add)

        return response;
    }

    // Método auxiliar para escapar HTML (simples)
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}