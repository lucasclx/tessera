// src/app/shared/components/rich-text-editor/rich-text-editor.component.ts
import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MaterialModule } from '../../../material.module';

@Component({
  selector: 'app-rich-text-editor',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MaterialModule
  ],
  template: `
    <div class="editor-container">
      <div class="editor-toolbar">
        <button mat-icon-button (click)="execCommand('bold')" matTooltip="Negrito">
          <mat-icon>format_bold</mat-icon>
        </button>
        <button mat-icon-button (click)="execCommand('italic')" matTooltip="Itálico">
          <mat-icon>format_italic</mat-icon>
        </button>
        <button mat-icon-button (click)="execCommand('underline')" matTooltip="Sublinhado">
          <mat-icon>format_underline</mat-icon>
        </button>
        <span class="toolbar-divider"></span>
        
        <button mat-icon-button [matMenuTriggerFor]="headingMenu" matTooltip="Título">
          <mat-icon>title</mat-icon>
        </button>
        <mat-menu #headingMenu="matMenu">
          <button mat-menu-item (click)="formatBlock('h1')">Título 1</button>
          <button mat-menu-item (click)="formatBlock('h2')">Título 2</button>
          <button mat-menu-item (click)="formatBlock('h3')">Título 3</button>
          <button mat-menu-item (click)="formatBlock('h4')">Título 4</button>
        </mat-menu>
        
        <button mat-icon-button (click)="execCommand('justifyLeft')" matTooltip="Alinhar à esquerda">
          <mat-icon>format_align_left</mat-icon>
        </button>
        <button mat-icon-button (click)="execCommand('justifyCenter')" matTooltip="Centralizar">
          <mat-icon>format_align_center</mat-icon>
        </button>
        <button mat-icon-button (click)="execCommand('justifyRight')" matTooltip="Alinhar à direita">
          <mat-icon>format_align_right</mat-icon>
        </button>
        <span class="toolbar-divider"></span>
        
        <button mat-icon-button (click)="execCommand('insertUnorderedList')" matTooltip="Lista com marcadores">
          <mat-icon>format_list_bulleted</mat-icon>
        </button>
        <button mat-icon-button (click)="execCommand('insertOrderedList')" matTooltip="Lista numerada">
          <mat-icon>format_list_numbered</mat-icon>
        </button>
        <span class="toolbar-divider"></span>
        
        <button mat-icon-button (click)="addComment()" matTooltip="Adicionar comentário">
          <mat-icon>comment</mat-icon>
        </button>
        <button mat-icon-button (click)="insertImage()" matTooltip="Inserir imagem">
          <mat-icon>image</mat-icon>
        </button>
        <button mat-icon-button (click)="insertTable()" matTooltip="Inserir tabela">
          <mat-icon>table_chart</mat-icon>
        </button>
        
        <span class="flex-spacer"></span>
        
        <button mat-stroked-button color="primary" (click)="saveDocument()" [disabled]="saving">
          <mat-icon>save</mat-icon>
          <span>{{ saving ? 'Salvando...' : 'Salvar' }}</span>
        </button>
      </div>
      
      <div class="editor-content-container">
        <div
          #editorContent
          class="editor-content"
          contenteditable="true"
          [innerHTML]="content"
          (input)="onContentChange($event)"
          (blur)="onBlur()"
        ></div>
      </div>
      
      <div *ngIf="showCommentPanel" class="comment-panel">
        <h3>Comentários</h3>
        <div class="comments-list">
          <div *ngFor="let comment of comments" class="comment-item">
            <div class="comment-header">
              <span class="comment-author">{{ comment.author }}</span>
              <span class="comment-date">{{ comment.date | date:'dd/MM/yyyy HH:mm' }}</span>
            </div>
            <div class="comment-text">{{ comment.text }}</div>
          </div>
        </div>
        <div class="add-comment">
          <mat-form-field appearance="outline" class="full-width">
            <textarea matInput [(ngModel)]="newComment" placeholder="Adicione um comentário..."></textarea>
          </mat-form-field>
          <button mat-raised-button color="primary" (click)="submitComment()">Comentar</button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .editor-container {
      display: flex;
      flex-direction: column;
      border: 1px solid #e0e7f1;
      border-radius: 8px;
      overflow: hidden;
      background-color: white;
      height: 100%;
    }
    
    .editor-toolbar {
      display: flex;
      align-items: center;
      padding: 8px 16px;
      border-bottom: 1px solid #e0e7f1;
      background-color: #f5f8fc;
      flex-wrap: wrap;
    }
    
    .toolbar-divider {
      height: 24px;
      width: 1px;
      background-color: #e0e7f1;
      margin: 0 8px;
    }
    
    .flex-spacer {
      flex: 1;
    }
    
    .editor-content-container {
      flex: 1;
      overflow: auto;
      position: relative;
    }
    
    .editor-content {
      min-height: 300px;
      padding: 16px;
      outline: none;
      line-height: 1.6;
    }
    
    .comment-panel {
      width: 300px;
      border-left: 1px solid #e0e7f1;
      padding: 16px;
      background-color: #f5f8fc;
      overflow-y: auto;
    }
    
    .comment-item {
      padding: 12px;
      background-color: white;
      border-radius: 8px;
      margin-bottom: 12px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.05);
    }
    
    .comment-header {
      display: flex;
      justify-content: space-between;
      margin-bottom: 8px;
    }
    
    .comment-author {
      font-weight: 600;
      color: #2a3f75;
    }
    
    .comment-date {
      font-size: 0.8rem;
      color: #546e7a;
    }
    
    .comment-text {
      font-size: 0.95rem;
      line-height: 1.5;
    }
    
    .add-comment {
      margin-top: 16px;
    }
    
    .full-width {
      width: 100%;
    }
  `]
})
export class RichTextEditorComponent implements OnInit {
  @Input() content: string = '';
  @Input() monografiaId!: number;
  @Input() versaoId?: number;
  @Output() contentChange = new EventEmitter<string>();
  @Output() saved = new EventEmitter<any>();
  
