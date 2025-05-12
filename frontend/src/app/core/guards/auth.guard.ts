// src/app/core/guards/auth.guard.ts
import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router, UrlTree } from '@angular/router';
import { Observable } from 'rxjs';
import { AuthService } from '../auth.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {

  constructor(private authService: AuthService, private router: Router) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    
    console.log('AuthGuard verificando se o usuário está autenticado');
    console.log('Usuário atual:', this.authService.currentUserValue?.username);
    console.log('Token válido:', !!this.authService.getToken());
    
    if (this.authService.isLoggedIn()) {
      console.log('AuthGuard: usuário autenticado, permitindo acesso');
      return true;
    }

    console.log('AuthGuard: usuário não autenticado, redirecionando para login');
    // Salva a URL que o usuário tentou acessar para redirecionamento após login
    this.router.navigate(['/auth/login'], { queryParams: { returnUrl: state.url }});
    return false;
  }
}