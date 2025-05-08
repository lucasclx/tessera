// src/app/auth/login/login.component.ts
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss' // Certifique-se que aponta para o arquivo SCSS
})
export class LoginComponent implements OnInit {
  loginForm: FormGroup;
  errorMessage: string | null = null;
  loading = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      username: ['', [Validators.required]],
      password: ['', [Validators.required]]
    });
  }

  ngOnInit(): void {
    console.log('LoginComponent ngOnInit disparado.'); // Log adicional
    if (this.authService.isLoggedIn()) {
        console.log('Usuário já logado, redirecionando para o dashboard.'); // Log adicional
        this.redirectToDashboard();
    }
  }

  // Método de log para o clique do botão (para teste)
  logButtonClick(): void {
    console.log('Botão ENTRAR clicado! (Evento de clique direto)');
  }

  onSubmit(): void {
    console.log('onSubmit foi chamado.'); // Log principal
    console.log('Formulário válido:', !this.loginForm.invalid);
    console.log('Valores do formulário:', this.loginForm.value);
    console.log('Estado de loading:', this.loading);

    if (this.loginForm.invalid) {
      console.log('Formulário inválido, marcando campos como touched.');
      this.loginForm.markAllAsTouched(); // Marca todos os campos como "tocados" para exibir erros
      return;
    }
    this.loading = true;
    this.errorMessage = null;
    const { username, password } = this.loginForm.value;

    console.log(`Tentando login com usuário: ${username}`); // Log antes da chamada de serviço
    this.authService.login({ username, password }).subscribe({
      next: (response) => { // Adicionado response para log
        console.log('Login bem-sucedido!', response); // Log de sucesso
        this.loading = false;
        this.redirectToDashboard();
      },
      error: (err) => {
        console.error('Erro no login:', err); // Log de erro detalhado
        this.loading = false;
        this.errorMessage = err.error?.message || err.error?.error || err.message || 'Falha no login. Verifique suas credenciais.';
      }
    });
  }

  private redirectToDashboard(): void {
    console.log('redirectToDashboard foi chamado.'); // Log
    if (this.authService.hasRole('PROFESSOR')) {
      console.log('Redirecionando para /dashboard/professor'); // Log
      this.router.navigate(['/dashboard/professor']);
    } else if (this.authService.hasRole('ALUNO')) {
      console.log('Redirecionando para /dashboard/aluno'); // Log
      this.router.navigate(['/dashboard/aluno']);
    } else {
      this.errorMessage = 'Usuário não possui um perfil válido para acesso.';
      console.error("Usuário logado mas sem role PROFESSOR ou ALUNO:", this.authService.currentUserValue);
      // Opcional: redirecionar para uma página de erro ou logout
      // this.authService.logout();
    }
  }
}