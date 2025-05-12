// src/app/shared/components/versao-diff-viewer/versao-diff-viewer.component.ts
import { Component, OnInit, Input, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MaterialModule } from '../../../material.module';
import { VersaoService, Versao } from '../../../core/services/versao.service';

@Component({
  selector: 'app-versao-diff-viewer',
  standalone: true,
  imports: [CommonModule, MaterialModule],
  encapsulation: ViewEncapsulation.None,
  template: `
    <div class="diff-container">
      <div class="diff-header">
        <h3>Comparação de Versões</h3>
        
        <div class="diff-versions">
          <div class="version-info">
            <span class="version-label">Base:</span>
            <span class="version-value">v{{ versaoBase?.numeroVersao }} ({{ versaoBase?.dataCriacao | date:'dd/MM/yyyy' }})</span>
          </div>
          
          <mat-icon>arrow_forward</mat-icon>
          
          <div class="version-info">
            <span class="version-label">Nova:</span>
            <span class="version-value">v{{ versaoNova?.numeroVersao }} ({{ versaoNova?.dataCriacao | date:'dd/MM/yyyy' }})</span>
          </div>
        </div>
        
        <div class="diff-actions">
          <button mat-button (click)="inverterComparacao()">
            <mat-icon>swap_horiz</mat-icon>
            Inverter comparação
          </button>
          
          <button mat-raised-button color="primary" (click)="fecharComparacao()">
            <mat-icon>close</mat-icon>
            Fechar
          </button>
        </div>
      </div>
      
      <div *ngIf="loading" class="diff-loading">
        <mat-spinner diameter="40"></mat-spinner>
        <p>Comparando versões...</p>
      </div>
      
      <div *ngIf="!loading" class="diff-content">
        <div class="diff-stats">
          <div class="stat-item added">
            <span class="stat-value">{{ diffStats.added }}</span>
            <span class="stat-label">Adições</span>
          </div>
          
          <div class="stat-item removed">
            <span class="stat-value">{{ diffStats.removed }}</span>
            <span class="stat-label">Remoções</span>
          </div>
          
          <div class="stat-item modified">
            <span class="stat-value">{{ diffStats.modified }}</span>
            <span class="stat-label">Modificações</span>
          </div>
        </div>
        
        <div class="diff-view" [innerHTML]="diffHtml"></div>
      </div>
    </div>
  `,
  styles: [`
    .diff-container {
      background-color: white;
      border-radius: 8px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      overflow: hidden;
      display: flex;
      flex-direction: column;
      height: 100%;
    }
    
    .diff-header {
      padding: 16px;
      background-color: #f5f8fc;
      border-bottom: 1px solid #e0e7f1;
    }
    
    .diff-header h3 {
      margin: 0 0 16px 0;
      font-size: 1.25rem;
      font-weight: 600;
      color: #2a3f75;
    }
    
    .diff-versions {
      display: flex;
      align-items: center;
      margin-bottom: 16px;
    }
    
    .diff-versions mat-icon {
      margin: 0 16px;
      color: #546e7a;
    }
    
    .version-info {
      display: flex;
      flex-direction: column;
    }
    
    .version-label {
      font-size: 0.8rem;
      color: #546e7a;
    }
    
    .version-value {
      font-size: 0.95rem;
      font-weight: 500;
      color: #2a3f75;
    }
    
    .diff-actions {
      display: flex;
      justify-content: flex-end;
      gap: 8px;
    }
    
    .diff-loading {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: #546e7a;
    }
    
    .diff-loading p {
      margin-top: 16px;
    }
    
    .diff-content {
      flex: 1;
      padding: 16px;
      overflow: auto;
    }
    
    .diff-stats {
      display: flex;
      gap: 16px;
      margin-bottom: 16px;
      padding: 16px;
      background-color: #f5f8fc;
      border-radius: 8px;
    }
    
    .stat-item {
      display: flex;
      flex-direction: column;
      align-items: center;
    }
    
    .stat-value {
      font-size: 1.5rem;
      font-weight: 700;
    }
    
    .stat-label {
      font-size: 0.8rem;
      color: #546e7a;
    }
    
    .stat-item.added .stat-value {
      color: #4caf50;
    }
    
    .stat-item.removed .stat-value {
      color: #f44336;
    }
    
    .stat-item.modified .stat-value {
      color: #ff9800;
    }
    
    .diff-view {
      font-family: "Consolas", "Monaco", monospace;
      line-height: 1.5;
      white-space: pre-wrap;
    }
    
    /* Estilos para o HTML de diferenças */
    .diff-add {
      background-color: #e8f5e9;
      color: #2e7d32;
      text-decoration: none;
    }
    
    .diff-remove {
      background-color: #ffebee;
      color: #c62828;
      text-decoration: line-through;
    }
    
    .diff-context {
      color: #546e7a;
    }
  `]
})
export class VersaoDiffViewerComponent implements OnInit {
  @Input() versaoBase?: Versao;
  @Input() versaoNova?: Versao;
  @Input() onClose: () => void = () => {};
  
  loading = false;
  diffHtml = '';
  diffStats = {
    added: 0,
    removed: 0,
    modified: 0
  };
  
  constructor(private versaoService: VersaoService) {}// Continuação do src/app/shared/components/versao-diff-viewer/versao-diff-viewer.component.ts
  ngOnInit(): void {
    if (this.versaoBase && this.versaoNova) {
      this.carregarDiferencas();
    }
  }
  
  carregarDiferencas(): void {
    if (!this.versaoBase || !this.versaoNova) {
      return;
    }
    
    this.loading = true;
    this.versaoService.compararVersoes(this.versaoBase.id, this.versaoNova.id).subscribe({
      next: (diff) => {
        this.diffHtml = this.processarDiferencas(diff);
        this.diffStats = {
          added: diff.added || 0,
          removed: diff.removed || 0,
          modified: diff.modified || 0
        };
        this.loading = false;
      },
      error: (err) => {
        console.error('Erro ao comparar versões:', err);
        this.loading = false;
      }
    });
  }
  
  processarDiferencas(diff: any): string {
    // Processamento simulado do HTML de diferenças
    // Em uma implementação real, processar a resposta do backend
    if (diff.htmlDiff) {
      return diff.htmlDiff;
    }
    
    // Implementação básica de visualização de diferenças
    let html = '';
    
    if (diff.diffs && Array.isArray(diff.diffs)) {
      for (const part of diff.diffs) {
        if (part.added) {
          html += `<span class="diff-add">${part.value}</span>`;
        } else if (part.removed) {
          html += `<span class="diff-remove">${part.value}</span>`;
        } else {
          html += `<span class="diff-context">${part.value}</span>`;
        }
      }
    }
    
    return html || 'Não há diferenças significativas entre as versões.';
  }
  
  inverterComparacao(): void {
    // Trocar versão base e nova
    const temp = this.versaoBase;
    this.versaoBase = this.versaoNova;
    this.versaoNova = temp;
    
    this.carregarDiferencas();
  }
  
  fecharComparacao(): void {
    if (this.onClose) {
      this.onClose();
    }
  }
}