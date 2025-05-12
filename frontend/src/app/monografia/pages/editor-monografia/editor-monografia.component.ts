// src/app/monografia/pages/editor-monografia/editor-monografia.component.ts
import { Component, OnInit, ViewChild, TemplateRef, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MaterialModule } from '../../../material.module';

import { RichTextEditorComponent } from '../../../shared/components/rich-text-editor/rich-text-editor.component';
import { VersaoTimelineComponent } from '../../components/versao-timeline/versao-timeline.component';
import { VersaoDiffViewerComponent } from '../../../shared/components/versao-diff-viewer/versao-diff-viewer.component';
import { ComentariosComponent } from '../../components/comentarios/comentarios.component';

import { VersaoService, Versao, NovaVersaoRequest } from '../../../core/services/versao.service';
import { MonografiaService, Monografia } from '../../../core/services/monografia.service';
import { ComentarioService, Comentario } from '../../../core/services/comentario.service';
import { AuthService, AuthResponse } from '../../../core/auth.service';

@Component({
  selector: 'app-editor-monografia',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MaterialModule,
    RichTextEditorComponent,
    VersaoTimelineComponent,
    VersaoDiffViewerComponent,
    ComentariosComponent
  ],
  templateUrl: './editor-monografia.component.html', // CERTIFIQUE-SE DE QUE O ARQUIVO FÍSICO FOI RENOMEADO PARA .html
  styleUrls: ['./editor-monografia.component.scss']
})
export class EditorMonografiaComponent implements OnInit {
  @ViewChild('saveVersionDialog') saveVersionDialog!: TemplateRef<any>;
  @ViewChild(RichTextEditorComponent) richTextEditor!: RichTextEditorComponent;
  @ViewChild(ComentariosComponent) comentariosComponent!: ComentariosComponent;
  @ViewChild(VersaoTimelineComponent) versaoTimelineComponent!: VersaoTimelineComponent;

  monografiaId!: number;
  monografia: Monografia | null = null;
  documentContent: string = '';
  allCommentsForCurrentVersion: Comentario[] = [];

  showTimeline: boolean = false;
  showComments: boolean = true;
  titleForm: FormGroup;
  editingTitle: boolean = false;
  currentVersaoId?: number;
  currentVersaoDetails?: Versao;

  isAutor: boolean = false;
  isOrientador: boolean = false;

  showDiff: boolean = false;
  diffBaseVersao?: Versao;
  diffNovaVersao?: Versao;

  loading: boolean = true;
  error: string | null = null;

  dialogRef: MatDialogRef<any> | null = null;
  saveForm: FormGroup;
  savingVersion: boolean = false;

