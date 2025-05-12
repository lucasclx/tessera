package com.backend.tessera.monografia.service;

import com.backend.tessera.auth.entity.Role;
import com.backend.tessera.auth.entity.User;
import com.backend.tessera.monografia.dto.MonografiaDto;
import com.backend.tessera.monografia.dto.MonografiaRequest;
import com.backend.tessera.monografia.entity.Monografia;
import com.backend.tessera.monografia.exception.MonografiaNotFoundException;
import com.backend.tessera.monografia.repository.MonografiaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MonografiaService {

    private static final Logger logger = LoggerFactory.getLogger(MonografiaService.class);

    @Autowired
    private MonografiaRepository monografiaRepository;

    /**
     * Busca monografias para o usuário autenticado com base em seu papel
     * @param authentication Objeto de autenticação do usuário
     * @return Lista de monografias que o usuário tem acesso
     */
    @Transactional(readOnly = true)
    public List<Monografia> buscarMonografiasParaUsuarioAutenticado(Authentication authentication) {
        logger.info("Buscando monografias para usuário autenticado...");
        
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Usuário não autenticado ou autenticação nula");
            return Collections.emptyList();
        }
        
        User usuarioAtual = (User) authentication.getPrincipal();
        logger.info("Usuário autenticado: {}, Papel: {}", usuarioAtual.getUsername(), usuarioAtual.getRole());
        
        List<Monografia> monografias;
        
        try {
            if (usuarioAtual.getRole() == Role.ADMIN) {
                logger.info("Usuário ADMIN: buscando todas as monografias");
                monografias = monografiaRepository.findAll();
            } else if (usuarioAtual.getRole() == Role.PROFESSOR) {
                logger.info("Usuário PROFESSOR: buscando monografias onde é orientador ou co-orientador");
                monografias = monografiaRepository.findByOrientadorPrincipalIdOrCoOrientadoresContains(usuarioAtual.getId(), usuarioAtual);
            } else {
                // ALUNO
                logger.info("Usuário ALUNO: buscando monografias onde é autor principal ou co-autor");
                monografias = monografiaRepository.findByAutorPrincipalIdOrCoAutoresContains(usuarioAtual.getId(), usuarioAtual);
            }
            
            logger.info("Total de monografias encontradas: {}", monografias.size());
            return monografias;
            
        } catch (Exception e) {
            logger.error("Erro ao buscar monografias para o usuário {}: {}", usuarioAtual.getUsername(), e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Busca uma monografia por ID, verificando permissões de acesso
     * @param id ID da monografia
     * @param authentication Objeto de autenticação do usuário
     * @return Monografia encontrada
     */
    @Transactional(readOnly = true)
    public Monografia findByIdAndUser(Long id, Authentication authentication) {
        logger.info("Buscando monografia por ID: {}", id);
        
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Tentativa de acesso não autenticado à monografia ID: {}", id);
            throw new AccessDeniedException("Acesso negado");
        }
        
        User usuarioAtual = (User) authentication.getPrincipal();
        logger.info("Usuário: {}, Papel: {}", usuarioAtual.getUsername(), usuarioAtual.getRole());
        
        Optional<Monografia> monografiaOpt = monografiaRepository.findById(id);
        if (monografiaOpt.isEmpty()) {
            logger.warn("Monografia não encontrada com ID: {}", id);
            throw new MonografiaNotFoundException("Monografia não encontrada com ID: " + id);
        }
        
        Monografia monografia = monografiaOpt.get();
        
        // Verificar permissões de acesso
        if (usuarioAtual.getRole() == Role.ADMIN) {
            // Administradores têm acesso a todas as monografias
            logger.info("Acesso ADMIN à monografia ID: {}", id);
            return monografia;
        } else if (usuarioAtual.getRole() == Role.PROFESSOR) {
            // Verificar se é orientador ou co-orientador
            if (monografia.getOrientadorPrincipal().getId().equals(usuarioAtual.getId()) ||
                monografia.getCoOrientadores().stream().anyMatch(user -> user.getId().equals(usuarioAtual.getId()))) {
                logger.info("Acesso PROFESSOR à monografia ID: {} (Orientador/Co-orientador)", id);
                return monografia;
            }
        } else {
            // Verificar se é autor principal ou co-autor
            if (monografia.getAutorPrincipal().getId().equals(usuarioAtual.getId()) ||
                monografia.getCoAutores().stream().anyMatch(user -> user.getId().equals(usuarioAtual.getId()))) {
                logger.info("Acesso ALUNO à monografia ID: {} (Autor/Co-autor)", id);
                return monografia;
            }
        }
        
        logger.warn("Acesso negado para usuário {} à monografia ID: {}", usuarioAtual.getUsername(), id);
        throw new AccessDeniedException("Você não tem permissão para acessar esta monografia");
    }

    /**
     * Criar uma nova monografia
     * @param monografiaRequest Dados da monografia
     * @param authentication Objeto de autenticação do usuário
     * @return Monografia criada
     */
    @Transactional
    public Monografia criarMonografia(Monografia monografiaRequest, Authentication authentication) {
        logger.info("Criando nova monografia");
        
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Tentativa de criação de monografia por usuário não autenticado");
            throw new AccessDeniedException("Acesso negado");
        }
        
        User usuarioAtual = (User) authentication.getPrincipal();
        logger.info("Usuário criador: {}, Papel: {}", usuarioAtual.getUsername(), usuarioAtual.getRole());
        
        // Verificar permissões
        if (usuarioAtual.getRole() != Role.ADMIN && usuarioAtual.getRole() != Role.PROFESSOR &&
            usuarioAtual.getRole() != Role.ALUNO) {
            logger.warn("Usuário {} sem permissão para criar monografia", usuarioAtual.getUsername());
            throw new AccessDeniedException("Você não tem permissão para criar monografias");
        }
        
        // Setar informações básicas
        Monografia novaMonografia = new Monografia();
        novaMonografia.setTitulo(monografiaRequest.getTitulo());
        novaMonografia.setDescricao(monografiaRequest.getDescricao());
        
        // Definir autor principal (se não for fornecido, usar o usuário atual se for ALUNO)
        if (monografiaRequest.getAutorPrincipal() != null) {
            novaMonografia.setAutorPrincipal(monografiaRequest.getAutorPrincipal());
        } else if (usuarioAtual.getRole() == Role.ALUNO) {
            novaMonografia.setAutorPrincipal(usuarioAtual);
        } else {
            logger.error("Autor principal não especificado e usuário criador não é ALUNO");
            throw new IllegalArgumentException("É necessário especificar um autor principal");
        }
        
        // Definir orientador principal (se não for fornecido, usar o usuário atual se for PROFESSOR)
        if (monografiaRequest.getOrientadorPrincipal() != null) {
            novaMonografia.setOrientadorPrincipal(monografiaRequest.getOrientadorPrincipal());
        } else if (usuarioAtual.getRole() == Role.PROFESSOR) {
            novaMonografia.setOrientadorPrincipal(usuarioAtual);
        } else {
            logger.error("Orientador principal não especificado e usuário criador não é PROFESSOR");
            throw new IllegalArgumentException("É necessário especificar um orientador principal");
        }
        
        // Definir co-autores e co-orientadores (se fornecidos)
        if (monografiaRequest.getCoAutores() != null) {
            novaMonografia.setCoAutores(monografiaRequest.getCoAutores());
        } else {
            novaMonografia.setCoAutores(new ArrayList<>());
        }
        
        if (monografiaRequest.getCoOrientadores() != null) {
            novaMonografia.setCoOrientadores(monografiaRequest.getCoOrientadores());
        } else {
            novaMonografia.setCoOrientadores(new ArrayList<>());
        }
        
        // Versões inicialmente vazia
        novaMonografia.setVersoes(new ArrayList<>());
        
        // Salvar e retornar
        Monografia monografiaSalva = monografiaRepository.save(novaMonografia);
        logger.info("Monografia criada com sucesso. ID: {}", monografiaSalva.getId());
        
        return monografiaSalva;
    }

    /**
     * Atualizar uma monografia existente
     * @param id ID da monografia
     * @param monografiaRequest Dados atualizados
     * @return Monografia atualizada
     */
    @Transactional
    public Monografia atualizarMonografia(Long id, Monografia monografiaRequest) {
        logger.info("Atualizando monografia ID: {}", id);
        
        // Obter usuário atual
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User usuarioAtual = (User) authentication.getPrincipal();
        logger.info("Usuário editor: {}, Papel: {}", usuarioAtual.getUsername(), usuarioAtual.getRole());
        
        // Buscar monografia existente
        Monografia monografiaExistente = monografiaRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Monografia não encontrada com ID: {}", id);
                    return new MonografiaNotFoundException("Monografia não encontrada com ID: " + id);
                });
        
        // Verificar permissões
        boolean temPermissao = false;
        
        if (usuarioAtual.getRole() == Role.ADMIN) {
            temPermissao = true;
        } else if (usuarioAtual.getRole() == Role.PROFESSOR &&
                  (monografiaExistente.getOrientadorPrincipal().getId().equals(usuarioAtual.getId()) ||
                   monografiaExistente.getCoOrientadores().stream().anyMatch(u -> u.getId().equals(usuarioAtual.getId())))) {
            temPermissao = true;
        } else if (usuarioAtual.getRole() == Role.ALUNO &&
                  monografiaExistente.getAutorPrincipal().getId().equals(usuarioAtual.getId())) {
            temPermissao = true;
        }
        
        if (!temPermissao) {
            logger.warn("Usuário {} sem permissão para editar monografia ID: {}", usuarioAtual.getUsername(), id);
            throw new AccessDeniedException("Você não tem permissão para editar esta monografia");
        }
        
        // Atualizar campos permitidos
        monografiaExistente.setTitulo(monografiaRequest.getTitulo());
        monografiaExistente.setDescricao(monografiaRequest.getDescricao());
        
        // Campos que podem exigir verificações adicionais
        if (usuarioAtual.getRole() == Role.ADMIN || usuarioAtual.getRole() == Role.PROFESSOR) {
            // Administradores e professores podem alterar autores e orientadores
            if (monografiaRequest.getAutorPrincipal() != null) {
                monografiaExistente.setAutorPrincipal(monografiaRequest.getAutorPrincipal());
            }
            
            if (monografiaRequest.getOrientadorPrincipal() != null) {
                monografiaExistente.setOrientadorPrincipal(monografiaRequest.getOrientadorPrincipal());
            }
            
            if (monografiaRequest.getCoAutores() != null) {
                monografiaExistente.setCoAutores(monografiaRequest.getCoAutores());
            }
            
            if (monografiaRequest.getCoOrientadores() != null) {
                monografiaExistente.setCoOrientadores(monografiaRequest.getCoOrientadores());
            }
        }
        
        // Salvar e retornar
        Monografia monografiaAtualizada = monografiaRepository.save(monografiaExistente);
        logger.info("Monografia ID: {} atualizada com sucesso", id);
        
        return monografiaAtualizada;
    }

    /**
     * Excluir uma monografia
     * @param id ID da monografia
     */
    @Transactional
    public void excluirMonografia(Long id) {
        logger.info("Excluindo monografia ID: {}", id);
        
        // Obter usuário atual
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User usuarioAtual = (User) authentication.getPrincipal();
        logger.info("Usuário: {}, Papel: {}", usuarioAtual.getUsername(), usuarioAtual.getRole());
        
        // Buscar monografia
        Monografia monografia = monografiaRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Monografia não encontrada com ID: {}", id);
                    return new MonografiaNotFoundException("Monografia não encontrada com ID: " + id);
                });
        
        // Verificar permissões (apenas ADMINs e PROFESSORs orientadores podem excluir)
        boolean temPermissao = false;
        
        if (usuarioAtual.getRole() == Role.ADMIN) {
            temPermissao = true;
        } else if (usuarioAtual.getRole() == Role.PROFESSOR &&
                  monografia.getOrientadorPrincipal().getId().equals(usuarioAtual.getId())) {
            temPermissao = true;
        }
        
        if (!temPermissao) {
            logger.warn("Usuário {} sem permissão para excluir monografia ID: {}", usuarioAtual.getUsername(), id);
            throw new AccessDeniedException("Você não tem permissão para excluir esta monografia");
        }
        
        // Excluir monografia
        monografiaRepository.delete(monografia);
        logger.info("Monografia ID: {} excluída com sucesso", id);
    }

    /**
     * Converter Monografia para DTO
     * @param monografia Entidade Monografia
     * @return MonografiaDto
     */
    private MonografiaDto convertToDto(Monografia monografia) {
        MonografiaDto dto = new MonografiaDto();
        dto.setId(monografia.getId());
        dto.setTitulo(monografia.getTitulo());
        dto.setDescricao(monografia.getDescricao());
        dto.setAutorPrincipal(monografia.getAutorPrincipal());
        dto.setOrientadorPrincipal(monografia.getOrientadorPrincipal());
        dto.setDataCriacao(monografia.getDataCriacao());
        dto.setDataAtualizacao(monografia.getDataAtualizacao());
        
        // Converter listas
        if (monografia.getCoAutores() != null) {
            dto.setCoAutores(monografia.getCoAutores());
        }
        
        if (monografia.getCoOrientadores() != null) {
            dto.setCoOrientadores(monografia.getCoOrientadores());
        }
        
        return dto;
    }

    /**
     * Buscar todas as monografias (apenas para ADMINs)
     * @return Lista de todas as monografias
     */
    @Transactional(readOnly = true)
    public List<MonografiaDto> buscarTodas() {
        logger.info("Buscando todas as monografias");
        
        // Obter usuário atual
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User usuarioAtual = (User) authentication.getPrincipal();
        
        // Verificar se é ADMIN
        if (usuarioAtual.getRole() != Role.ADMIN) {
            logger.warn("Usuário {} sem permissão para listar todas as monografias", usuarioAtual.getUsername());
            throw new AccessDeniedException("Apenas administradores podem listar todas as monografias");
        }
        
        List<Monografia> monografias = monografiaRepository.findAll();
        logger.info("Total de monografias encontradas: {}", monografias.size());
        
        return monografias.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
}