// src/app/core/guards/role.guard.ts
import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { AuthService } from '../auth.service';

@Injectable({
  providedIn: 'root'
})
export class RoleGuard implements CanActivate {

  constructor(private authService: AuthService, private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    // Obter o papel esperado da configuração da rota
    const expectedRole = route.data['expectedRole'] as string;

    console.log('RoleGuard verificando acesso para role:', expectedRole);
    console.log('Usuário atual:', this.authService.currentUserValue?.username);
    console.log('Roles do usuário:', this.authService.currentUserValue?.roles);

    // Verificar se o usuário está autenticado
    if (!this.authService.isLoggedIn()) {
      console.log('RoleGuard: usuário não autenticado, redirecionando para login');
      this.router.navigate(['/auth/login'], { queryParams: { returnUrl: state.url }});
      return false;
    }

    // Verificar se o usuário tem a role esperada
    if (this.authService.hasRole(expectedRole)) {
      console.log(`RoleGuard: usuário tem a role ${expectedRole}, permitindo acesso`);
      return true;
    }

    // Usuário não tem a role esperada, redirecionar ao dashboard apropriado com base em sua role
    console.warn(`RoleGuard: acesso negado. Role esperada: ${expectedRole}, Roles do usuário: ${this.authService.currentUserValue?.roles?.join(', ')}`);
    
    // Redirecionar para o dashboard correspondente à role do usuário
    if (this.authService.hasRole('ADMIN')) {
      this.router.navigate(['/dashboard/admin']);
    } else if (this.authService.hasRole('PROFESSOR')) {
      this.router.navigate(['/dashboard/professor']);
    } else if (this.authService.hasRole('ALUNO')) {
      this.router.navigate(['/dashboard/aluno']);
    } else {
      // Caso não tenha nenhuma role válida, redirecionar para home
      this.router.navigate(['/home']);
    }
    
    return false;
  }
}