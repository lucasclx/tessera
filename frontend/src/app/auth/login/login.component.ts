// src/app/auth/login/login.component.ts
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService, ApprovalStatus } from '../../core/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent implements OnInit {
  loginForm: FormGroup;
  errorMessage: string | null = null;
  loading = false;
  returnUrl: string = '/dashboard';
  passwordVisible = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.loginForm = this.fb.group({
      username: ['', [Validators.required]],
      password: ['', [Validators.required]]
    });
  }

  ngOnInit(): void {
    console.log('LoginComponent ngOnInit');
    
    // Pega a URL de retorno dos query params ou usa o valor padrão
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/dashboard';
    
    // Verifica se já está logado
    if (this.authService.isLoggedIn()) {
      console.log('Usuário já está logado, redirecionando...');
      this.redirectToDashboard();
    }
  }

  // Alterna a visibilidade da senha
  togglePasswordVisibility(): void {
    this.passwordVisible = !this.passwordVisible;
  }

  onSubmit(): void {
    console.log('onSubmit foi chamado');
    
    if (this.loginForm.invalid) {
      console.log('Formulário inválido, marcando campos como touched');
      this.loginForm.markAllAsTouched();
      return;
    }
    
    this.loading = true;
    this.errorMessage = null;
    const { username, password } = this.loginForm.value;

    console.log(`Tentando login com usuário: ${username}`);
    
    // Primeiro verificamos o status de aprovação do usuário
    this.authService.checkApprovalStatus(username).subscribe({
      next: (status) => {
        if (status === ApprovalStatus.PENDING_APPROVAL) {
          // Redirecionar para a página de aprovação pendente
          this.loading = false;
          this.router.navigate(['/auth/pending-approval'], { 
            queryParams: { username: username }
          });
          return;
        } else if (status === ApprovalStatus.ACCOUNT_DISABLED) {
          // Conta desativada
          this.loading = false;
          this.errorMessage = 'Sua conta está desativada. Entre em contato com o administrador.';
          return;
        }
        
        // Se chegou aqui, o usuário está aprovado ou o status é desconhecido
        // Prosseguimos com o login normal
        this.performLogin(username, password);
      },
      error: (err) => {
        // Se houver erro ao verificar o status, tentamos o login normal
        console.warn('Erro ao verificar status de aprovação, tentando login direto:', err);
        this.performLogin(username, password);
      }
    });
  }
  
  private performLogin(username: string, password: string): void {
    this.authService.login({ username, password }).subscribe({
      next: (response) => {
        console.log('Login bem-sucedido!', response);
        this.loading = false;
        this.redirectToDashboard();
      },
      error: (err) => {
        console.error('Erro no login:', err);
        this.loading = false;
        
        // Verificar se o erro é devido a aprovação pendente
        if (err.status === 403 && err.error?.message?.includes('aguardando aprovação')) {
          this.router.navigate(['/auth/pending-approval'], { 
            queryParams: { username: username }
          });
          return;
        }
        
        this.errorMessage = err.error?.message || err.error?.error || err.message || 'Falha no login. Verifique suas credenciais.';
      }
    });
  }

  private redirectToDashboard(): void {
    console.log('redirectToDashboard, returnUrl =', this.returnUrl);
    
    // Se houver uma URL de retorno específica, usa ela
    if (this.returnUrl && this.returnUrl !== '/dashboard') {
      this.router.navigateByUrl(this.returnUrl);
      return;
    }
    
    // Caso contrário, redireciona com base no papel do usuário
    if (this.authService.hasRole('PROFESSOR')) {
      console.log('Redirecionando para dashboard do professor');
      this.router.navigate(['/dashboard/professor']);
    } else if (this.authService.hasRole('ALUNO')) {
      console.log('Redirecionando para dashboard do aluno');
      this.router.navigate(['/dashboard/aluno']);
    } else if (this.authService.hasRole('ADMIN')) {
      console.log('Redirecionando para dashboard do administrador');
      this.router.navigate(['/dashboard/admin']);
    } else {
      this.errorMessage = 'Usuário não possui um perfil válido para acesso.';
      console.error("Usuário logado mas sem role PROFESSOR, ALUNO ou ADMIN:", this.authService.currentUserValue);
    }
  }
}