// src/app/shared/components/versao-timeline/versao-timeline.component.ts
import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Versao, VersaoService } from '../../../core/services/versao.service';
import { MaterialModule } from '../../../material.module';

@Component({
  selector: 'app-versao-timeline',
  standalone: true,
  imports: [CommonModule, MaterialModule],
  template: `
    <div class="timeline-container">
      <h3 class="timeline-header">Histórico de Versões</h3>
      
      <div *ngIf="loading" class="loading-state">
        <mat-spinner diameter="30"></mat-spinner>
        <span>Carregando versões...</span>
      </div>
      
      <div *ngIf="!loading && versoes.length === 0" class="empty-state">
        <mat-icon>history</mat-icon>
        <p>Nenhuma versão encontrada</p>
      </div>
      
      <div *ngIf="!loading && versoes.length > 0" class="timeline">
        <div *ngFor="let versao of versoes; let i = index" class="timeline-item" 
             [class.active]="selectedVersaoId === versao.id"
             (click)="selectVersao(versao)">
          <div class="timeline-badge">
            <mat-icon *ngIf="i === 0">new_releases</mat-icon>
            <span *ngIf="i > 0">{{ versoes.length - i }}</span>
          </div>
          
          <div class="timeline-content">
            <div class="timeline-header">
              <div class="version-info">
                <span class="version-number">v{{ versao.numeroVersao }}</span>
                <span *ngIf="versao.tag" class="version-tag">{{ versao.tag }}</span>
              </div>
              <span class="version-date">{{ versao.dataCriacao | date:'dd/MM/yyyy HH:mm' }}</span>
            </div>
            
            <p class="commit-message">{{ versao.mensagemCommit }}</p>
            
            <div class="version-meta">
              <span class="author">
                <mat-icon>person</mat-icon>
                {{ versao.criadoPor.nome }}
              </span>
              <span class="hash">
                <mat-icon>fingerprint</mat-icon>
                {{ versao.hashArquivo.substring(0, 8) }}
              </span>
            </div>
            
            <div class="timeline-actions">
              <button mat-button color="primary" (click)="viewVersao(versao); $event.stopPropagation()">
                <mat-icon>visibility</mat-icon>
                Visualizar
              </button>
              
              <button *ngIf="i > 0" mat-button color="accent" 
                      (click)="comparar(versoes[0], versao); $event.stopPropagation()">
                <mat-icon>compare_arrows</mat-icon>
                Comparar com atual
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .timeline-container {
      padding: 16px;
      background-color: white;
      border-radius: 8px;
      box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
    }
    
    .timeline-header {
      font-size: 1.25rem;
      font-weight: 600;
      color: #2a3f75;
      margin-bottom: 20px;
    }
    
    .loading-state, .empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 40px 0;
      color: #546e7a;
    }
    
    .empty-state mat-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
      margin-bottom: 16px;
      opacity: 0.5;
    }
    
    .timeline {
      position: relative;
      padding-left: 40px;
    }
    
    .timeline::before {
      content: '';
      position: absolute;
      top: 0;
      bottom: 0;
      left: 15px;
      width: 2px;
      background-color: #e0e7f1;
    }
    
    .timeline-item {
      position: relative;
      margin-bottom: 24px;
      cursor: pointer;
    }
    
    .timeline-item:last-child {
      margin-bottom: 0;
    }
    
    .timeline-item.active .timeline-content {
      background-color: #f5f8fc;
      border-color: #4c6ecc;
    }
    
    .timeline-badge {
      position: absolute;
      left: -40px;
      width: 30px;
      height: 30px;
      border-radius: 50%;
      background-color: #4c6ecc;
      color: white;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: 600;
      z-index: 1;
    }
    
    .timeline-content {
      padding: 16px;
      border: 1px solid #e0e7f1;
      border-radius: 8px;
      transition: all 0.2s ease;
    }
    
    .timeline-content:hover {
      border-color: #4c6ecc;
      box-shadow: 0 3px 8px rgba(0, 0, 0, 0.08);
    }
    
    .version-info {
      display: flex;
      align-items: center;
    }
    
    .version-number {
      font-weight: 600;
      color: #2a3f75;
    }
    
    .version-tag {
      margin-left: 8px;
      padding: 2px 8px;
      background-color: #e8f5e9;
      color: #2e7d32;
      border-radius: 12px;
      font-size: 0.8rem;
      font-weight: 500;
    }
    
    .timeline-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;
    }
    
    .version-date {
      font-size: 0.8rem;
      color: #546e7a;
    }
    
    .commit-message {
      margin: 0 0 12px 0;
      font-size: 0.95rem;
    }
    
    .version-meta {
      display: flex;
      font-size: 0.85rem;
      color: #546e7a;
      margin-bottom: 12px;
    }
    
    .author, .hash {
      display: flex;
      align-items: center;
      margin-right: 16px;
    }
    
    .author mat-icon, .hash mat-icon {
      font-size: 16px;
      width: 16px;
      height: 16px;
      margin-right: 4px;
    }
    
    .timeline-actions {
      display: flex;
      gap: 8px;
    }
  `]
})
export class VersaoTimelineComponent implements OnInit {
  @Input() monografiaId!: number;
  @Output() versaoSelected = new EventEmitter<Versao>();
  @Output() versaoViewed = new EventEmitter<Versao>();
  @Output() versoesComparadas = new EventEmitter<{base: Versao, nova: Versao}>();
  
  versoes: Versao[] = [];
  loading = false;
  selectedVersaoId?: number;
  
  constructor(private versaoService: VersaoService) {}
  
  ngOnInit(): void {
    this.carregarVersoes();
  }
  
  carregarVersoes(): void {
    this.loading = true;
    this.versaoService.getVersoes(this.monografiaId).subscribe({
      next: (versoes) => {
        this.versoes = versoes.sort((a, b) => {
          // Ordenar por data de criação, mais recente primeiro
          return new Date(b.dataCriacao).getTime() - new Date(a.dataCriacao).getTime();
        });
        this.loading = false;
      },
      error: (err) => {
        console.error('Erro ao carregar versões:', err);
        this.loading = false;
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
  
  comparar(versaoBase: Versao, versaoNova: Versao): void {
    this.versoesComparadas.emit({ base: versaoBase, nova: versaoNova });
  }
}