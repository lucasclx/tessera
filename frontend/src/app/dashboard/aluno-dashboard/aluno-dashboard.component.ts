// src/app/dashboard/aluno-dashboard/aluno-dashboard.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { MaterialModule } from '../../material.module';
import { AuthService } from '../../core/auth.service';
import { MonografiaService, Monografia } from '../../core/services/monografia.service';
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

  dashboardData: any = { // Dados ilustrativos
    disciplinasCursando: 5,
    mediaGeral: 8.2,
    atividadesPendentes: 3,
    mensagensNaoLidas: 1,
    proximasAulas: [
      { hora: '09:00', materia: 'Cálculo Avançado', sala: 'Online - Prof. Silva' },
      { hora: '11:00', materia: 'Engenharia de Software II', sala: 'Lab. Info 5 - Profa. Costa' },
    ],
    atividades: [
      { titulo: 'Entrega Cap. 1 Monografia', materia: 'TCC - Prazo: 15/05', status: 'Urgente', urgente: true },
      { titulo: 'Prova P2 - Cálculo', materia: 'Cálculo Avançado - Data: 20/05', status: 'Próxima semana' },
    ],
    notasRecentes: [
        {materia: 'Algoritmos', tipo: 'Trabalho Prático 1', nota: 9.0, classeNota: 'excellent'},
        {materia: 'Redes de Computadores', tipo: 'Prova 1', nota: 7.5, classeNota: 'good'},
    ]
  };

  constructor(
    private authService: AuthService,
    private monografiaService: MonografiaService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    console.log('ALUNO_DASHBOARD: Componente inicializado.');
    const currentUser = this.authService.currentUserValue;
    this.username = currentUser?.username || 'Aluno';
    console.log(`ALUNO_DASHBOARD: Username definido como: ${this.username}`);
    
    const userRole = this.authService.getUserRole();
    console.log(`ALUNO_DASHBOARD: Role do usuário (via authService.getUserRole()): ${userRole}`);
    console.log(`ALUNO_DASHBOARD: Roles completas do usuário (via currentUserValue): ${currentUser?.roles}`);

    if (userRole === 'ALUNO') {
      console.log('ALUNO_DASHBOARD: Usuário é ALUNO. Carregando monografias...');
      this.carregarMinhasMonografias();
    } else {
      const msg = `ALUNO_DASHBOARD: Usuário ${this.username} não possui a role ALUNO (Role atual: ${userRole}). Acesso ao dashboard de aluno não deveria ser permitido por RoleGuard.`;
      console.warn(msg);
      this.erroMonografias = "Acesso não autorizado para este perfil.";
      // Idealmente, o RoleGuard já deveria ter impedido o acesso a este componente.
      // Se chegou aqui, pode ser um problema no guard ou na navegação.
      this.snackBar.open(this.erroMonografias, 'Fechar', { duration: 7000, panelClass: ['snackbar-error'] });
    }
  }

  carregarMinhasMonografias(): void {
    this.loadingMonografias = true;
    this.erroMonografias = null;
    
    const token = this.authService.getToken();
    console.log('ALUNO_DASHBOARD: Iniciando GET /api/monografias.');
    console.log('ALUNO_DASHBOARD: Token JWT (início):', token ? token.substring(0,20)+'...' : 'NULO');
    
    this.monografiaService.getMonografias().subscribe({
      next: (data) => {
        this.minhasMonografias = data;
        this.loadingMonografias = false;
        console.log(`ALUNO_DASHBOARD: Monografias recebidas com sucesso. Quantidade: ${data ? data.length : 0}`, data);
        if(!data || data.length === 0) {
            this.snackBar.open('Nenhuma monografia encontrada para você.', 'Fechar', { duration: 4000 });
        }
      },
      error: (err) => {
        console.error('ALUNO_DASHBOARD: Erro CRÍTICO ao buscar monografias:', err);
        
        let displayErrorMessage = 'Erro ao carregar suas monografias.';
        if (err.status === 0) {
          displayErrorMessage = "Falha de conexão com o servidor. Verifique sua internet ou contate o suporte.";
        } else if (err.status === 401) {
          displayErrorMessage = "Sessão expirada. Por favor, faça login novamente.";
          this.authService.logout(); 
        } else if (err.status === 403) {
          displayErrorMessage = "Acesso negado. Você não tem permissão para visualizar as monografias.";
        } else if (err.error && typeof err.error === 'string' && err.error.includes("accessDenied")) { // Exemplo de tratamento específico se o backend retornar JSON de erro customizado
            displayErrorMessage = "Acesso negado pelo servidor (regra de segurança).";
        } else if (err.error?.message) {
          displayErrorMessage += ` Detalhe do servidor: ${err.error.message}`;
        } else if (err.message) {
          displayErrorMessage += ` Detalhe: ${err.message}`;
        } else {
          displayErrorMessage += ` Código do erro: ${err.status || 'Desconhecido'}.`;
        }
        
        this.erroMonografias = displayErrorMessage;
        this.loadingMonografias = false;
        this.snackBar.open(this.erroMonografias, 'Fechar', { duration: 7000, panelClass: ['snackbar-error'] });
      }
    });
  }

  navegarParaEditor(monografiaId?: number): void {
    if (monografiaId) {
      console.log(`ALUNO_DASHBOARD: Navegando para editor da monografia ID: ${monografiaId}`);
      this.router.navigate(['/monografia/editor', monografiaId]);
    } else {
      console.error('ALUNO_DASHBOARD: ID da monografia inválido para navegação.');
      this.snackBar.open('ID da monografia inválido.', 'Fechar', { duration: 3000, panelClass: ['snackbar-error'] });
    }
  }

  iniciarNovaMonografia(): void {
    this.snackBar.open('Funcionalidade "Iniciar Nova Monografia" ainda não implementada.', 'Ok', { duration: 3500 });
    console.log('ALUNO_DASHBOARD: Tentativa de iniciar nova monografia.');
  }

  logout(): void {
    this.authService.logout();
  }
}