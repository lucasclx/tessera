// src/app/monografia/pages/editor-monografia/editor-monografia.component.ts
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MaterialModule } from '../../../material.module';
import { RichTextEditorComponent } from '../../../shared/components/rich-text-editor/rich-text-editor.component';
import { VersaoTimelineComponent } from '../../../shared/components/versao-timeline/versao-timeline.component';
import { VersaoDiffViewerComponent } from '../../../shared/components/versao-diff-viewer/versao-diff-viewer.component';
import { VersaoService, Versao } from '../../../core/services/versao.service';

@Component({
  selector: 'app-editor-monografia',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MaterialModule,
    RichTextEditorComponent,
    VersaoTimelineComponent,
    VersaoDiffViewerComponent
  ],
  template: `
    <div class="editor-page">
      <div class="editor-header">
        <div class="header-content">
          <div *ngIf="!editingTitle" class="title-display">
            <h1>{{ monografia?.titulo || 'Carregando...' }}</h1>
            <button mat-icon-button (click)="startEditTitle()" *ngIf="monografia">
              <mat-icon>edit</mat-icon>
            </button>
          </div>
          
          <div *ngIf="editingTitle" class="title-edit">
            <mat-form-field appearance="outline">
              <input matInput [formControl]="titleControl" placeholder="Título da Monografia">
            </mat-form-field>
            <button mat-icon-button color="primary" (click)="saveTitle()">
              <mat-icon>check</mat-icon>
            </button>
            <button mat-icon-button (click)="cancelEditTitle()">
              <mat-icon>close</mat-icon>
            </button>
          </div>
          
          <div class="editor-actions">
            <button mat-button (click)="toggleTimeline()">
              <mat-icon>history</mat-icon>
              {{ showTimeline ? 'Ocultar histórico' : 'Ver histórico' }}
            </button>
            
            <button mat-raised-button color="primary" (click)="openSaveDialog()">
              <mat-icon>save</mat-icon>
              Salvar nova versão
            </button>
          </div>
        </div>
      </div>
      
      <div class="editor-content">
        <div class="main-content" [class.with-timeline]="showTimeline">
          <app-rich-text-editor 
            [content]="documentContent" 
            [monografiaId]="monografiaId"
            [versaoId]="currentVersaoId"
            (contentChange)="onContentChange($event)"
            (saved)="onSaved($event)">
          </app-rich-text-editor>
        </div>
        
        <div *ngIf="showTimeline" class="timeline-panel">
          <app-versao-timeline 
            [monografiaId]="monografiaId"
            (versaoSelected)="loadVersao($event)"
            (versaoViewed)="previewVersao($event)"
            (versoesComparadas)="compareVersoes($event)">
          </app-versao-timeline>
        </div>
      </div>
      
      <div *ngIf="showDiff" class="diff-overlay">
        <app-versao-diff-viewer
          [versaoBase]="diffBaseVersao"
          [versaoNova]="diffNovaVersao"
          [onClose]="closeDiff.bind(this)">
        </app-versao-diff-viewer>
      </div>
    </div>
    
    <!-- Diálogo para salvar nova versão -->
    <ng-template #saveVersionDialog>
      <h2 mat-dialog-title>Salvar Nova Versão</h2>
      <div mat-dialog-content>
        <form [formGroup]="saveForm">
          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Mensagem da versão</mat-label>
            <textarea matInput formControlName="mensagemCommit" placeholder="Descreva as alterações realizadas..." rows="4"></textarea>
            <mat-error *ngIf="saveForm.get('mensagemCommit')?.hasError('required')">
              Mensagem é obrigatória
            </mat-error>
          </mat-form-field>
          
          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Tag (opcional)</mat-label>
            <input matInput formControlName="tag" placeholder="Ex: Rascunho, Final, Revisão">
            <mat-hint>Adicione uma tag para identificar versões importantes</mat-hint>
          </mat-form-field>
        </form>
      </div>
      <div mat-dialog-actions align="end">
        <button mat-button mat-dialog-close>Cancelar</button>
        <button mat-raised-button color="primary" (click)="saveVersion()" [disabled]="saveForm.invalid || savingVersion">
          <mat-icon>save</mat-icon>
          {{ savingVersion ? 'Salvando...' : 'Salvar Versão' }}
        </button>
      </div>
    </ng-template>
  `,
  styles: [`
    .editor-page {
      display: flex;
      flex-direction: column;
      height: 100vh;
      background-color: #f5f8fc;
    }
    
    .editor-header {
      background-color: white;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
      padding: 16px;
      z-index: 10;
    }
    
    .header-content {
      display: flex;
      justify-content: space-between;
      align-items: center;
      max-width: 1600px;
      margin: 0 auto;
      width: 100%;
    }
    
    .title-display {
      display: flex;
      align-items: center;
      
      h1 {
        margin: 0;
        font-size: 1.5rem;
        color: #2a3f75;
      }
      
      button {
        margin-left: 8px;
      }
    }
    
    .title-edit {
      display: flex;
      align-items: center;
      
      mat-form-field {
        width: 400px;
        margin-right: 8px;
      }
    }
    
    .editor-actions {
      display: flex;
      gap: 8px;
    }
    
    .editor-content {
      display: flex;
      flex: 1;
      overflow: hidden;
    }
    
    .main-content {
      flex: 1;
      padding: 20px;
      height: 100%;
      overflow: auto;
      transition: all 0.3s ease;
      
      &.with-timeline {
        flex: 0.7;
      }
    }
    
    .timeline-panel {
      flex: 0.3;
      min-width: 300px;
      max-width: 400px;
      padding: 20px;
      height: 100%;
      overflow: auto;
      background-color: white;
      box-shadow: -2px 0 5px rgba(0, 0, 0, 0.05);
    }
    
    .diff-overlay {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background-color: rgba(0, 0, 0, 0.5);
      z-index: 1000;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 40px;
    }
    
    .full-width {
      width: 100%;
    }
  `]
})
export class EditorMonografiaComponent implements OnInit {
  monografiaId!: number;
  monografia: any;
  documentContent: string = '';
  showTimeline: boolean = false;
  titleControl = this.fb.control('', Validators.required);
  editingTitle: boolean = false;
  currentVersaoId?: number;
  
