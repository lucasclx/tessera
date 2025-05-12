// src/app/core/services/monografia.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

export interface Monografia {
  id: number;
  titulo: string;
  descricao?: string;
  autorPrincipal: any;
  coAutores?: any[];
  orientadorPrincipal: any;
  coOrientadores?: any[];
  dataCriacao: string;
  dataAtualizacao?: string;
}

export interface MonografiaRequest {
  titulo: string;
  descricao?: string;
  orientadorPrincipalId: number;
  coOrientadoresIds?: number[];
}

export interface MonografiaAtualizacaoRequest {
  id: number;
  titulo?: string;
  descricao?: string;
  orientadorPrincipalId?: number;
  coOrientadoresIds?: number[];
}

@Injectable({
  providedIn: 'root'
})
export class MonografiaService {
  private apiUrl = `${environment.apiUrl}/monografias`;
  
  constructor(private http: HttpClient) {}
  
  /**
   * Obtém todas as monografias do usuário atual
   */
  getMonografias(): Observable<Monografia[]> {
    console.log('Buscando monografias do usuário atual...');
    return this.http.get<Monografia[]>(this.apiUrl)
      .pipe(
        tap(response => console.log('Monografias recebidas:', response)),
        catchError(error => {
          console.error('Erro ao buscar monografias:', error);
          return throwError(() => error);
        })
      );
  }
  
  /**
   * Obtém monografias onde o usuário atual é orientador
   */
  getMonografiasOrientador(): Observable<Monografia[]> {
    console.log('Buscando monografias onde o usuário é orientador...');
    return this.http.get<Monografia[]>(`${this.apiUrl}/orientador`)
      .pipe(
        tap(response => console.log('Monografias de orientação recebidas:', response)),
        catchError(error => {
          console.error('Erro ao buscar monografias de orientação:', error);
          return throwError(() => error);
        })
      );
  }
  
  /**
   * Obtém detalhes de uma monografia específica
   * @param id ID da monografia
   */
  getMonografia(id: number): Observable<Monografia> {
    console.log(`Buscando detalhes da monografia ID ${id}...`);
    return this.http.get<Monografia>(`${this.apiUrl}/${id}`)
      .pipe(
        tap(response => console.log('Detalhes da monografia recebidos:', response)),
        catchError(error => {
          console.error(`Erro ao buscar monografia ID ${id}:`, error);
          return throwError(() => error);
        })
      );
  }
  
  /**
   * Cria uma nova monografia
   * @param monografia Dados da monografia a ser criada
   */
  criarMonografia(monografia: MonografiaRequest): Observable<Monografia> {
    console.log('Criando nova monografia:', monografia);
    return this.http.post<Monografia>(this.apiUrl, monografia)
      .pipe(
        tap(response => console.log('Monografia criada com sucesso:', response)),
        catchError(error => {
          console.error('Erro ao criar monografia:', error);
          return throwError(() => error);
        })
      );
  }
  
  /**
   * Atualiza uma monografia existente
   * @param monografia Dados da monografia a ser atualizada
   */
  atualizarMonografia(monografia: MonografiaAtualizacaoRequest): Observable<Monografia> {
    console.log(`Atualizando monografia ID ${monografia.id}:`, monografia);
    return this.http.put<Monografia>(`${this.apiUrl}/${monografia.id}`, monografia)
      .pipe(
        tap(response => console.log('Monografia atualizada com sucesso:', response)),
        catchError(error => {
          console.error(`Erro ao atualizar monografia ID ${monografia.id}:`, error);
          return throwError(() => error);
        })
      );
  }
  
  /**
   * Exclui uma monografia
   * @param id ID da monografia a ser excluída
   */
  excluirMonografia(id: number): Observable<void> {
    console.log(`Excluindo monografia ID ${id}...`);
    return this.http.delete<void>(`${this.apiUrl}/${id}`)
      .pipe(
        tap(() => console.log(`Monografia ID ${id} excluída com sucesso`)),
        catchError(error => {
          console.error(`Erro ao excluir monografia ID ${id}:`, error);
          return throwError(() => error);
        })
      );
  }
  
  /**
   * Adiciona um co-autor a uma monografia
   * @param monografiaId ID da monografia
   * @param userId ID do usuário a ser adicionado como co-autor
   */
  adicionarCoAutor(monografiaId: number, userId: number): Observable<Monografia> {
    console.log(`Adicionando co-autor (ID ${userId}) à monografia ID ${monografiaId}...`);
    return this.http.post<Monografia>(`${this.apiUrl}/${monografiaId}/co-autores/${userId}`, {})
      .pipe(
        tap(response => console.log('Co-autor adicionado com sucesso:', response)),
        catchError(error => {
          console.error(`Erro ao adicionar co-autor (ID ${userId}) à monografia ID ${monografiaId}:`, error);
          return throwError(() => error);
        })
      );
  }
  
  /**
   * Remove um co-autor de uma monografia
   * @param monografiaId ID da monografia
   * @param userId ID do usuário a ser removido
   */
  removerCoAutor(monografiaId: number, userId: number): Observable<void> {
    console.log(`Removendo co-autor (ID ${userId}) da monografia ID ${monografiaId}...`);
    return this.http.delete<void>(`${this.apiUrl}/${monografiaId}/co-autores/${userId}`)
      .pipe(
        tap(() => console.log(`Co-autor (ID ${userId}) removido com sucesso da monografia ID ${monografiaId}`)),
        catchError(error => {
          console.error(`Erro ao remover co-autor (ID ${userId}) da monografia ID ${monografiaId}:`, error);
          return throwError(() => error);
        })
      );
  }
  
  /**
   * Adiciona um co-orientador a uma monografia
   * @param monografiaId ID da monografia
   * @param userId ID do usuário a ser adicionado como co-orientador
   */
  adicionarCoOrientador(monografiaId: number, userId: number): Observable<Monografia> {
    console.log(`Adicionando co-orientador (ID ${userId}) à monografia ID ${monografiaId}...`);
    return this.http.post<Monografia>(`${this.apiUrl}/${monografiaId}/co-orientadores/${userId}`, {})
      .pipe(
        tap(response => console.log('Co-orientador adicionado com sucesso:', response)),
        catchError(error => {
          console.error(`Erro ao adicionar co-orientador (ID ${userId}) à monografia ID ${monografiaId}:`, error);
          return throwError(() => error);
        })
      );
  }
  
  /**
   * Remove um co-orientador de uma monografia
   * @param monografiaId ID da monografia
   * @param userId ID do usuário a ser removido
   */
  removerCoOrientador(monografiaId: number, userId: number): Observable<void> {
    console.log(`Removendo co-orientador (ID ${userId}) da monografia ID ${monografiaId}...`);
    return this.http.delete<void>(`${this.apiUrl}/${monografiaId}/co-orientadores/${userId}`)
      .pipe(
        tap(() => console.log(`Co-orientador (ID ${userId}) removido com sucesso da monografia ID ${monografiaId}`)),
        catchError(error => {
          console.error(`Erro ao remover co-orientador (ID ${userId}) da monografia ID ${monografiaId}:`, error);
          return throwError(() => error);
        })
      );
  }
}