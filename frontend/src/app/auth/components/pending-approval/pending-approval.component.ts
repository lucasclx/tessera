// src/app/auth/components/pending-approval/pending-approval.component.ts
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { AuthService, ApprovalStatus } from '../../../core/auth.service'; // Caminho corrigido
import { environment } from '../../../../environments/environment'; // Verifique este caminho
import { MaterialModule } from '../../../material.module'; // Adicione esta importação

interface AccountDetails {
  nome: string;
  role: string;
  requestedRole?: string;
  createdAt: string;
}

@Component({
  selector: 'app-pending-approval',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MaterialModule // Adicione este módulo
  ],
  templateUrl: './pending-approval.component.html',
  styleUrls: ['./pending-approval.component.scss']
})
export class PendingApprovalComponent implements OnInit {
  username: string | null = null;
  accountDetails: AccountDetails | null = null;
  statusMessage: string | null = null;
  statusClass: 'status-success' | 'status-warning' | 'status-error' | '' = '';
  loading: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private http: HttpClient,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    // Obter o nome de usuário da URL
    this.route.queryParams.subscribe(params => {
      if (params['username']) {
        this.username = params['username'];
        this.loadAccountDetails();
      } else {
        // Se não tiver username, redirecionar para login
        this.router.navigate(['/auth/login']);
      }
    });
  }

  /**
   * Carrega os detalhes da conta (isso exigiria um endpoint do backend)
   * Aqui vamos simular com dados fixos
   */
  loadAccountDetails(): void {
    // Aqui você precisaria criar um endpoint no backend
    // Por enquanto, usando dados simulados
    this.accountDetails = {
      nome: this.username || 'Usuário',
      role: 'Não atribuído',
      requestedRole: 'PROFESSOR',
      createdAt: new Date().toISOString()
    };
  }

  /**
   * Verifica o status atual da conta
   */
  checkStatus(): void {
    this.loading = true;
    this.statusMessage = null;
    this.statusClass = '';

    // Verificar o status de aprovação
    this.authService.checkApprovalStatus(this.username || '').subscribe({
      next: (status) => {
        this.loading = false;
        
        switch (status) {
          case ApprovalStatus.APPROVED:
            this.statusClass = 'status-success';
            this.statusMessage = 'Sua conta foi aprovada! Você já pode fazer login.';
            break;
          case ApprovalStatus.PENDING_APPROVAL:
            this.statusClass = 'status-warning';
            this.statusMessage = 'Sua conta ainda está aguardando aprovação. Por favor, tente novamente mais tarde.';
            break;
          case ApprovalStatus.ACCOUNT_DISABLED:
            this.statusClass = 'status-error';
            this.statusMessage = 'Sua conta foi desativada. Entre em contato com o administrador para mais informações.';
            break;
          case ApprovalStatus.ROLE_MISSING:
            this.statusClass = 'status-error';
            this.statusMessage = 'Sua conta foi aprovada mas não tem perfil atribuído. Entre em contato com o administrador.';
            break;
          default:
            this.statusClass = 'status-error';
            this.statusMessage = 'Não foi possível verificar o status da sua conta. Tente novamente mais tarde.';
        }
      },
      error: (err) => {
        this.loading = false;
        this.statusClass = 'status-error';
        this.statusMessage = 'Erro ao verificar o status: ' + (err.error?.message || err.message || 'Erro desconhecido');
      }
    });
  }

  /**
   * Formata uma data para exibição
   */
  formatDate(dateString: string): string {
    if (!dateString) return 'Data desconhecida';
    
    const date = new Date(dateString);
    return date.toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }
}