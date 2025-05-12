// src/app/monografia/pages/editor-monografia/editor-monografia.component.ts
import { Component, OnInit, ViewChild, TemplateRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MaterialModule } from '../../../material.module';
import { RichTextEditorComponent } from '../../components/rich-text-editor/rich-text-editor.component';
import { VersaoTimelineComponent } from '../../components/versao-timeline/versao-timeline.component';
import { VersaoDiffViewerComponent } from '../../components/versao-diff-viewer/versao-diff-viewer.component';
import { ComentariosComponent } from '../../components/comentarios/comentarios.component';

import { VersaoService, Versao, NovaVersaoRequest } from '../../../core/services/versao.service';
import { MonografiaService, Monografia } from '../../../core/services/monografia.service';
import { ComentarioService } from '../../../core/services/comentario.service';
import { AuthService } from '../../../core/auth.service';

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
  templateUrl: './editor-monografia.component.html',
  styleUrls: ['./editor-monografia.component.scss']
})
export class EditorMonografiaComponent implements OnInit {
  @ViewChild('saveVersionDialog') saveVersionDialog!: TemplateRef<any>;

  // Informações da monografia e versão
  monografiaId!: number;
  monografia: Monografia | null = null;
  documentContent: string = '';
  showTimeline: boolean = false;
  showComments: boolean = false;
  titleForm: FormGroup;
  editingTitle: boolean = false;
  currentVersaoId?: number;
  isAutor: boolean = false;
  isOrientador: boolean = false;
  
  // Informações para o diff
  showDiff: boolean = false;
  diffBaseVersao?: Versao;
  diffNovaVersao?: Versao;
  
  // Estado de carregamento e erros
  loading: boolean = false;
  error: string | null = null;
  