  highlightedCommentAnchorId: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private fb: FormBuilder,
    private versaoService: VersaoService,
    private monografiaService: MonografiaService,
    private comentarioService: ComentarioService,
    private authService: AuthService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private cdr: ChangeDetectorRef
  ) {
    this.titleForm = this.fb.group({
      titulo: ['', Validators.required]
    });
    this.saveForm = this.fb.group({
      mensagemCommit: ['', Validators.required],
      tag: ['']
    });
  }

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.monografiaId = +id;
        this.carregarMonografiaEConteudo();
      } else {
        this.error = "ID da monografia não fornecido.";
        this.loading = false;
      }
    });
  }

  // Getter para a contagem de comentários não resolvidos
  public get unresolvedCommentsCount(): number {
    if (!this.allCommentsForCurrentVersion) {
      return 0;
    }
    return this.allCommentsForCurrentVersion.filter(c => !c.resolvido).length;
  }

  carregarMonografiaEConteudo(): void {
    this.loading = true;
    this.error = null;
    this.monografiaService.getMonografia(this.monografiaId).subscribe({
      next: (monografia) => {
        this.monografia = monografia;
        this.titleForm.patchValue({ titulo: monografia.titulo });
        this.setupUserPermissions();
        this.carregarUltimaVersao();
      },
      error: (err) => this.handleError(err, 'carregar monografia')
    });
  }

  setupUserPermissions(): void {
    const currentUser: AuthResponse | null = this.authService.currentUserValue;
    if (currentUser && this.monografia) {
        const userId = currentUser.id;
        this.isAutor = this.monografia.autorPrincipal.id === userId ||
                       (this.monografia.coAutores || []).some(autor => autor.id === userId);
        this.isOrientador = this.monografia.orientadorPrincipal.id === userId ||
                            (this.monografia.coOrientadores || []).some(orientador => orientador.id === userId);
    }
  }

  carregarUltimaVersao(): void {
    this.versaoService.getVersoes(this.monografiaId).subscribe({
      next: (versoes) => {
        if (versoes.length > 0) {
          versoes.sort((a, b) => new Date(b.dataCriacao).getTime() - new Date(a.dataCriacao).getTime());
          this.selecionarVersao(versoes[0]);
        } else {
          this.documentContent = '<p>Comece a escrever sua monografia aqui...</p>';
          this.allCommentsForCurrentVersion = [];
          this.currentVersaoId = undefined;
          this.currentVersaoDetails = undefined;
          this.loading = false;
        }
      },
      error: (err) => this.handleError(err, 'carregar lista de versões')
    });
  }

  selecionarVersao(versao: Versao): void {
    this.loading = true;
    this.currentVersaoDetails = versao;
    this.currentVersaoId = versao.id;
    this.highlightedCommentAnchorId = null;

    this.versaoService.getConteudoVersao(versao.id).subscribe({
      next: (conteudo) => {
        this.documentContent = conteudo;
        this.cdr.detectChanges();
        this.carregarComentariosDaVersao(versao.id);
      },
      error: (err) => this.handleError(err, `carregar conteúdo da versão ${versao.numeroVersao}`)
    });
  }

  carregarComentariosDaVersao(versaoId: number): void {
    this.comentarioService.getComentariosPorVersao(versaoId).subscribe({
      next: (comments) => {
        this.allCommentsForCurrentVersion = comments;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => this.handleError(err, `carregar comentários da versão ${versaoId}`)
    });
  }

  onContentChangedByEditor(content: string): void {
    this.documentContent = content;
  }

  handleNewCommentAnchorRequested({ anchorId, selectionText }: { anchorId: string, selectionText: string }): void {
    this.showComments = true;
    this.highlightedCommentAnchorId = anchorId;
    if (this.comentariosComponent) {
      this.comentariosComponent.prepareNewCommentForAnchor(anchorId, selectionText);
    }
    this.snackBar.open(`Comentário para trecho "${selectionText.substring(0,20)}..." solicitado.`, 'Ok', { duration: 3500 });
  }

  handleViewCommentThread(anchorId: string): void {
    this.showComments = true;
    this.highlightedCommentAnchorId = anchorId;
     if (this.comentariosComponent) {
      this.comentariosComponent.filtragem = 'ancora_atual';
      this.comentariosComponent.aplicarFiltrosEOrdenacao();
       setTimeout(() => (this.comentariosComponent as any)['scrollToComment'](anchorId), 100);
    }
    this.snackBar.open(`Visualizando comentários para trecho ID: ${anchorId}`, 'Ok', { duration: 2000 });
  }

  handleCommentSubmittedOrUpdated(comentario: Comentario): void {
    if (this.currentVersaoId) {
      this.carregarComentariosDaVersao(this.currentVersaoId);
    }
  }

   handleCommentDeleted(): void {
    if (this.currentVersaoId) {
      this.carregarComentariosDaVersao(this.currentVersaoId);
    }
  }

  toggleTimeline(): void {
    this.showTimeline = !this.showTimeline;
    if (this.showTimeline && this.showComments) this.showComments = false;
  }

  toggleComments(): void {
    this.showComments = !this.showComments;
    if (this.showComments && this.showTimeline) this.showTimeline = false;
    if (!this.showComments) this.highlightedCommentAnchorId = null;
  }

  startEditTitle(): void {
    if (!this.isAutor && !this.isOrientador) {
      this.snackBar.open('Você não tem permissão para editar o título.', 'Fechar', { duration: 3000 });
      return;
    }
    this.editingTitle = true;
  }

  saveTitle(): void {
    if (this.titleForm.invalid || !this.monografia) return;
    const novoTitulo = this.titleForm.value.titulo;
    this.monografiaService.atualizarMonografia({ id: this.monografia.id, titulo: novoTitulo })
      .subscribe({
        next: (monografiaAtualizada) => {
          this.monografia = monografiaAtualizada;
          this.editingTitle = false;
          this.snackBar.open('Título atualizado!', 'Fechar', { duration: 3000 });
        },
        error: (err) => this.snackBar.open(`Erro: ${err.error?.message || 'Não foi possível atualizar título.'}`, 'Fechar', { duration: 5000 })
      });
  }

  cancelEditTitle(): void {
    this.titleForm.patchValue({ titulo: this.monografia?.titulo });
    this.editingTitle = false;
  }

  previewVersaoFromTimeline(versao: Versao): void {
    this.selecionarVersao(versao);
  }

  compararVersoes(data: { base: Versao, nova: Versao }): void {
    this.diffBaseVersao = data.base;
    this.diffNovaVersao = data.nova;
    this.showDiff = true;
    this.showTimeline = false;
    this.showComments = false;
  }

  fecharComparacao(): void {
    this.showDiff = false;
  }

  openSaveDialog(): void {
    if (!this.isAutor && !this.isOrientador) {
      this.snackBar.open('Você não tem permissão para criar novas versões.', 'Fechar', { duration: 3000 });
      return;
    }
    this.saveForm.reset({ mensagemCommit: '', tag: '' });
    this.dialogRef = this.dialog.open(this.saveVersionDialog, { width: '500px', disableClose: true });
  }

  salvarNovaVersao(): void {
    if (this.saveForm.invalid) return;
    this.savingVersion = true;

    const novaVersaoReq: NovaVersaoRequest = {
      monografiaId: this.monografiaId,
      conteudo: this.documentContent,
      mensagemCommit: this.saveForm.value.mensagemCommit,
      tag: this.saveForm.value.tag || undefined
    };

    this.versaoService.criarVersao(novaVersaoReq).subscribe({
      next: (versaoSalva) => {
        this.savingVersion = false;
        this.dialogRef?.close();
        this.snackBar.open('Nova versão salva com sucesso!', 'Fechar', { duration: 3000 });
        this.selecionarVersao(versaoSalva);
        if (this.versaoTimelineComponent) {
          this.versaoTimelineComponent.carregarVersoes();
        }
      },
      error: (err) => {
        this.savingVersion = false;
        this.snackBar.open(`Erro ao salvar versão: ${err.error?.message || 'Erro desconhecido'}`, 'Fechar', { duration: 5000 });
      }
    });
  }

  handleEditorQuickSave(event: { content: string }): void {
    console.log('Conteúdo do editor para salvamento rápido:', event.content);
    this.snackBar.open('Rascunho salvo localmente (simulado).', 'Ok', {duration: 2000});
  }

  exportarPDF(): void {
    this.snackBar.open('Exportação para PDF será implementada.', 'Fechar', { duration: 3000 });
  }

  exportarDOCX(): void {
    this.snackBar.open('Exportação para DOCX será implementada.', 'Fechar', { duration: 3000 });
  }

  compartilhar(): void {
    this.snackBar.open('Compartilhamento será implementado.', 'Fechar', { duration: 3000 });
  }

  private handleError(error: any, context: string): void {
    this.loading = false;
    this.error = `Erro ao ${context}: ${error.error?.message || error.message || 'Erro desconhecido'}`;
    console.error(`Erro ao ${context}:`, error);
    this.snackBar.open(this.error, 'Fechar', { duration: 7000, panelClass: ['snackbar-error'] });
  }
}