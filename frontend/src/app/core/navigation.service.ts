// src/app/core/services/navigation.service.ts
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../auth.service';

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
    // Se houver uma URL de retorno específica e não for uma página de autenticação, usa ela
    if (returnUrl && 
        !returnUrl.includes('/login') && 
        !returnUrl.includes('/register') && 
        !returnUrl.includes('/auth') && 
        returnUrl !== '/') {
      this.router.navigateByUrl(returnUrl);
      return;
    }
    
    // Caso contrário, redireciona com base no perfil do usuário
    this.navigateToDashboard();
  }
  
  /**
   * Navega para o dashboard apropriado com base no perfil do usuário
   */
  navigateToDashboard(): void {
    // Obter a role principal do usuário
    const role = this.authService.getUserRole();
    
    console.log('Navegando para dashboard com base na role:', role);
    
    // Redirecionar com base na role
    switch (role) {
      case 'ADMIN':
        this.router.navigate(['/dashboard/admin']);
        break;
      case 'PROFESSOR':
        this.router.navigate(['/dashboard/professor']);
        break;
      case 'ALUNO':
        this.router.navigate(['/dashboard/aluno']);
        break;
      default:
        // Se não tiver uma role específica ou valid, redireciona para home
        this.router.navigate(['/home']);
        console.warn('Usuário sem role válida detectado:', 
                    this.authService.currentUserValue?.roles || 'sem roles');
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
  
  /**
   * Verifica se o usuário tem permissão para acessar uma rota específica
   * @param expectedRole Role esperada para acesso
   * @returns boolean indicando se o usuário tem permissão
   */
  canAccessRoute(expectedRole: string): boolean {
    // Verificar se o usuário está autenticado
    if (!this.authService.isLoggedIn()) {
      return false;
    }
    
    // Verificar se o usuário tem a role esperada
    return this.authService.hasRole(expectedRole);
  }
}