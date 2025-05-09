// src/app/core/auth.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { BehaviorSubject, Observable, of, throwError } from 'rxjs';
import { map, tap, catchError, switchMap } from 'rxjs/operators';
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
  institution: string;
  role: Set<string>;
}

export enum ApprovalStatus {
  APPROVED = 'APPROVED',
  PENDING_APPROVAL = 'PENDING_APPROVAL',
  ACCOUNT_DISABLED = 'ACCOUNT_DISABLED',
  UNKNOWN = 'UNKNOWN'
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
    
    console.log('AuthService inicializado. Usuário atual:', this.currentUserValue);
  }

  public get currentUserValue(): AuthResponse | null {
    return this.currentUserSubject.value;
  }

  /**
   * Verifica o status de aprovação de um usuário
   * @param username Nome de usuário a verificar
   * @returns Status de aprovação como enum ApprovalStatus
   */
  checkApprovalStatus(username: string): Observable<ApprovalStatus> {
    return this.http.get<any>(`${this.apiUrl}/check-approval/${username}`).pipe(
      map(response => {
        return ApprovalStatus.APPROVED;
      }),
      catchError(error => {
        if (error.status === 403) {
          if (error.error?.message === 'PENDING_APPROVAL') {
            return of(ApprovalStatus.PENDING_APPROVAL);
          } else if (error.error?.message === 'ACCOUNT_DISABLED') {
            return of(ApprovalStatus.ACCOUNT_DISABLED);
          }
        }
        return of(ApprovalStatus.UNKNOWN);
      })
    );
  }

  login(credentials: LoginData): Observable<AuthResponse> {
    console.log('Enviando requisição de login para:', `${this.apiUrl}/login`);
    console.log('Credenciais:', {username: credentials.username, password: '******'});
    
    // Primeiro verificamos o status de aprovação
    return this.checkApprovalStatus(credentials.username).pipe(
      switchMap(status => {
        if (status === ApprovalStatus.PENDING_APPROVAL) {
          // Redirecionar para página de "aguardando aprovação"
          this.router.navigate(['/auth/pending-approval'], { 
            queryParams: { username: credentials.username }
          });
          return throwError(() => new Error('Sua conta está aguardando aprovação do administrador.'));
        } else if (status === ApprovalStatus.ACCOUNT_DISABLED) {
          return throwError(() => new Error('Sua conta está desativada. Entre em contato com o administrador.'));
        }
        
        // Se estiver aprovado ou status desconhecido, prosseguimos com o login normal
        return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials)
          .pipe(
            tap(user => {
              console.log('Resposta do login:', user);
              if (user && user.token) {
                localStorage.setItem('currentUser', JSON.stringify(user));
                this.currentUserSubject.next(user);
                console.log('Token armazenado no localStorage e currentUserSubject atualizado');
              }
            })
          );
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
    console.log('Realizando logout');
    localStorage.removeItem('currentUser');
    this.currentUserSubject.next(null);
    this.router.navigate(['/auth/login']);
  }

  isLoggedIn(): boolean {
    const isLogged = !!this.currentUserValue && !!this.currentUserValue.token;
    console.log('isLoggedIn check:', isLogged);
    return isLogged;
  }

  hasRole(role: string): boolean {
    if (!this.isLoggedIn() || !this.currentUserValue?.roles) {
      console.log('hasRole - Usuário não logado ou sem roles');
      return false;
    }
    // As roles vêm do backend como "PROFESSOR", "ALUNO" (sem "ROLE_")
    const hasRole = this.currentUserValue.roles.includes(role.toUpperCase());
    console.log(`hasRole check para ${role}:`, hasRole, 'Roles disponíveis:', this.currentUserValue.roles);
    return hasRole;
  }

  /**
   * Retorna a role principal do usuário atual
   * @returns string com a role principal ou null se não estiver logado
   */
  getUserRole(): string | null {
    if (!this.isLoggedIn() || !this.currentUserValue?.roles || this.currentUserValue.roles.length === 0) {
      return null;
    }
    
    // Ordem de prioridade: ADMIN > PROFESSOR > ALUNO
    if (this.hasRole('ADMIN')) {
      return 'ADMIN';
    } else if (this.hasRole('PROFESSOR')) {
      return 'PROFESSOR';
    } else if (this.hasRole('ALUNO')) {
      return 'ALUNO';
    }
    
    // Retorna a primeira role disponível se nenhuma das principais
    return this.currentUserValue.roles[0];
  }

  getToken(): string | null {
    const token = this.currentUserValue?.token || null;
    console.log('getToken:', token ? token.substring(0, 15) + '...' : 'null');
    return token;
  }
}