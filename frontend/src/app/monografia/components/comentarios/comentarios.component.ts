// src/app/monografia/components/comentarios/comentarios.component.ts
import { Component, OnInit, Input, Output, EventEmitter, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MaterialModule } from '../../../material.module'; // <--- GARANTIR QUE ESTÁ IMPORTADO
import { ComentarioService, Comentario, NovoComentarioRequest, ComentarioAutor } from '../../../core/services/comentario.service';
import { AuthService } from '../../../core/auth.service';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-comentarios',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MaterialModule // <--- ADICIONADO AOS IMPORTS DO COMPONENTE
  ],
  templateUrl: './comentarios.component.html',
  styleUrls: ['./comentarios.component.scss']
})
export class ComentariosComponent implements OnInit, OnChanges {
  @Input() monografiaId!: number;
  @Input() versaoId!: number;
  @Input() highlightedAnchorId: string | null = null;

  @Output() commentSubmitted = new EventEmitter<Comentario>();
  @Output() commentUpdated = new EventEmitter<Comentario>();
  @Output() commentDeleted = new EventEmitter<number>();

  comentarios: Comentario[] = [];
  comentariosFiltrados: Comentario[] = [];

  novoComentarioForm: FormGroup;
  respostaForm: FormGroup;

  filtragem: 'todos' | 'nao-resolvidos' | 'resolvidos' | 'ancora_atual' = 'nao-resolvidos';
  ordenacao: 'recentes' | 'antigos' = 'recentes';

  loading: boolean = false;
  error: string | null = null;

  respondendoAoId: number | null = null;
  private currentUser: ComentarioAutor | null = null;

  public newCommentForAnchorId: string | null = null;
  public newCommentForSelectionText: string | null = null;


  constructor(
    private fb: FormBuilder,
    private comentarioService: ComentarioService,
    private authService: AuthService,
    private snackBar: MatSnackBar
  ) {
    this.novoComentarioForm = this.fb.group({
      texto: ['', Validators.required]
    });
    this.respostaForm = this.fb.group({
      texto: ['', Validators.required]
    });

    const userAuth = this.authService.currentUserValue;
    if (userAuth && userAuth.username) {
        const userId = userAuth.id; // Agora 'id' existe em AuthResponse
        this.currentUser = {
            id: userId,
            nome: (this.authService.currentUserValue as any)?.nome || userAuth.username, // Melhorar isso se 'nome' vier na AuthResponse
            username: userAuth.username
        };
    }
  }

  ngOnInit(): void {
    if (this.versaoId) {
      this.carregarComentarios();
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['versaoId'] && !changes['versaoId'].firstChange) {
      this.carregarComentarios();
    }
    if (changes['highlightedAnchorId'] && changes['highlightedAnchorId'].currentValue) {
      this.filtragem = 'ancora_atual';
      this.aplicarFiltrosEOrdenacao();
      setTimeout(() => this.scrollToComment(this.highlightedAnchorId), 100);
    } else if (changes['highlightedAnchorId'] && !changes['highlightedAnchorId'].currentValue) {
      this.filtragem = 'nao-resolvidos';
      this.aplicarFiltrosEOrdenacao();
    }
  }

  private scrollToComment(anchorId: string | null): void {
    if (!anchorId) return;
    const element = document.getElementById(`comment-anchor-${anchorId}`);
    if (element) {
      element.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }
  }

  public prepareNewCommentForAnchor(anchorId: string, selectionText: string): void {
    this.newCommentForAnchorId = anchorId;
    this.newCommentForSelectionText = selectionText;
    this.novoComentarioForm.reset();
    this.filtragem = 'todos';
    this.aplicarFiltrosEOrdenacao();
  }

  carregarComentarios(): void {
    if (!this.versaoId) return;
    this.loading = true;
    this.error = null;

    this.comentarioService.getComentariosPorVersao(this.versaoId).subscribe({
      next: (comentarios) => {
        this.comentarios = comentarios;
        this.aplicarFiltrosEOrdenacao();
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.error = `Erro ao carregar comentários: ${err.error?.message || err.message || 'Erro desconhecido'}`;
        console.error('Erro ao carregar comentários:', err);
      }
    });
  }

  aplicarFiltrosEOrdenacao(): void {
    let comentariosProcessados = [...this.comentarios];

    if (this.filtragem === 'resolvidos') {
      comentariosProcessados = comentariosProcessados.filter(c => c.resolvido);
    } else if (this.filtragem === 'nao-resolvidos') {
      comentariosProcessados = comentariosProcessados.filter(c => !c.resolvido);
    } else if (this.filtragem === 'ancora_atual' && this.highlightedAnchorId) {
      comentariosProcessados = comentariosProcessados.filter(c => c.posicaoTexto === this.highlightedAnchorId);
    }

    if (this.ordenacao === 'recentes') {
      comentariosProcessados.sort((a, b) => new Date(b.dataCriacao).getTime() - new Date(a.dataCriacao).getTime());
    } else {
      comentariosProcessados.sort((a, b) => new Date(a.dataCriacao).getTime() - new Date(b.dataCriacao).getTime());
    }
    this.comentariosFiltrados = comentariosProcessados;
  }

