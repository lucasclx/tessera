// src/app/auth/login/login.component.ts
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService, ApprovalStatus } from '../../core/auth.service'; // Mantido
import { CommonModule } from '@angular/common';
import { MaterialModule } from '../../material.module'; // Adicionado
import { NavigationService } from '../../core/services/navigation.service'; // Importar para navegação pós-login

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule, // Mantido
    RouterLink,
    MaterialModule // Adicionado
  ],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'] // Corrigido para styleUrls
})
export class LoginComponent implements OnInit {
  loginForm: FormGroup;
  errorMessage: string | null = null;
  loading = false;
  returnUrl: string = ''; // Inicializar returnUrl
  passwordVisible = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute,
    private navigationService: NavigationService // Injetar NavigationService
  ) {
    this.loginForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]], // Adicionado minLength como exemplo
      password: ['', [Validators.required, Validators.minLength(6)]] // Adicionado minLength
    });
  }

  ngOnInit(): void {
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/'; // Definir um padrão
    const error = this.route.snapshot.queryParams['error'];
    if (error) {
      this.errorMessage = error;
    }

    if (this.authService.isLoggedIn()) {
      this.navigationService.navigateToDashboard(); // Usar NavigationService
    }
  }

  togglePasswordVisibility(): void {
    this.passwordVisible = !this.passwordVisible;
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched(); // Marcar todos os campos para exibir erros
      return;
    }

    this.loading = true;
    this.errorMessage = null;
    const { username, password } = this.loginForm.value;

    this.authService.checkApprovalStatus(username).subscribe({
      next: (status) => {
        if (status === ApprovalStatus.PENDING_APPROVAL) {
          this.loading = false;
          this.router.navigate(['/auth/pending-approval'], { queryParams: { username } });
          return;
        } else if (status === ApprovalStatus.ACCOUNT_DISABLED) {
          this.loading = false;
          this.errorMessage = 'Sua conta está desativada. Entre em contato com o administrador.';
          return;
        } else if (status === ApprovalStatus.ROLE_MISSING) {
          this.loading = false;
          this.errorMessage = 'Sua conta foi aprovada mas não possui um papel atribuído. Entre em contato com o administrador.';
          return;
        }
        this.performLogin(username, password);
      },
      error: (err) => {
        console.warn('Erro ao verificar status de aprovação, tentando login direto:', err);
        this.performLogin(username, password);
      }
    });
  }

  private performLogin(username: string, password: string): void {
    this.authService.login({ username, password }).subscribe({
      next: () => {
        this.loading = false;
        // Usar o NavigationService para redirecionar após o login
        this.navigationService.navigateAfterLogin(this.returnUrl);
      },
      error: (err) => {
        this.loading = false;
        if (err.status === 403 && err.error?.message?.includes('aguardando aprovação')) {
          this.router.navigate(['/auth/pending-approval'], { queryParams: { username } });
          return;
        }
        this.errorMessage = err.error?.message || err.error?.error || err.message || 'Falha no login. Verifique suas credenciais.';
      }
    });
  }

  // Getters para fácil acesso aos controles do formulário no template
  get usernameFc() {
    return this.loginForm.get('username');
  }

  get passwordFc() {
    return this.loginForm.get('password');
  }
}