  // Para comparação de versões
  showDiff: boolean = false;
  diffBaseVersao?: Versao;
  diffNovaVersao?: Versao;
  
  // Para salvar nova versão
  saveForm: FormGroup;
  savingVersion: boolean = false;
  
  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private fb: FormBuilder,
    private versaoService: VersaoService,
    private dialog: MatDialog
  ) {
    this.saveForm = this.fb.group({
      mensagemCommit: ['', Validators.required],
      tag: ['']
    });
  }
  
  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.monografiaId = +params['id'];
      this.carregarMonografia();
    });
  }
  
  carregarMonografia(): void {
    // Na implementação real, chamar o serviço de monografia
    // Simulação para este exemplo
    setTimeout(() => {
      this.monografia = {
        id: this.monografiaId,
        titulo: 'Análise de Sistemas Distribuídos em Ambientes Corporativos',
        autorPrincipal: { id: 1, nome: 'João Silva' },
        orientadorPrincipal: { id: 2, nome: 'Prof. Carlos Mendes' }
      };
      
      this.titleControl.setValue(this.monografia.titulo);
      
      // Carregar a versão mais recente como padrão
      this.carregarUltimaVersao();
    }, 500);
  }
  
  carregarUltimaVersao(): void {
    this.versaoService.getVersoes(this.monografiaId).subscribe({
      next: (versoes) => {
        if (versoes.length > 0) {
          // Ordenar por data de criação, mais recente primeiro
          versoes.sort((a, b) => {
            return new Date(b.dataCriacao).getTime() - new Date(a.dataCriacao).getTime();
          });
          
          const ultimaVersao = versoes[0];
          this.loadVersao(ultimaVersao);
        } else {
          // Nenhuma versão encontrada, começar com documento vazio
          this.documentContent = '';
        }
      },
      error: (err) => {
        console.error('Erro ao carregar versões:', err);
      }
    });
  }
  
  loadVersao(versao: Versao): void {
    this.currentVersaoId = versao.id;
    this.versaoService.getConteudoVersao(versao.id).subscribe({
      next: (conteudo) => {
        this.documentContent = conteudo;
      },
      error: (err) => {
        console.error('Erro ao carregar conteúdo da versão:', err);
      }
    });
  }
  
  onContentChange(content: string): void {
    this.documentContent = content;
  }
  
  onSaved(data: any): void {
    // Implementar lógica para salvar nos componentes filhos
    this.openSaveDialog();
  }
  
  toggleTimeline(): void {
    this.showTimeline = !this.showTimeline;
  }
  
  startEditTitle(): void {
    this.editingTitle = true;
  }
  
  saveTitle(): void {
    if (this.titleControl.valid && this.monografia) {
      // Salvar o título no backend
      const novoTitulo = this.titleControl.value;
      
      // Na implementação real, chamar o serviço de monografia
      // Simulação para este exemplo
      this.monografia.titulo = novoTitulo;
      this.editingTitle = false;
    }
  }
  
  cancelEditTitle(): void {
    this.titleControl.setValue(this.monografia.titulo);
    this.editingTitle = false;
  }
  
  previewVersao(versao: Versao): void {
    // Implementar visualização completa da versão
    this.loadVersao(versao);
  }
  
  compareVersoes(data: {base: Versao, nova: Versao}): void {
    this.diffBaseVersao = data.base;
    this.diffNovaVersao = data.nova;
    this.showDiff = true;
  }
  
  closeDiff(): void {
    this.showDiff = false;
  }
  
  openSaveDialog(): void {
    const dialogRef = this.dialog.open(this.saveVersionDialog, {
      width: '500px'
    });
  }
  
  saveVersion(): void {
    if (this.saveForm.invalid) {
      return;
    }
    
    this.savingVersion = true;
    
    const novaVersao = {
      monografiaId: this.monografiaId,
      conteudo: this.documentContent,
      mensagemCommit: this.saveForm.get('mensagemCommit')?.value,
      tag: this.saveForm.get('tag')?.value
    };
    
    this.versaoService.criarVersao(novaVersao).subscribe({
      next: (versao) => {
        this.savingVersion = false;
        this.dialog.closeAll();
        
        // Atualizar a lista de versões e selecionar a nova versão
        if (this.showTimeline) {
          // Recarregar a timeline
          const timelineComponent = this.timelineComponent;
          if (timelineComponent) {
            timelineComponent.carregarVersoes();
          }
        }
        
        // Feedback de sucesso
        this.snackBar.open('Versão salva com sucesso!', 'Fechar', {
          duration: 3000
        });
      },
      error: (err) => {
        console.error('Erro ao salvar versão:', err);
        this.savingVersion = false;
        
        // Feedback de erro
        this.snackBar.open('Erro ao salvar versão. Tente novamente.', 'Fechar', {
          duration: 5000
        });
      }
    });
  }
}