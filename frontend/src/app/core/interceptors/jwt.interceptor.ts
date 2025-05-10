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

@Injectable({
  providedIn: 'root'
})
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

// REMOVIDO: Código problemático que usa inject() e Injector