// src/app/core/services/versao.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
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

@Injectable({
  providedIn: 'root'
})
export class VersaoService {
  private apiUrl = `${environment.apiUrl}/versoes`;
  
  constructor(private http: HttpClient) {}
  
  getVersoes(monografiaId: number): Observable<Versao[]> {
    return this.http.get<Versao[]>(`${this.apiUrl}/monografia/${monografiaId}`);
  }
  
  getVersao(versaoId: number): Observable<Versao> {
    return this.http.get<Versao>(`${this.apiUrl}/${versaoId}`);
  }
  
  getConteudoVersao(versaoId: number): Observable<string> {
    return this.http.get(`${this.apiUrl}/${versaoId}/conteudo`, { responseType: 'text' });
  }
  
  criarVersao(novaVersao: NovaVersaoRequest): Observable<Versao> {
    return this.http.post<Versao>(`${this.apiUrl}`, novaVersao);
  }
  
  compararVersoes(versaoBaseId: number, versaoNovaId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/diff?versaoBaseId=${versaoBaseId}&versaoNovaId=${versaoNovaId}`);
  }
}