  adicionarNovoComentario(): void {
    if (this.novoComentarioForm.invalid || !this.currentUser) return;

    const request: NovoComentarioRequest = {
      versaoId: this.versaoId,
      comentario: this.novoComentarioForm.value.texto,
      posicaoTexto: this.newCommentForAnchorId || undefined
    };

    this.loading = true;
    this.comentarioService.criarComentario(request).subscribe({
      next: (comentarioAdicionado) => {
        this.carregarComentarios();
        this.novoComentarioForm.reset();
        this.newCommentForAnchorId = null;
        this.newCommentForSelectionText = null;
        this.loading = false;
        this.snackBar.open('Comentário adicionado!', 'Fechar', { duration: 3000 });
        this.commentSubmitted.emit(comentarioAdicionado);
      },
      error: (err) => {
        this.loading = false;
        this.snackBar.open(`Erro: ${err.error?.message || 'Não foi possível adicionar comentário.'}`, 'Fechar', { duration: 5000 });
      }
    });
  }

  prepararResposta(comentarioPaiId: number): void {
    this.respondendoAoId = comentarioPaiId;
    this.respostaForm.reset();
  }

  cancelarResposta(): void {
    this.respondendoAoId = null;
    this.respostaForm.reset();
  }

  enviarResposta(): void {
    if (this.respostaForm.invalid || !this.respondendoAoId || !this.currentUser) {
      return;
    }
    this.loading = true;
    this.comentarioService.responderComentario(this.respondendoAoId, this.respostaForm.value.texto, this.versaoId).subscribe({
      next: (respostaAdicionada) => {
        this.carregarComentarios();
        this.cancelarResposta();
        this.loading = false;
        this.snackBar.open('Resposta enviada!', 'Fechar', { duration: 3000 });
        this.commentSubmitted.emit(respostaAdicionada);
      },
      error: (err) => {
        this.loading = false;
        this.snackBar.open(`Erro: ${err.error?.message || 'Não foi possível enviar resposta.'}`, 'Fechar', { duration: 5000 });
      }
    });
  }

  excluirComentario(comentarioId: number): void {
    if (!confirm('Tem certeza que deseja excluir este comentário e suas respostas?')) return;

    this.loading = true;
    this.comentarioService.excluirComentario(comentarioId).subscribe({
      next: () => {
        this.carregarComentarios();
        this.loading = false;
        this.snackBar.open('Comentário excluído.', 'Fechar', { duration: 3000 });
        this.commentDeleted.emit(comentarioId);
      },
      error: (err) => {
        this.loading = false;
        this.snackBar.open(`Erro: ${err.error?.message || 'Não foi possível excluir comentário.'}`, 'Fechar', { duration: 5000 });
      }
    });
  }

  toggleResolvido(comentario: Comentario): void {
    const novoEstadoResolvido = !comentario.resolvido;
    this.loading = true;
    this.comentarioService.atualizarStatusResolvido(comentario.id, novoEstadoResolvido).subscribe({
      next: (comentarioAtualizado) => {
        this.carregarComentarios();
        this.loading = false;
        this.snackBar.open(`Comentário marcado como ${novoEstadoResolvido ? 'resolvido' : 'não resolvido'}.`, 'Fechar', { duration: 3000 });
        this.commentUpdated.emit(comentarioAtualizado);
      },
      error: (err) => {
        this.loading = false;
        this.snackBar.open(`Erro: ${err.error?.message || 'Não foi possível atualizar status.'}`, 'Fechar', { duration: 5000 });
      }
    });
  }


  formatarData(data: string): string {
    if (!data) return '';
    return new Date(data).toLocaleString('pt-BR', {
      day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit'
    });
  }

  podeExcluir(comentario: Comentario): boolean {
    if (!this.currentUser) return false;
    const isAdmin = this.authService.hasRole('ADMIN');
    return isAdmin || comentario.autor.username === this.currentUser.username;
  }

  podeResolver(comentario: Comentario): boolean {
    if (!this.currentUser) return false;
    const isAdmin = this.authService.hasRole('ADMIN');
    const isAutorComentario = comentario.autor.username === this.currentUser.username;
    const isOrientadorDaMonografia = this.authService.hasRole('PROFESSOR');
    return isAdmin || isAutorComentario || isOrientadorDaMonografia;
  }

  alterarFiltragem(filtro: 'todos' | 'nao-resolvidos' | 'resolvidos'): void {
    this.filtragem = filtro;
    this.highlightedAnchorId = null;
    this.newCommentForAnchorId = null;
    this.aplicarFiltrosEOrdenacao();
  }

  alterarOrdenacao(ordem: 'recentes' | 'antigos'): void {
    this.ordenacao = ordem;
    this.aplicarFiltrosEOrdenacao();
  }

  getAvatarLetra(autor: ComentarioAutor | undefined): string {
    if (!autor) return '?';
    return (autor.nome || autor.username || 'U').charAt(0).toUpperCase();
  }

  cancelNewCommentForAnchor(): void {
    this.newCommentForAnchorId = null;
    this.newCommentForSelectionText = null;
    this.novoComentarioForm.reset();
    if (this.filtragem === 'todos' && this.comentarios.some(c => !c.resolvido)) {
        this.filtragem = 'nao-resolvidos';
    }
    this.aplicarFiltrosEOrdenacao();
  }
}