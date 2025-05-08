// src/app/core/jwt.interceptor.ts
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
import { AuthService } from './auth.service';
import { environment } from '../../environments/environment';
import { Router } from '@angular/router';

@Injectable()
export class JwtInterceptor implements HttpInterceptor {

  constructor(private authService: AuthService, private router: Router) {}

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const token = this.authService.getToken();
    const isLoggedIn = this.authService.isLoggedIn();
    const isApiUrl = request.url.startsWith(environment.apiUrl);

    if (isLoggedIn && isApiUrl && token) {
      request = request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }

    return next.handle(request).pipe(
      catchError((err: HttpErrorResponse) => {
        // Intercepta erros 401 (Unauthorized) e redireciona para login
        if (err.status === 401) {
          console.log('JwtInterceptor: Token expirado ou inválido, redirecionando para login');
          this.authService.logout();
          this.router.navigate(['/auth/login']);
        }
        
        // Intercepta erros 403 (Forbidden) e redireciona para a página de acesso negado
        if (err.status === 403) {
          console.log('JwtInterceptor: Acesso negado (403 Forbidden)');
          // Opcionalmente redirecionar para uma página de acesso negado
          // this.router.navigate(['/unauthorized']);
        }
        
        return throwError(() => err);
      })
    );
  }
}