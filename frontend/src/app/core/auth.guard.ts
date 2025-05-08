import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router, UrlTree } from '@angular/router'; // Router e UrlTree podem ser necessários
import { Observable } from 'rxjs';
import { AuthService } from './auth.service'; // Importar AuthService

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate { // Deve implementar CanActivate

  constructor(private authService: AuthService, private router: Router) {} // Injetar AuthService e Router

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    
    if (this.authService.isLoggedIn()) {
      return true; // Usuário logado, pode acessar a rota
    }

    // Usuário não logado, redirecionar para a página de login
    // Passar a URL de retorno para que o usuário seja redirecionado de volta após o login
    this.router.navigate(['/auth/login'], { queryParams: { returnUrl: state.url }});
    return false;
  }
}