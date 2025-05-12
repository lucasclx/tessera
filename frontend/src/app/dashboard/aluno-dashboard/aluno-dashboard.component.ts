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
    this.username = this.authService.currentUserValue?.username || 'Aluno';
    this.carregarMinhasMonografias();
  }

  carregarMinhasMonografias(): void {
    this.loadingMonografias = true;
    this.erroMonografias = null;
    this.monografiaService.getMonografias().subscribe({
      next: (data) => {
        this.minhasMonografias = data;
        this.loadingMonografias = false;
      },
      error: (err) => {
        console.error('Erro ao buscar monografias do aluno:', err);
        this.erroMonografias = `Erro ao carregar monografias: ${err.error?.message || err.message || 'Serviço indisponível'}`;
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