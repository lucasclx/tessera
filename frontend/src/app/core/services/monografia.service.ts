// src/app/core/services/monografia.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
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
    return this.http.get<Monografia[]>(this.apiUrl);
  }
  
  /**
   * Obtém monografias onde o usuário atual é orientador
   */
  getMonografiasOrientador(): Observable<Monografia[]> {
    return this.http.get<Monografia[]>(`${this.apiUrl}/orientador`);
  }
  
  /**
   * Obtém detalhes de uma monografia específica
   * @param id ID da monografia
   */
  getMonografia(id: number): Observable<Monografia> {
    return this.http.get<Monografia>(`${this.apiUrl}/${id}`);
  }
  
  /**
   * Cria uma nova monografia
   * @param monografia Dados da monografia a ser criada
   */
  criarMonografia(monografia: MonografiaRequest): Observable<Monografia> {
    return this.http.post<Monografia>(this.apiUrl, monografia);
  }
  
  /**
   * Atualiza uma monografia existente
   * @param monografia Dados da monografia a ser atualizada
   */
  atualizarMonografia(monografia: MonografiaAtualizacaoRequest): Observable<Monografia> {
    return this.http.put<Monografia>(`${this.apiUrl}/${monografia.id}`, monografia);
  }
  
  /**
   * Exclui uma monografia
   * @param id ID da monografia a ser excluída
   */
  excluirMonografia(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
  
  /**
   * Adiciona um co-autor a uma monografia
   * @param monografiaId ID da monografia
   * @param userId ID do usuário a ser adicionado como co-autor
   */
  adicionarCoAutor(monografiaId: number, userId: number): Observable<Monografia> {
    return this.http.post<Monografia>(`${this.apiUrl}/${monografiaId}/co-autores/${userId}`, {});
  }
  
  /**
   * Remove um co-autor de uma monografia
   * @param monografiaId ID da monografia
   * @param userId ID do usuário a ser removido
   */
  removerCoAutor(monografiaId: number, userId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${monografiaId}/co-autores/${userId}`);
  }
  
  /**
   * Adiciona um co-orientador a uma monografia
   * @param monografiaId ID da monografia
   * @param userId ID do usuário a ser adicionado como co-orientador
   */
  adicionarCoOrientador(monografiaId: number, userId: number): Observable<Monografia> {
    return this.http.post<Monografia>(`${this.apiUrl}/${monografiaId}/co-orientadores/${userId}`, {});
  }
  
  /**
   * Remove um co-orientador de uma monografia
   * @param monografiaId ID da monografia
   * @param userId ID do usuário a ser removido
   */
  removerCoOrientador(monografiaId: number, userId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${monografiaId}/co-orientadores/${userId}`);
  }
}