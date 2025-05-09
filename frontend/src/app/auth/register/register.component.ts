import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/auth.service';

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
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent implements OnInit {
  registerForm!: FormGroup;
  errorMessage: string | null = null;
  successMessage: string | null = null;
  loading = false;
  submitted = false;
  showPassword = false;
  showConfirmPassword = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.createForm();
  }

  ngOnInit(): void { }

  createForm(): void {
    this.registerForm = this.fb.group({
      nome: ['', Validators.required],
      username: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required],
      institution: ['', Validators.required],
      role: ['ALUNO', Validators.required]
    }, { validators: passwordMatchValidator });
  }

  // getter para facilitar acesso aos campos do formulário
  get f(): { [key: string]: AbstractControl } { 
    return this.registerForm.controls; 
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  toggleConfirmPasswordVisibility(): void {
    this.showConfirmPassword = !this.showConfirmPassword;
  }

  onSubmit(): void {
    this.submitted = true;
    
    if (this.registerForm.invalid) {
      return;
    }
    
    this.loading = true;
    this.errorMessage = null;
    this.successMessage = null;

    const formData = this.registerForm.value;
    
    // Criar o objeto de registro conforme esperado pelo backend
    const registrationData = {
      nome: formData.nome,
      username: formData.username,
      email: formData.email,
      password: formData.password,
      institution: formData.institution,
      role: new Set([formData.role]) // Converte para Set conforme esperado pelo API
    };

    this.authService.register(registrationData).subscribe({
      next: (response) => {
        this.loading = false;
        this.successMessage = 'Usuário registrado com sucesso! Você pode fazer login agora.';
        
        // Se o perfil escolhido for professor, mostra uma mensagem adicional
        if (formData.role === 'PROFESSOR') {
          this.successMessage += ' Sua solicitação de perfil de Professor será analisada pelos administradores.';
        }
        
        // Resetar o formulário
        this.registerForm.reset();
        this.submitted = false;
        
        // Redirecionar para login após 3 segundos
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