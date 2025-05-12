// src/app/core/interceptors/jwt.interceptor.ts
import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from '../auth.service';
import { environment } from '../../../environments/environment';
import { Router } from '@angular/router';

@Injectable()
export class JwtInterceptor implements HttpInterceptor {

  constructor(private authService: AuthService, private router: Router) {}

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const token = this.authService.getToken();
    const isLoggedIn = this.authService.isLoggedIn();
    const isApiUrl = request.url.startsWith(environment.apiUrl);
    const isAuthRequest = request.url.includes('/auth/'); // Não adiciona token em requisições de autenticação
    
    if (isLoggedIn && isApiUrl && token && !isAuthRequest) {
      console.log(`JwtInterceptor: Adicionando token à requisição ${request.url}`);
      request = request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }

    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        // Interceptar erros 401 (Unauthorized) e redirecionar para login
        if (error.status === 401) {
          console.warn('JwtInterceptor: Erro 401 (Unauthorized), redirecionando para login');
          this.authService.logout();
          this.router.navigate(['/auth/login'], { 
            queryParams: { 
              returnUrl: this.router.url, 
              error: 'Sua sessão expirou. Por favor, faça login novamente.' 
            } 
          });
        }
        
        // Interceptar erros 403 (Forbidden)
        if (error.status === 403) {
          console.warn('JwtInterceptor: Erro 403 (Forbidden)', error.error);
          // Verificar se o erro é devido a uma aprovação pendente
          if (error.error?.message === 'PENDING_APPROVAL') {
            const username = this.authService.currentUserValue?.username;
            if (username) {
              this.router.navigate(['/auth/pending-approval'], { 
                queryParams: { username }
              });
              return throwError(() => new Error('Conta aguardando aprovação.'));
            }
          }
          
          // Outros erros 403 - acesso negado
          this.router.navigate(['/access-denied']);
        }
        
        return throwError(() => error);
      })
    );
  }
}