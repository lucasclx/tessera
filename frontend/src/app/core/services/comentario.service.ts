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
  
  getComentariosPorVersao(versaoId: number): Observable<Comentario[]> {
    return this.http.get<Comentario[]>(`${this.apiUrl}/versao/${versaoId}`);
  }
  
  criarComentario(novoComentario: NovoComentarioRequest): Observable<Comentario> {
    return this.http.post<Comentario>(`${this.apiUrl}`, novoComentario);
  }
  
  excluirComentario(comentarioId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${comentarioId}`);
  }
}