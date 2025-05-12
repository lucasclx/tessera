// src/app/core/services/comentario.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Comentario {
  id: number;
  comentario: string;
  autor: any;
  versaoId: number;
  dataCriacao: string;
  posicaoTexto?: string;
}

export interface NovoComentarioRequest {
  versaoId: number;
  comentario: string;
  posicaoTexto?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ComentarioService {
  private apiUrl = `${environment.apiUrl}/comentarios`;
  
  constructor(private http: HttpClient) {}
  
  /**
   * Obtém todos os comentários de uma versão
   * @param versaoId ID da versão
   */
  getComentariosPorVersao(versaoId: number): Observable<Comentario[]> {
    return this.http.get<Comentario[]>(`${this.apiUrl}/versao/${versaoId}`);
  }
  
  /**
   * Cria um novo comentário
   * @param comentario Dados do comentário a ser criado
   */
  criarComentario(comentario: NovoComentarioRequest): Observable<Comentario> {
    return this.http.post<Comentario>(this.apiUrl, comentario);
  }
  
  /**
   * Exclui um comentário
   * @param comentarioId ID do comentário a ser excluído
   */
  excluirComentario(comentarioId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${comentarioId}`);
  }
  
  /**
   * Responde a um comentário (criando um novo comentário vinculado)
   * @param comentarioId ID do comentário a ser respondido
   * @param resposta Texto da resposta
   */
  responderComentario(comentarioId: number, resposta: string): Observable<Comentario> {
    return this.http.post<Comentario>(`${this.apiUrl}/${comentarioId}/responder`, { comentario: resposta });
  }
  
  /**
   * Marca um comentário como resolvido
   * @param comentarioId ID do comentário
   */
  marcarComoResolvido(comentarioId: number): Observable<Comentario> {
    return this.http.put<Comentario>(`${this.apiUrl}/${comentarioId}/resolver`, {});
  }
}