package com.backend.tessera.monografia.repository;

import com.backend.tessera.auth.entity.User;
import com.backend.tessera.monografia.entity.Monografia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MonografiaRepository extends JpaRepository<Monografia, Long> {

    /**
     * Busca monografias onde o usuário é autor principal ou co-autor
     * @param autorId ID do autor principal
     * @param coAutor Usuário co-autor
     * @return Lista de monografias
     */
    List<Monografia> findByAutorPrincipalIdOrCoAutoresContains(Long autorId, User coAutor);

    /**
     * Busca monografias onde o usuário é orientador principal ou co-orientador
     * @param orientadorId ID do orientador principal
     * @param coOrientador Usuário co-orientador
     * @return Lista de monografias
     */
    List<Monografia> findByOrientadorPrincipalIdOrCoOrientadoresContains(Long orientadorId, User coOrientador);

    /**
     * Busca monografias por título contendo a string de pesquisa
     * @param titulo String de pesquisa
     * @return Lista de monografias
     */
    List<Monografia> findByTituloContainingIgnoreCase(String titulo);

    /**
     * Busca monografias por autor principal e título
     * @param autorPrincipal Autor principal
     * @param titulo String de pesquisa
     * @return Lista de monografias
     */
    List<Monografia> findByAutorPrincipalAndTituloContainingIgnoreCase(User autorPrincipal, String titulo);
    
    /**
     * Busca monografias criadas pelo usuário (como autor principal)
     * @param autorPrincipal Autor principal
     * @return Lista de monografias
     */
    List<Monografia> findByAutorPrincipal(User autorPrincipal);
    
    /**
     * Busca monografias orientadas pelo usuário (como orientador principal)
     * @param orientadorPrincipal Orientador principal
     * @return Lista de monografias
     */
    List<Monografia> findByOrientadorPrincipal(User orientadorPrincipal);

    /**
     * Busca avançada combinando vários critérios
     * @param titulo Título (opcional)
     * @param descricao Descrição (opcional)
     * @param autorId ID do autor (opcional)
     * @param orientadorId ID do orientador (opcional)
     * @return Lista de monografias
     */
    @Query("SELECT m FROM Monografia m WHERE " +
           "(:titulo IS NULL OR LOWER(m.titulo) LIKE LOWER(CONCAT('%', :titulo, '%'))) AND " +
           "(:descricao IS NULL OR LOWER(m.descricao) LIKE LOWER(CONCAT('%', :descricao, '%'))) AND " +
           "(:autorId IS NULL OR m.autorPrincipal.id = :autorId) AND " +
           "(:orientadorId IS NULL OR m.orientadorPrincipal.id = :orientadorId)")
    List<Monografia> buscarPorCriterios(
            @Param("titulo") String titulo,
            @Param("descricao") String descricao,
            @Param("autorId") Long autorId,
            @Param("orientadorId") Long orientadorId);
}