// src/app/core/role.guard.ts
import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router, UrlTree } from '@angular/router';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class RoleGuard implements CanActivate {

  constructor(private authService: AuthService, private router: Router) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {

    // A role esperada é passada via 'data' na configuração da rota
    const expectedRole = route.data['expectedRole'] as string;

    if (!this.authService.isLoggedIn()) {
      console.log('RoleGuard: usuário não autenticado, redirecionando para login');
      this.router.navigate(['/auth/login'], { queryParams: { returnUrl: state.url }});
      return false;
    }

    if (this.authService.hasRole(expectedRole)) {
      console.log(`RoleGuard: usuário tem a role ${expectedRole}, permitindo acesso`);
      return true;
    }

    // Usuário não tem a role esperada
    console.warn(`RoleGuard: acesso negado. Role esperada: ${expectedRole}, Roles do usuário: ${this.authService.currentUserValue?.roles?.join(', ')}`);
    
    // Verifica se é aluno tentando acessar área de professor ou vice-versa
    if (expectedRole === 'PROFESSOR' && this.authService.hasRole('ALUNO')) {
      this.router.navigate(['/dashboard/aluno']);
    } else if (expectedRole === 'ALUNO' && this.authService.hasRole('PROFESSOR')) {
      this.router.navigate(['/dashboard/professor']);
    } else {
      // Caso não tenha nenhuma role válida
      this.router.navigate(['/home']);
    }
    
    return false;
  }
}