// src/app/auth/login/login.component.ts
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';
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
    this.authService.login({ username, password }).subscribe({
      next: (response) => {
        console.log('Login bem-sucedido!', response);
        this.loading = false;
        this.redirectToDashboard();
      },
      error: (err) => {
        console.error('Erro no login:', err);
        this.loading = false;
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
    } else {
      this.errorMessage = 'Usuário não possui um perfil válido para acesso.';
      console.error("Usuário logado mas sem role PROFESSOR ou ALUNO:", this.authService.currentUserValue);
    }
  }
}