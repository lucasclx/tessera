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
    if (this.authService.isLoggedIn()) {
        this.redirectToDashboard();
    }
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched(); // Marca todos os campos como "tocados" para exibir erros
      return;
    }
    this.loading = true;
    this.errorMessage = null;
    const { username, password } = this.loginForm.value;

    this.authService.login({ username, password }).subscribe({
      next: () => {
        this.loading = false;
        this.redirectToDashboard();
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.message || err.error?.error || err.message || 'Falha no login. Verifique suas credenciais.';
        console.error('Login error:', err);
      }
    });
  }

  private redirectToDashboard(): void {
    if (this.authService.hasRole('PROFESSOR')) {
      this.router.navigate(['/dashboard/professor']);
    } else if (this.authService.hasRole('ALUNO')) {
      this.router.navigate(['/dashboard/aluno']);
    } else {
      this.errorMessage = 'Usuário não possui um perfil válido para acesso.';
      console.error("Usuário logado mas sem role PROFESSOR ou ALUNO:", this.authService.currentUserValue);
      // Opcional: redirecionar para uma página de erro ou logout
      // this.authService.logout();
    }
  }
}