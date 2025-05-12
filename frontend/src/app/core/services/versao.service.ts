// src/app/core/services/versao.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Versao {
  id: number;
  numeroVersao: string;
  hashArquivo: string;
  nomeArquivo: string;
  mensagemCommit: string;
  tag?: string;
  criadoPor: any;
  dataCriacao: string;
  caminhoArquivo: string;
  tamanhoArquivo: number;
}

export interface NovaVersaoRequest {
  monografiaId: number;
  conteudo: string;
  mensagemCommit: string;
  tag?: string;
}

export interface DiffResponse {
  versaoBase: Versao;
  versaoNova: Versao;
  diffs: any[];
  htmlDiff: string;
  added: number;
  removed: number;
  modified: number;
}

@Injectable({
  providedIn: 'root'
})
export class VersaoService {
  private apiUrl = `${environment.apiUrl}/versoes`;
  
  constructor(private http: HttpClient) {}
  
  /**
   * Obtém todas as versões de uma monografia
   * @param monografiaId ID da monografia
   */
  getVersoes(monografiaId: number): Observable<Versao[]> {
    return this.http.get<Versao[]>(`${this.apiUrl}/monografia/${monografiaId}`);
  }
  
  /**
   * Obtém detalhes de uma versão específica
   * @param versaoId ID da versão
   */
  getVersao(versaoId: number): Observable<Versao> {
    return this.http.get<Versao>(`${this.apiUrl}/${versaoId}`);
  }
  
  /**
   * Obtém o conteúdo de uma versão específica
   * @param versaoId ID da versão
   */
  getConteudoVersao(versaoId: number): Observable<string> {
    return this.http.get(`${this.apiUrl}/${versaoId}/conteudo`, { responseType: 'text' });
  }
  
  /**
   * Cria uma nova versão
   * @param novaVersao Dados da nova versão
   */
  criarVersao(novaVersao: NovaVersaoRequest): Observable<Versao> {
    return this.http.post<Versao>(`${this.apiUrl}`, novaVersao);
  }
  
  /**
   * Compara duas versões e retorna as diferenças
   * @param versaoBaseId ID da versão base
   * @param versaoNovaId ID da versão nova
   */
  compararVersoes(versaoBaseId: number, versaoNovaId: number): Observable<DiffResponse> {
    const params = new HttpParams()
      .set('versaoBaseId', versaoBaseId.toString())
      .set('versaoNovaId', versaoNovaId.toString());
    
    return this.http.get<DiffResponse>(`${this.apiUrl}/diff`, { params });
  }
}