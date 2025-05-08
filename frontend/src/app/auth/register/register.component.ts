// src/app/auth/register/register.component.ts
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/auth.service'; // Importe o AuthService

// Validador customizado para senhas
export function passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
  const password = control.get('password');
  const confirmPassword = control.get('confirmPassword');

  if (password && confirmPassword && password.value !== confirmPassword.value) {
    return { passwordMismatch: true };
  }
  return null;
}

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink
  ],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})
export class RegisterComponent implements OnInit {
  registerForm: FormGroup;
  errorMessage: string | null = null;
  successMessage: string | null = null;
  loading = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.registerForm = this.fb.group({
      nome: ['', Validators.required],
      username: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required],
      roles: [[], Validators.required] // Array para roles, inicialmente vazio
    }, { validators: passwordMatchValidator });
  }

  ngOnInit(): void { }

  onSubmit(): void {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }
    this.loading = true;
    this.errorMessage = null;
    this.successMessage = null;

    const { nome, username, email, password, roles } = this.registerForm.value;
    // O backend espera um Set<String> para roles, então enviamos como array
    const registrationData = { nome, username, email, password, roles: roles };


    this.authService.register(registrationData).subscribe({
      next: (response) => {
        this.loading = false;
        this.successMessage = 'Usuário registrado com sucesso! Você pode fazer login agora.';
        console.log('Registration successful', response);
        // Opcional: limpar o formulário ou redirecionar para login após um tempo
        // this.registerForm.reset();
        // setTimeout(() => this.router.navigate(['/auth/login']), 3000);
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.message || err.error?.error || err.message || 'Falha no registro. Tente novamente.';
        console.error('Registration error', err);
      }
    });
  }
}