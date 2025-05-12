// src/app/shared/components/rich-text-editor/rich-text-editor.component.ts
import {
    Component, OnInit, Input, Output, EventEmitter,
    ViewChild, ElementRef, AfterViewInit, Renderer2, OnChanges, SimpleChanges
  } from '@angular/core';
  import { CommonModule } from '@angular/common';
  import { FormsModule } from '@angular/forms';
  import { MaterialModule } from '../../../material.module';
  import { MatSnackBar } from '@angular/material/snack-bar';
  import { Comentario, ComentarioService, NovoComentarioRequest } from '../../../core/services/comentario.service'; // Ajuste o caminho se necessário
  
  interface EditorComment extends Comentario {
    anchorId?: string; // Para vincular ao span no editor
  }
  
  @Component({
    selector: 'app-rich-text-editor',
    standalone: true,
    imports: [
      CommonModule,
      FormsModule,
      MaterialModule
    ],
    templateUrl: './rich-text-editor.component.html',
    styleUrls: ['./rich-text-editor.component.scss']
  })
  export class RichTextEditorComponent implements OnInit, AfterViewInit, OnChanges {
    @Input() content: string = '';
    @Input() monografiaId!: number;
    @Input() versaoId?: number;
    @Input() readOnly: boolean = false;
    @Input() existingComments: Comentario[] = []; // Receber comentários do pai
  
    @Output() contentChange = new EventEmitter<string>();
    @Output() saved = new EventEmitter<any>();
    @Output() newCommentAnchorRequested = new EventEmitter<{ anchorId: string, selectionText: string }>();
    @Output() viewCommentThread = new EventEmitter<string>(); // Emite o anchorId do comentário a ser visualizado
  
    @ViewChild('editorContent') editorElement!: ElementRef<HTMLDivElement>;
  
    // Não mais gerenciamos 'comments' internamente para adição,
    // o painel de comentários do editor-monografia fará isso.
    // Esta lista será usada para destacar trechos comentados.
    commentsToHighlight: EditorComment[] = [];
  
    newCommentTextForSelectedAnchor: string = ''; // Texto para um novo comentário (se o painel for interno)
    showInternalCommentPanel: boolean = false; // Controla um painel de comentário (se for interno)
    currentAnchorIdForNewComment: string | null = null;
    currentSelectionTextForNewComment: string | null = null;
  
    saving: boolean = false;
    private lastSelection: Range | null = null;
  
    constructor(
      private snackBar: MatSnackBar,
      private renderer: Renderer2,
      private comentarioService: ComentarioService // Para salvar diretamente se for o caso
    ) {}
  
    ngOnInit(): void {
      this.processExistingComments();
    }
  
    ngAfterViewInit(): void {
      if (this.editorElement) {
        this.editorElement.nativeElement.contentEditable = this.readOnly ? 'false' : 'true';
        this.updateEditorContent(this.content); // Garante que o conteúdo inicial seja renderizado e processado
      }
      document.addEventListener('selectionchange', this.handleSelectionChange);
    }
  
    ngOnChanges(changes: SimpleChanges): void {
      if (changes['content']) {
        this.updateEditorContent(changes['content'].currentValue);
      }
      if (changes['existingComments']) {
        this.processExistingComments();
        this.highlightCommentedAnchors();
      }
       if (changes['readOnly'] && this.editorElement) {
          this.editorElement.nativeElement.contentEditable = this.readOnly ? 'false' : 'true';
      }
    }
  
    private processExistingComments(): void {
      this.commentsToHighlight = this.existingComments.map(comment => ({
        ...comment,
        // Assumindo que comentario.posicaoTexto armazena o anchorId
        anchorId: comment.posicaoTexto 
      }));
    }
  
    private updateEditorContent(newContent: string): void {
      if (this.editorElement && this.editorElement.nativeElement.innerHTML !== newContent) {
        this.editorElement.nativeElement.innerHTML = newContent;
        this.highlightCommentedAnchors();
      }
    }
  
    // Modificado para não ser arrow function para poder remover com removeEventListener
    private handleSelectionChange = (): void => {
      if (document.activeElement === this.editorElement?.nativeElement) {
        const sel = window.getSelection();
        if (sel && sel.rangeCount > 0) {
          this.lastSelection = sel.getRangeAt(0).cloneRange();
        }
      }
    }
  
    ngOnDestroy(): void {
      document.removeEventListener('selectionchange', this.handleSelectionChange);
    }
  
  
    execCommand(command: string, value: string = ''): void {
      if (this.readOnly) {
        this.snackBar.open('O editor está em modo de leitura.', 'Fechar', { duration: 3000 });
        return;
      }
      // Restaurar seleção antes de executar o comando
      this.restoreSelection();
      document.execCommand(command, false, value);
      this.editorElement.nativeElement.focus(); // Manter o foco
      this.onContentChanged();
    }
  
    formatBlock(block: string): void {
      if (this.readOnly) {
        this.snackBar.open('O editor está em modo de leitura.', 'Fechar', { duration: 3000 });
        return;
      }
      this.restoreSelection();
      document.execCommand('formatBlock', false, `<${block}>`);
      this.editorElement.nativeElement.focus();
      this.onContentChanged();
    }
  
    onContentChanged(): void {
      if (this.editorElement) {
        this.content = this.editorElement.nativeElement.innerHTML;
        this.contentChange.emit(this.content);
        this.highlightCommentedAnchors(); // Re-aplicar destaques após mudança
      }
    }
  
    private restoreSelection(): void {
      if (this.lastSelection) {
        const sel = window.getSelection();
        if (sel) {
          sel.removeAllRanges();
          sel.addRange(this.lastSelection);
        }
      } else {
        // Se não houver seleção salva, focar no editor para permitir comandos
        this.editorElement?.nativeElement.focus();
      }
    }
  
    private generateUniqueId(): string {
      return `comment-anchor-${Date.now()}-${Math.random().toString(36).substring(2, 9)}`;
    }
  
    requestNewComment(): void {
      if (this.readOnly) {
        this.snackBar.open('Não é possível adicionar comentários no modo de leitura.', 'Fechar', { duration: 3000 });
        return;
      }
  
      this.restoreSelection();
      const selection = window.getSelection();
      const selectionText = selection?.toString().trim();
  
      if (selection && selection.rangeCount > 0 && selectionText) {
        const range = selection.getRangeAt(0);
        if (!range.collapsed) {
          const anchorId = this.generateUniqueId();
          const span = this.renderer.createElement('span');
          this.renderer.setAttribute(span, 'data-comment-anchor-id', anchorId);
          this.renderer.addClass(span, 'comment-highlight');
          // Tentar envolver o conteúdo do range com o span
          // Cuidado: range.surroundContents(span) pode falhar se o range cruzar limites de nós não textuais
          try {
            // Se a seleção for apenas texto, podemos fazer de forma mais simples
            // Caso contrário, surroundContents é melhor mas mais propenso a erro.
            // Para simplificar e maior robustez com execCommand:
            document.execCommand('insertHTML', false, `<span class="comment-highlight" data-comment-anchor-id="${anchorId}">${selectionText}</span>`);
            this.onContentChanged(); // Atualiza o conteúdo e re-aplica destaques
            
            this.newCommentAnchorRequested.emit({ anchorId, selectionText });
            this.snackBar.open(`Seleção marcada para comentário. ID: ${anchorId}`, 'Fechar', { duration: 2000 });
  
          } catch (e) {
            console.error('Erro ao envolver seleção com span:', e);
            this.snackBar.open('Erro ao marcar texto para comentário. Tente selecionar apenas texto simples.', 'Fechar', { duration: 4000 });
          }
        } else {
          this.snackBar.open('Selecione um trecho de texto para comentar.', 'Fechar', { duration: 3000 });
        }
      } else {
        this.snackBar.open('Selecione um trecho de texto para comentar.', 'Fechar', { duration: 3000 });
      }
    }
    
    private highlightCommentedAnchors(): void {
      if (!this.editorElement || !this.editorElement.nativeElement) return;
  
      // Remover destaques antigos para evitar duplicação se o conteúdo for atualizado
      const oldHighlights = this.editorElement.nativeElement.querySelectorAll('span.comment-highlight[data-comment-anchor-id]');
      oldHighlights.forEach(node => {
        // Se o nó ainda não é um link, apenas remove a classe de destaque se não for mais um comentário válido.
        // Se já for um link, não mexemos aqui, o addEventListener cuida disso.
        // A lógica atual é: se está em commentsToHighlight, deve ser um link.
      });
  
  
      this.commentsToHighlight.forEach(comment => {
        if (comment.anchorId) {
          const anchorElement = this.editorElement.nativeElement.querySelector(`span[data-comment-anchor-id="${comment.anchorId}"]`) as HTMLElement;
          if (anchorElement) {
            this.renderer.addClass(anchorElement, 'comment-highlight'); // Garante a classe
            this.renderer.addClass(anchorElement, 'has-comment'); // Nova classe para diferenciar
            this.renderer.setStyle(anchorElement, 'cursor', 'pointer');
            
            // Adicionar listener para ver a thread de comentários
            // Remover listener antigo para evitar múltiplos listeners no mesmo elemento
            anchorElement.onclick = null; // Simplista, idealmente gerenciar com Renderer2.listen
            this.renderer.listen(anchorElement, 'click', (event) => {
              event.stopPropagation();
              this.viewCommentThread.emit(comment.anchorId);
            });
          }
        }
      });
    }
  
  
    insertImage(): void {
      if (this.readOnly) {
        this.snackBar.open('O editor está em modo de leitura.', 'Fechar', { duration: 3000 });
        return;
      }
      const url = prompt('Insira a URL da imagem:');
      if (url) {
        this.execCommand('insertImage', url);
      }
    }
  
    insertTable(): void {
      if (this.readOnly) {
        this.snackBar.open('O editor está em modo de leitura.', 'Fechar', { duration: 3000 });
        return;
      }
      const rows = prompt('Número de linhas:', '2');
      const cols = prompt('Número de colunas:', '2');
  
      if (rows && cols) {
        let tableHtml = '<table border="1" style="width:100%; border-collapse: collapse;">';
        for (let i = 0; i < parseInt(rows); i++) {
          tableHtml += '<tr>';
          for (let j = 0; j < parseInt(cols); j++) {
            tableHtml += '<td style="padding: 8px; border: 1px solid #ddd;">Célula</td>';
          }
          tableHtml += '</tr>';
        }
        tableHtml += '</table><p></p>'; // Adicionar um parágrafo após a tabela para facilitar a edição
        
        this.restoreSelection();
        document.execCommand('insertHTML', false, tableHtml);
        this.editorElement.nativeElement.focus();
        this.onContentChanged();
      }
    }
  
    // O botão de "Salvar" no editor pode ser um "Salvar Rascunho Rápido" ou
    // pode ser removido se o salvamento principal for feito pelo `EditorMonografiaComponent`
    // ao criar uma nova versão. Por ora, manteremos a lógica original de `saveDocument`.
    saveDocument(): void {
      if (this.readOnly) {
        this.snackBar.open('O editor está em modo de leitura. Não é possível salvar alterações.', 'Fechar', { duration: 3000 });
        return;
      }
      this.saving = true;
      this.contentChange.emit(this.editorElement.nativeElement.innerHTML); // Garante que o pai tem o conteúdo mais recente
      this.saved.emit({
        content: this.editorElement.nativeElement.innerHTML,
        monografiaId: this.monografiaId,
        versaoId: this.versaoId // O pai decidirá se isso cria uma nova versão ou atualiza um rascunho
      });
  
      // Simular o fim do salvamento (o pai deve controlar 'saving' na realidade)
      setTimeout(() => {
        this.saving = false;
        this.snackBar.open('Conteúdo do editor enviado para salvamento.', 'Fechar', { duration: 2000 });
      }, 1000);
    }
  }