// src/app/core/auth.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { Router } from '@angular/router';
import { environment } from '../../environments/environment';

export interface AuthResponse {
  token: string;
  username: string;
  roles: string[];
}

// Interface para os dados de login
export interface LoginData {
  username: string;
  password: string;
}

// Interface para os dados de registro
export interface RegistrationData {
  nome: string;
  username: string;
  email: string;
  password: string;
  institution: string; // Campo obrigatório
  role: Set<string>; // Tipo específico Set<string>
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = `${environment.apiUrl}/auth`;
  private currentUserSubject: BehaviorSubject<AuthResponse | null>;
  public currentUser: Observable<AuthResponse | null>;

  constructor(private http: HttpClient, private router: Router) {
    const storedUser = localStorage.getItem('currentUser');
    this.currentUserSubject = new BehaviorSubject<AuthResponse | null>(
      storedUser ? JSON.parse(storedUser) : null
    );
    this.currentUser = this.currentUserSubject.asObservable();
  }

  public get currentUserValue(): AuthResponse | null {
    return this.currentUserSubject.value;
  }

  login(credentials: LoginData): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials)
      .pipe(
        tap(user => {
          if (user && user.token) {
            localStorage.setItem('currentUser', JSON.stringify(user));
            this.currentUserSubject.next(user);
          }
        })
      );
  }

  register(data: RegistrationData): Observable<any> {
    // Convertemos o Set para um formato que JSON possa serializar (Array)
    const requestData = {
      ...data,
      role: Array.from(data.role)
    };
    
    console.log('Enviando requisição de registro:', requestData);
    
    return this.http.post(`${this.apiUrl}/register`, requestData, {
      headers: new HttpHeaders({ 'Content-Type': 'application/json' })
    });
  }

  logout(): void {
    localStorage.removeItem('currentUser');
    this.currentUserSubject.next(null);
    this.router.navigate(['/auth/login']);
  }

  isLoggedIn(): boolean {
    return !!this.currentUserValue && !!this.currentUserValue.token;
  }

  hasRole(role: string): boolean {
    if (!this.isLoggedIn() || !this.currentUserValue?.roles) {
      return false;
    }
    // As roles vêm do backend como "PROFESSOR", "ALUNO" (sem "ROLE_")
    return this.currentUserValue.roles.includes(role.toUpperCase());
  }

  getToken(): string | null {
    return this.currentUserValue?.token || null;
  }
}