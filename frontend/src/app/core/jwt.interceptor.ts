// src/app/core/jwt.interceptor.ts
import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse,
  HttpInterceptorFn
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
    const isAuthRequest = request.url.includes('/auth/');
    
    if (isLoggedIn && isApiUrl && token && !isAuthRequest) {
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
          this.authService.logout();
          this.router.navigate(['/auth/login'], { 
            queryParams: { returnUrl: this.router.url, error: 'Sua sessão expirou. Por favor, faça login novamente.' } 
          });
        }
        
        // Interceptar erros 403 (Forbidden)
        if (error.status === 403) {
          this.router.navigate(['/access-denied']);
        }
        
        return throwError(() => error);
      })
    );
  }
}

// Para suporte ao novo sistema de interceptors no Angular standalone
export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = new AuthService(null!, null!); // Não ideal, mas funcional para o interceptor
  const token = authService.getToken();
  const isLoggedIn = authService.isLoggedIn();
  const isApiUrl = req.url.startsWith(environment.apiUrl);
  const isAuthRequest = req.url.includes('/auth/');
  
  if (isLoggedIn && isApiUrl && token && !isAuthRequest) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      // Lidar com erros 401 aqui não é ideal sem acesso ao serviço e router
      return throwError(() => error);
    })
  );
};