  // Diálogo para salvar nova versão
  dialogRef: MatDialogRef<any> | null = null;
  saveForm: FormGroup;
  savingVersion: boolean = false;
  
  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private fb: FormBuilder,
    private versaoService: VersaoService,
    private monografiaService: MonografiaService,
    private comentarioService: ComentarioService,
    private authService: AuthService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {
    // Inicializar formulários
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
        this.carregarMonografia();
      } else {
        this.router.navigate(['/monografias']);
      }
    });
  }
  
  carregarMonografia(): void {
    this.loading = true;
    this.error = null;
    
    this.monografiaService.getMonografia(this.monografiaId).subscribe({
      next: (monografia) => {
        this.monografia = monografia;
        this.titleForm.get('titulo')?.setValue(monografia.titulo);
        
        // Verificar permissões do usuário
        const currentUser = this.authService.currentUserValue;
        if (currentUser) {
          this.isAutor = monografia.autorPrincipal.id === currentUser.id || 
                         (monografia.coAutores || []).some(autor => autor.id === currentUser.id);
          
          this.isOrientador = monografia.orientadorPrincipal.id === currentUser.id || 
                             (monografia.coOrientadores || []).some(orientador => orientador.id === currentUser.id);
        }
        
        // Carregar a versão mais recente
        this.carregarUltimaVersao();
      },
      error: (err) => {
        this.loading = false;
        this.error = `Erro ao carregar monografia: ${err.error?.message || err.message || 'Erro desconhecido'}`;
        console.error('Erro ao carregar monografia:', err);
      }
    });
  }
  
  carregarUltimaVersao(): void {
    this.versaoService.getVersoes(this.monografiaId).subscribe({
      next: (versoes) => {
        this.loading = false;
        
        if (versoes.length > 0) {
          // Ordenar versões por data de criação (mais recente primeiro)
          versoes.sort((a, b) => new Date(b.dataCriacao).getTime() - new Date(a.dataCriacao).getTime());
          
          const ultimaVersao = versoes[0];
          this.carregarVersao(ultimaVersao);
        } else {
          // Nenhuma versão encontrada, iniciar com documento vazio
          this.documentContent = '';
        }
      },
      error: (err) => {
        this.loading = false;
        this.error = `Erro ao carregar versões: ${err.error?.message || err.message || 'Erro desconhecido'}`;
        console.error('Erro ao carregar versões:', err);
      }
    });
  }
  
  carregarVersao(versao: Versao): void {
    this.loading = true;
    this.currentVersaoId = versao.id;
    
    this.versaoService.getConteudoVersao(versao.id).subscribe({
      next: (conteudo) => {
        this.documentContent = conteudo;
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.error = `Erro ao carregar conteúdo da versão: ${err.error?.message || err.message || 'Erro desconhecido'}`;
        console.error('Erro ao carregar conteúdo da versão:', err);
      }
    });
  }
  
  onContentChange(content: string): void {
    this.documentContent = content;
  }
  
  toggleTimeline(): void {
    this.showTimeline = !this.showTimeline;
    
    // Fechar painel de comentários se estiver aberto
    if (this.showTimeline && this.showComments) {
      this.showComments = false;
    }
  }
  
  toggleComments(): void {
    this.showComments = !this.showComments;
    
    // Fechar timeline se estiver aberto
    if (this.showComments && this.showTimeline) {
      this.showTimeline = false;
    }
  }
  
  startEditTitle(): void {
    if (!this.isAutor && !this.isOrientador) {
      this.snackBar.open('Você não tem permissão para editar o título.', 'Fechar', {
        duration: 3000
      });
      return;
    }
    
    this.editingTitle = true;
  }
  
  saveTitle(): void {
    if (this.titleForm.valid && this.monografia) {
      const novoTitulo = this.titleForm.get('titulo')?.value;
      
      this.monografiaService.atualizarMonografia({
        id: this.monografia.id,
        titulo: novoTitulo
      }).subscribe({
        next: (monografiaAtualizada) => {
          this.monografia = monografiaAtualizada;
          this.editingTitle = false;
          this.snackBar.open('Título atualizado com sucesso!', 'Fechar', {
            duration: 3000
          });
        },
        error: (err) => {
          console.error('Erro ao atualizar título:', err);
          this.snackBar.open('Erro ao atualizar título. Tente novamente.', 'Fechar', {
            duration: 5000
          });
        }
      });
    }
  }
  
  cancelEditTitle(): void {
    this.titleForm.get('titulo')?.setValue(this.monografia?.titulo);
    this.editingTitle = false;
  }
  
  previewVersao(versao: Versao): void {
    this.carregarVersao(versao);
  }
  
  compararVersoes(data: {base: Versao, nova: Versao}): void {
    this.diffBaseVersao = data.base;
    this.diffNovaVersao = data.nova;
    this.showDiff = true;
  }
  
  fecharComparacao(): void {
    this.showDiff = false;
  }
  
  openSaveDialog(): void {
    if (!this.isAutor && !this.isOrientador) {
      this.snackBar.open('Você não tem permissão para criar versões.', 'Fechar', {
        duration: 3000
      });
      return;
    }
    
    // Resetar o formulário
    this.saveForm.reset({
      mensagemCommit: '',
      tag: ''
    });
    
    this.dialogRef = this.dialog.open(this.saveVersionDialog, {
      width: '500px',
      disableClose: false
    });
  }
  
  salvarVersao(): void {
    if (this.saveForm.invalid) {
      return;
    }
    
    this.savingVersion = true;
    
    const novaVersao: NovaVersaoRequest = {
      monografiaId: this.monografiaId,
      conteudo: this.documentContent,
      mensagemCommit: this.saveForm.get('mensagemCommit')?.value,
      tag: this.saveForm.get('tag')?.value
    };
    
    this.versaoService.criarVersao(novaVersao).subscribe({
      next: (versao) => {
        this.savingVersion = false;
        
        if (this.dialogRef) {
          this.dialogRef.close();
        }
        
        this.currentVersaoId = versao.id;
        
        // Atualizar a timeline
        if (this.showTimeline) {
          setTimeout(() => {
            this.recarregarTimeline();
          }, 500);
        }
        
        this.snackBar.open('Versão salva com sucesso!', 'Fechar', {
          duration: 3000
        });
      },
      error: (err) => {
        this.savingVersion = false;
        console.error('Erro ao salvar versão:', err);
        
        this.snackBar.open(
          `Erro ao salvar versão: ${err.error?.message || err.message || 'Erro desconhecido'}`, 
          'Fechar', 
          { duration: 5000 }
        );
      }
    });
  }
  
  recarregarTimeline(): void {
    // Este método seria chamado pelo componente pai para atualizar a timeline
    // A implementação real depende de como o VersaoTimelineComponent é estruturado
    const timelineComponent = document.querySelector('app-versao-timeline') as any;
    if (timelineComponent && timelineComponent.carregarVersoes) {
      timelineComponent.carregarVersoes();
    }
  }
  
  exportarPDF(): void {
    // Implementação futura - exportar para PDF
    this.snackBar.open('Exportação para PDF será implementada em breve!', 'Fechar', {
      duration: 3000
    });
  }
  
  exportarDOCX(): void {
    // Implementação futura - exportar para DOCX
    this.snackBar.open('Exportação para DOCX será implementada em breve!', 'Fechar', {
      duration: 3000
    });
  }
  
  compartilhar(): void {
    // Implementação futura - compartilhar monografia
    this.snackBar.open('Compartilhamento será implementado em breve!', 'Fechar', {
      duration: 3000
    });
  }