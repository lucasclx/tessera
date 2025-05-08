// src/app/core/navigation.service.ts
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class NavigationService {
  
  constructor(
    private router: Router,
    private authService: AuthService
  ) {}
  
  /**
   * Navega para a página apropriada após login bem-sucedido
   * @param returnUrl URL opcional para retornar após login
   */
  navigateAfterLogin(returnUrl?: string): void {
    if (returnUrl && returnUrl !== '/login' && returnUrl !== '/register' && returnUrl !== '/') {
      this.router.navigateByUrl(returnUrl);
      return;
    }
    
    this.navigateToDashboard();
  }
  
  /**
   * Navega para o dashboard apropriado com base no perfil do usuário
   */
  navigateToDashboard(): void {
    if (this.authService.hasRole('ADMIN')) {
      this.router.navigate(['/dashboard/admin']);
    } else if (this.authService.hasRole('PROFESSOR')) {
      this.router.navigate(['/dashboard/professor']);
    } else if (this.authService.hasRole('ALUNO')) {
      this.router.navigate(['/dashboard/aluno']);
    } else {
      // Fallback para home caso não tenha perfil específico
      this.router.navigate(['/home']);
    }
  }
  
  /**
   * Navega para a página de login com uma URL de retorno opcional
   * @param returnUrl URL para retornar após login
   */
  navigateToLogin(returnUrl?: string): void {
    if (returnUrl) {
      this.router.navigate(['/auth/login'], { queryParams: { returnUrl } });
    } else {
      this.router.navigate(['/auth/login']);
    }
  }
}