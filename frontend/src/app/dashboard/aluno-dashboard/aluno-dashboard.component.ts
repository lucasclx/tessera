// src/app/dashboard/aluno-dashboard/aluno-dashboard.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { MaterialModule } from '../../material.module';
import { AuthService } from '../../core/auth.service';
import { MonografiaService, Monografia } from '../../core/services/monografia.service'; // Importar Monografia
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-aluno-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MaterialModule
  ],
  templateUrl: './aluno-dashboard.component.html',
  styleUrls: ['./aluno-dashboard.component.scss']
})
export class AlunoDashboardComponent implements OnInit {
  username: string | null = null;
  minhasMonografias: Monografia[] = [];
  loadingMonografias: boolean = false;
  erroMonografias: string | null = null;

  // Dados ilustrativos que podem ser mantidos ou removidos/substituídos
  dashboardData: any = { // Objeto para manter dados ilustrativos
    disciplinasCursando: 6,
    mediaGeral: 8.5,
    atividadesPendentes: 4,
    mensagensNaoLidas: 2,
    proximasAulas: [
      { hora: '08:00', materia: 'Matemática', sala: 'Sala 105 - Prof. Carlos' },
      { hora: '10:30', materia: 'Física', sala: 'Laboratório 3 - Profa. Ana' },
      { hora: '13:45', materia: 'História', sala: 'Sala 209 - Prof. Roberto' },
    ],
    atividades: [
      { titulo: 'Relatório de Laboratório', materia: 'Física - Entrega: 25/05', status: '2 dias', urgente: true },
      { titulo: 'Lista de Exercícios', materia: 'Matemática - Entrega: 28/05', status: '5 dias' },
    ],
    notasRecentes: [
        {materia: 'Matemática', tipo: 'Prova 1', nota: 8.5, classeNota: 'good'},
        {materia: 'História', tipo: 'Trabalho', nota: 9.8, classeNota: 'excellent'},
    ]
  };


  constructor(
    private authService: AuthService,
    private monografiaService: MonografiaService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    console.log('AlunoDashboardComponent inicializado');
    this.username = this.authService.currentUserValue?.username || 'Aluno';
    console.log('Username definido:', this.username);
    
    // Verificar se o usuário tem a role "ALUNO"
    console.log('Usuário atual:', this.authService.currentUserValue);
    console.log('Roles disponíveis:', this.authService.currentUserValue?.roles);
    console.log('Role principal:', this.authService.getUserRole());
    console.log('Tem role ALUNO?', this.authService.hasRole('ALUNO'));
    
    this.carregarMinhasMonografias();
  }

  carregarMinhasMonografias(): void {
    this.loadingMonografias = true;
    this.erroMonografias = null;
    
    console.log('Token atual:', this.authService.getToken());
    
    this.monografiaService.getMonografias().subscribe({
      next: (data) => {
        this.minhasMonografias = data;
        this.loadingMonografias = false;
        console.log('Monografias carregadas com sucesso:', data.length);
      },
      error: (err) => {
        console.error('Erro detalhado ao buscar monografias:', err);
        
        if (err.status === 401) {
          this.erroMonografias = "Sessão expirada. Por favor, faça login novamente.";
          this.authService.logout(); // Força logout para renovar o token
        } else {
          this.erroMonografias = `Erro ao carregar monografias: ${err.error?.message || err.status} - ${err.message || 'Serviço indisponível'}`;
        }
        
        this.loadingMonografias = false;
        this.snackBar.open(this.erroMonografias, 'Fechar', { duration: 5000 });
      }
    });
  }

  navegarParaEditor(monografiaId: number): void {
    this.router.navigate(['/monografia/editor', monografiaId]);
  }

  iniciarNovaMonografia(): void {
    // No futuro, isso navegará para um componente de criação de monografia.
    // Ex: this.router.navigate(['/monografia/nova']);
    this.snackBar.open('Funcionalidade "Iniciar Nova Monografia" será implementada.', 'Ok', { duration: 3000 });
    console.log('Tentativa de iniciar nova monografia.');
  }

  logout(): void {
    this.authService.logout();
  }
}