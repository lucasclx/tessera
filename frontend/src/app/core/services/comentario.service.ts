// src/app/core/services/comentario.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
// import { UserProfile } from '../auth.service'; // <--- REMOVIDO

export interface ComentarioAutor { // Definindo um tipo mais específico para o autor
  id: number;
  nome: string;
  username: string;
}
export interface Comentario {
  id: number;
  comentario: string;
  autor: ComentarioAutor; // Usar o tipo mais específico
  versaoId: number;
  dataCriacao: string;
  posicaoTexto?: string; // Este será nosso anchorId
  resolvido?: boolean;
  respostas?: Comentario[]; // Para threads
  // Outros campos como 'selectionTextRaw' se você quiser armazenar o texto original
}

export interface NovoComentarioRequest {
  versaoId: number;
  comentario: string;
  posicaoTexto?: string; // Este será nosso anchorId
  // Outros campos como 'selectionTextRaw'
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
  criarComentario(request: NovoComentarioRequest): Observable<Comentario> {
    return this.http.post<Comentario>(this.apiUrl, request);
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
   * @param comentarioPaiId ID do comentário a ser respondido
   * @param textoResposta Texto da resposta
   * @param versaoId Opcional, mas bom ter se a resposta também está ligada a uma versão
   */
  responderComentario(comentarioPaiId: number, textoResposta: string, versaoId: number): Observable<Comentario> {
    // O backend precisará lidar com a vinculação da resposta ao comentário pai
    const request: NovoComentarioRequest = {
      versaoId: versaoId, // A resposta também pertence à mesma versão do documento
      comentario: textoResposta,
      // posicaoTexto pode não ser aplicável para uma resposta direta, a menos que ela também ancore em algum lugar.
      // Se for uma resposta genérica à thread, não precisa de posicaoTexto.
      // Se a API suportar um campo 'comentarioPaiId', seria ideal.
    };
     // Ajuste o endpoint e o payload conforme a API do seu backend para respostas
    return this.http.post<Comentario>(`${this.apiUrl}/${comentarioPaiId}/responder`, { comentario: textoResposta });
  }

  /**
   * Marca um comentário como resolvido ou não resolvido
   * @param comentarioId ID do comentário
   * @param resolvido Estado de resolução
   */
  atualizarStatusResolvido(comentarioId: number, resolvido: boolean): Observable<Comentario> {
    return this.http.put<Comentario>(`${this.apiUrl}/${comentarioId}/resolver`, { resolvido });
  }
}