  comments: any[] = [];
  newComment: string = '';
  showCommentPanel: boolean = false;
  saving: boolean = false;
  
  constructor() {}
  
  ngOnInit(): void {
    // Carregar comentários se versaoId estiver presente
    if (this.versaoId) {
      this.loadComments();
    }
  }
  
  execCommand(command: string, value: string = ''): void {
    document.execCommand(command, false, value);
  }
  
  formatBlock(block: string): void {
    document.execCommand('formatBlock', false, `<${block}>`);
  }
  
  onContentChange(event: any): void {
    this.content = event.target.innerHTML;
    this.contentChange.emit(this.content);
  }
  
  onBlur(): void {
    // Opcional: salvar automaticamente quando o editor perder o foco
  }
  
  addComment(): void {
    this.showCommentPanel = !this.showCommentPanel;
  }
  
  insertImage(): void {
    const url = prompt('Insira a URL da imagem:');
    if (url) {
      this.execCommand('insertImage', url);
    }
  }
  
  insertTable(): void {
    const rows = prompt('Número de linhas:', '3');
    const cols = prompt('Número de colunas:', '3');
    
    if (rows && cols) {
      const table = document.createElement('table');
      table.border = '1';
      table.style.width = '100%';
      table.style.borderCollapse = 'collapse';
      
      for (let i = 0; i < parseInt(rows); i++) {
        const tr = table.insertRow();
        for (let j = 0; j < parseInt(cols); j++) {
          const td = tr.insertCell();
          td.innerHTML = 'Célula';
          td.style.padding = '8px';
          td.style.border = '1px solid #ddd';
        }
      }
      
      document.execCommand('insertHTML', false, table.outerHTML);
    }
  }
  
  loadComments(): void {
    // Simulação - na implementação real, buscar do backend
    this.comments = [
      {
        id: 1,
        author: 'Professor Silva',
        date: new Date(),
        text: 'Por favor, revise esta seção e adicione mais referências.'
      },
      {
        id: 2,
        author: 'João Aluno',
        date: new Date(Date.now() - 86400000), // 1 dia atrás
        text: 'Corrigi conforme sugerido.'
      }
    ];
  }
  
  submitComment(): void {
    if (this.newComment.trim()) {
      const comment = {
        id: this.comments.length + 1,
        author: 'Você', // Na implementação real, usar o usuário logado
        date: new Date(),
        text: this.newComment
      };
      
      this.comments.unshift(comment);
      this.newComment = '';
      
      // Na implementação real, salvar no backend
    }
  }
  
  saveDocument(): void {
    this.saving = true;
    
    // Simulação de salvar - na implementação real, enviar para o backend
    setTimeout(() => {
      this.saved.emit({
        content: this.content,
        monografiaId: this.monografiaId,
        versaoId: this.versaoId
      });
      
      this.saving = false;
    }, 1000);
  }
}