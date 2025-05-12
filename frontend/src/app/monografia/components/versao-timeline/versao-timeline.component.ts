// src/app/monografia/components/versao-timeline/versao-timeline.component.ts
import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MaterialModule } from '../../../material.module';
import { VersaoService, Versao } from '../../../core/services/versao.service';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-versao-timeline',
  standalone: true,
  imports: [
    CommonModule,
    MaterialModule
  ],
  templateUrl: './versao-timeline.component.html',
  styleUrls: ['./versao-timeline.component.scss']
})
export class VersaoTimelineComponent implements OnInit {
  @Input() monografiaId!: number;
  @Output() versaoSelected = new EventEmitter<Versao>();
  @Output() versaoViewed = new EventEmitter<Versao>();
  @Output() versoesComparadas = new EventEmitter<{base: Versao, nova: Versao}>();
  
  versoes: Versao[] = [];
  selectedVersaoId?: number;
  loading: boolean = false;
  error: string | null = null;
  
  constructor(
    private versaoService: VersaoService,
    private snackBar: MatSnackBar
  ) {}
  
  ngOnInit(): void {
    this.carregarVersoes();
  }
  
  carregarVersoes(): void {
    this.loading = true;
    this.error = null;
    
    this.versaoService.getVersoes(this.monografiaId).subscribe({
      next: (versoes) => {
        // Ordenar versões por data de criação (mais recente primeiro)
        this.versoes = versoes.sort((a, b) => {
          return new Date(b.dataCriacao).getTime() - new Date(a.dataCriacao).getTime();
        });
        
        this.loading = false;
        
        if (this.versoes.length > 0 && !this.selectedVersaoId) {
          this.selectedVersaoId = this.versoes[0].id;
        }
      },
      error: (err) => {
        this.loading = false;
        this.error = `Erro ao carregar versões: ${err.error?.message || err.message || 'Erro desconhecido'}`;
        console.error('Erro ao carregar versões:', err);
      }
    });
  }
  
  selectVersao(versao: Versao): void {
    this.selectedVersaoId = versao.id;
    this.versaoSelected.emit(versao);
  }
  
  viewVersao(versao: Versao): void {
    this.versaoViewed.emit(versao);
  }
  
  compararVersoes(versaoBase: Versao, versaoNova: Versao): void {
    if (versaoBase.id === versaoNova.id) {
      this.snackBar.open('Não é possível comparar uma versão com ela mesma.', 'Fechar', {
        duration: 3000
      });
      return;
    }
    
    this.versoesComparadas.emit({ base: versaoBase, nova: versaoNova });
  }
  
  compararComAtual(versao: Versao): void {
    const versaoAtual = this.versoes.length > 0 ? this.versoes[0] : null;
    
    if (!versaoAtual) {
      this.snackBar.open('Não foi possível identificar a versão atual.', 'Fechar', {
        duration: 3000
      });
      return;
    }
    
    if (versaoAtual.id === versao.id) {
      this.snackBar.open('Esta é a versão atual.', 'Fechar', {
        duration: 3000
      });
      return;
    }
    
    this.compararVersoes(versaoAtual, versao);
  }
  
  formatarData(data: string): string {
    const date = new Date(data);
    return date.toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }
  
  formatarNumeroVersao(numeroVersao: string): string {
    return 'v' + numeroVersao;
  }
  
  calcularDiferencaTempo(data: string): string {
    const agora = new Date();
    const dataCriacao = new Date(data);
    const diffMs = agora.getTime() - dataCriacao.getTime();
    
    // Diferença em minutos
    const diffMinutos = Math.floor(diffMs / (1000 * 60));
    
    if (diffMinutos < 1) {
      return 'agora mesmo';
    }
    if (diffMinutos < 60) {
      return `há ${diffMinutos} ${diffMinutos === 1 ? 'minuto' : 'minutos'}`;
    }
    
    // Diferença em horas
    const diffHoras = Math.floor(diffMinutos / 60);
    if (diffHoras < 24) {
      return `há ${diffHoras} ${diffHoras === 1 ? 'hora' : 'horas'}`;
    }
    
    // Diferença em dias
    const diffDias = Math.floor(diffHoras / 24);
    if (diffDias < 30) {
      return `há ${diffDias} ${diffDias === 1 ? 'dia' : 'dias'}`;
    }
    
    // Diferença em meses
    const diffMeses = Math.floor(diffDias / 30);
    if (diffMeses < 12) {
      return `há ${diffMeses} ${diffMeses === 1 ? 'mês' : 'meses'}`;
    }
    
    // Diferença em anos
    const diffAnos = Math.floor(diffMeses / 12);
    return `há ${diffAnos} ${diffAnos === 1 ? 'ano' : 'anos'}`;
  }
  
  getTruncatedHash(hash: string): string {
    return hash.substring(0, 8);
  }
  
  isVersaoAtual(versao: Versao): boolean {
    return this.versoes.length > 0 && this.versoes[0].id === versao.id;
  }
}