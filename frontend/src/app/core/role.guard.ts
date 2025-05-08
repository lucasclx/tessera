import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router, UrlTree } from '@angular/router'; // Importar Router, UrlTree
import { Observable } from 'rxjs';
import { AuthService } from './auth.service'; // Importar AuthService

@Injectable({
  providedIn: 'root'
})
export class RoleGuard implements CanActivate { // Deve implementar CanActivate

  constructor(private authService: AuthService, private router: Router) {} // Injetar AuthService e Router

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {

    // A role esperada será passada via 'data' na configuração da rota
    const expectedRole = route.data['expectedRole'] as string;

    if (!this.authService.isLoggedIn()) {
      // Não deveria chegar aqui se o AuthGuard estiver protegendo a rota pai, mas é uma boa prática
      this.router.navigate(['/auth/login'], { queryParams: { returnUrl: state.url }});
      return false;
    }

    if (this.authService.hasRole(expectedRole)) {
      return true; // Usuário tem a role esperada, pode acessar
    }

    // Usuário não tem a role esperada
    console.warn(`Acesso negado - RoleGuard. Role esperada: ${expectedRole}, Roles do usuário: ${this.authService.currentUserValue?.roles?.join(', ')}`);
    // Redirecionar para uma página de "acesso negado" ou para o dashboard padrão ou login
    this.router.navigate(['/dashboard']); // Ou para uma página '/unauthorized' ou de volta para o login
    return false;
  }
}