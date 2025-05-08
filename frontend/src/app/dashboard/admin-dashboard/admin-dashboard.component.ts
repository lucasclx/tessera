// src/app/dashboard/admin-dashboard/admin-dashboard.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { AuthService } from '../../core/auth.service';
import { AdminService, User, UserApprovalRequest } from '../../core/admin.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule
  ],
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.scss']
})
export class AdminDashboardComponent implements OnInit {
  adminName: string | null = null;
  allUsers: User[] = [];
  pendingUsers: User[] = [];
  selectedUser: User | null = null;
  
  approvalForm: FormGroup;
  statusForm: FormGroup;
  
  loading = {
    allUsers: false,
    pendingUsers: false,
    approvalUpdate: false,
    statusUpdate: false
  };
  
  message = {
    type: '',
    text: ''
  };

  viewMode: 'all' | 'pending' = 'pending';

  constructor(
    private authService: AuthService,
    private adminService: AdminService,
    private fb: FormBuilder
  ) {
    this.approvalForm = this.fb.group({
      approved: [false, Validators.required],
      role: [''],
      adminComments: ['', Validators.maxLength(500)]
    });

    this.statusForm = this.fb.group({
      enabled: [true, Validators.required]
    });
  }

  ngOnInit(): void {
    this.adminName = this.authService.currentUserValue?.username || 'Administrador';
    this.loadPendingUsers();
  }

  loadAllUsers(): void {
    this.loading.allUsers = true;
    this.message = { type: '', text: '' };

    this.adminService.getAllUsers().subscribe({
      next: (users: User[]) => {
        this.allUsers = users;
        this.viewMode = 'all';
        this.loading.allUsers = false;
      },
      error: (err: any) => {
        this.message = { 
          type: 'error', 
          text: 'Erro ao carregar usuários: ' + (err.error?.message || err.message || 'Erro desconhecido') 
        };
        this.loading.allUsers = false;
      }
    });
  }

  loadPendingUsers(): void {
    this.loading.pendingUsers = true;
    this.message = { type: '', text: '' };

    this.adminService.getPendingUsers().subscribe({
      next: (users: User[]) => {
        this.pendingUsers = users;
        this.viewMode = 'pending';
        this.loading.pendingUsers = false;
      },
      error: (err: any) => {
        this.message = { 
          type: 'error', 
          text: 'Erro ao carregar usuários pendentes: ' + (err.error?.message || err.message || 'Erro desconhecido') 
        };
        this.loading.pendingUsers = false;
      }
    });
  }

  selectUser(user: User): void {
    this.selectedUser = user;
    
    // Preencher o formulário de aprovação
    this.approvalForm.patchValue({
      approved: user.approved,
      role: user.requestedRole || user.role,
      adminComments: user.adminComments || ''
    });

    // Preencher o formulário de status
    this.statusForm.patchValue({
      enabled: user.enabled
    });
  }

  submitApproval(): void {
    if (!this.selectedUser || this.approvalForm.invalid) {
      return;
    }

    this.loading.approvalUpdate = true;
    this.message = { type: '', text: '' };

    const approvalData: UserApprovalRequest = {
      approved: this.approvalForm.value.approved,
      role: this.approvalForm.value.role,
      adminComments: this.approvalForm.value.adminComments
    };

    this.adminService.updateUserApproval(this.selectedUser.id, approvalData).subscribe({
      next: (updatedUser: User) => {
        this.message = { 
          type: 'success', 
          text: `Usuário ${updatedUser.username} ${approvalData.approved ? 'aprovado' : 'rejeitado'} com sucesso!` 
        };
        this.loading.approvalUpdate = false;
        
        // Atualizar a lista conforme a visualização atual
        if (this.viewMode === 'pending') {
          this.loadPendingUsers();
        } else {
          this.loadAllUsers();
        }
        
        // Limpar seleção
        this.selectedUser = null;
      },
      error: (err: any) => {
        this.message = { 
          type: 'error', 
          text: 'Erro ao atualizar aprovação: ' + (err.error?.message || err.message || 'Erro desconhecido') 
        };
        this.loading.approvalUpdate = false;
      }
    });
  }

  updateUserStatus(): void {
    if (!this.selectedUser || this.statusForm.invalid) {
      return;
    }

    this.loading.statusUpdate = true;
    this.message = { type: '', text: '' };

    const enabled = this.statusForm.value.enabled;

    this.adminService.updateUserStatus(this.selectedUser.id, enabled).subscribe({
      next: (updatedUser: User) => {
        this.message = { 
          type: 'success', 
          text: `Usuário ${updatedUser.username} ${enabled ? 'ativado' : 'desativado'} com sucesso!` 
        };
        this.loading.statusUpdate = false;
        
        // Atualizar a lista conforme a visualização atual
        if (this.viewMode === 'pending') {
          this.loadPendingUsers();
        } else {
          this.loadAllUsers();
        }
        
        // Limpar seleção
        this.selectedUser = null;
      },
      error: (err: any) => {
        this.message = { 
          type: 'error', 
          text: 'Erro ao atualizar status: ' + (err.error?.message || err.message || 'Erro desconhecido') 
        };
        this.loading.statusUpdate = false;
      }
    });
  }

  deleteUser(userId: number): void {
    if (!confirm('Tem certeza que deseja excluir este usuário? Esta ação não pode ser desfeita.')) {
      return;
    }

    this.adminService.deleteUser(userId).subscribe({
      next: () => {
        this.message = { 
          type: 'success', 
          text: 'Usuário excluído com sucesso!' 
        };
        
        // Atualizar a lista conforme a visualização atual
        if (this.viewMode === 'pending') {
          this.loadPendingUsers();
        } else {
          this.loadAllUsers();
        }
        
        // Limpar seleção se o usuário excluído for o selecionado
        if (this.selectedUser && this.selectedUser.id === userId) {
          this.selectedUser = null;
        }
      },
      error: (err: any) => {
        this.message = { 
          type: 'error', 
          text: 'Erro ao excluir usuário: ' + (err.error?.message || err.message || 'Erro desconhecido') 
        };
      }
    });
  }

  cancelSelection(): void {
    this.selectedUser = null;
    this.approvalForm.reset();
    this.statusForm.reset({ enabled: true });
  }

  logout(): void {
    this.authService.logout();
  }

  getFormattedDate(dateString: string | null): string {
    if (!dateString) return 'Não disponível';
    
    const date = new Date(dateString);
    return date.toLocaleDateString('pt-BR', { 
      day: '2-digit', 
      month: '2-digit', 
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getRoleBadgeClass(role: string): string {
    switch(role) {
      case 'ADMIN': return 'badge-admin';
      case 'PROFESSOR': return 'badge-professor';
      case 'ALUNO': return 'badge-aluno';
      default: return 'badge-default';
    }
  }

  getStatusBadgeClass(status: boolean): string {
    return status ? 'badge-approved' : 'badge-pending';
  }
}