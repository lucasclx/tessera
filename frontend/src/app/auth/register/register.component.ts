// src/app/auth/register/register.component.ts
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService, RegistrationData } from '../../core/auth.service'; // Importar RegistrationData também

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
      institution: ['', Validators.required],
      roles: [['ALUNO'], Validators.required] // Inicializado com ALUNO como valor padrão
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

    // Extrair os valores do formulário
    const { nome, username, email, password, institution, roles } = this.registerForm.value;
    
    // Criar o objeto de registro conforme esperado pelo backend
    const registrationData: RegistrationData = {
      nome,
      username,
      email,
      password,
      institution,
      // Converte o array para o formato esperado pelo backend e faz o cast explicito para Set<string>
      role: new Set<string>(roles as string[])
    };

    console.log('Enviando dados de registro:', registrationData);
    
    this.authService.register(registrationData).subscribe({
      next: (response) => {
        this.loading = false;
        this.successMessage = 'Usuário registrado com sucesso! Você pode fazer login agora.';
        console.log('Registro bem-sucedido:', response);
        setTimeout(() => this.router.navigate(['/auth/login']), 3000);
      },
      error: (err) => {
        this.loading = false;
        console.error('Erro no registro:', err);
        if (err.status === 403) {
          this.errorMessage = 'Acesso negado. Verifique se você tem permissão para registrar um novo usuário.';
        } else {
          this.errorMessage = err.error?.message || err.error?.error || err.message || 'Falha no registro. Tente novamente.';
        }
      }
    });
  }
}