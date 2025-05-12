// src/app/core/auth.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, of, throwError } from 'rxjs';
import { map, tap, catchError, switchMap } from 'rxjs/operators';
import { Router } from '@angular/router';
import { environment } from '../../environments/environment';

export interface AuthResponse {
  token: string;
  username: string;
  roles: string[];
  id: number;
}

export interface LoginData {
  username: string;
  password: string;
}

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
  ROLE_MISSING = 'ROLE_MISSING',
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
    console.log('AuthService: Inicializado. Usuário atual (localStorage):', this.currentUserValue?.username);
  }

  public get currentUserValue(): AuthResponse | null {
    return this.currentUserSubject.value;
  }

  checkApprovalStatus(username: string): Observable<ApprovalStatus> {
    console.log(`AuthService: Verificando status de aprovação para: ${username}`);
    return this.http.get<any>(`${this.apiUrl}/check-approval/${username}`).pipe(
      map(response => {
        console.log('AuthService: Resposta do check-approval:', response);
        if (response && response.message === 'APPROVED') return ApprovalStatus.APPROVED;
        if (response && response.message === 'ROLE_MISSING') return ApprovalStatus.ROLE_MISSING;
        return ApprovalStatus.UNKNOWN;
      }),
      catchError(error => {
        console.warn('AuthService: Erro ao verificar status de aprovação:', error);
        if (error.status === 403) {
          if (error.error?.message === 'PENDING_APPROVAL') return of(ApprovalStatus.PENDING_APPROVAL);
          if (error.error?.message === 'ACCOUNT_DISABLED') return of(ApprovalStatus.ACCOUNT_DISABLED);
          if (error.error?.message === 'ROLE_MISSING') return of(ApprovalStatus.ROLE_MISSING);
        }
        return of(ApprovalStatus.UNKNOWN);
      })
    );
  }

  login(credentials: LoginData): Observable<AuthResponse> {
    console.log('AuthService: Iniciando processo de login para usuário:', credentials.username);
    return this.checkApprovalStatus(credentials.username).pipe(
      switchMap(status => {
        console.log(`AuthService: Status de aprovação para ${credentials.username}: ${status}`);
        if (status === ApprovalStatus.PENDING_APPROVAL) {
          this.router.navigate(['/auth/pending-approval'], { queryParams: { username: credentials.username } });
          return throwError(() => ({ message: 'Sua conta está aguardando aprovação do administrador.', status: 'PENDING_APPROVAL' }));
        } else if (status === ApprovalStatus.ACCOUNT_DISABLED) {
          return throwError(() => ({ message: 'Sua conta está desativada. Entre em contato com o administrador.', status: 'ACCOUNT_DISABLED' }));
        } else if (status === ApprovalStatus.ROLE_MISSING) {
            return throwError(() => ({ message: 'Sua conta foi aprovada mas não possui um papel (role) atribuído. Entre em contato com o administrador.', status: 'ROLE_MISSING' }));
        }
        
        console.log(`AuthService: Status OK para ${credentials.username}, prosseguindo com POST /login.`);
        return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials).pipe(
          tap(user => {
            console.log('AuthService: Resposta da API de login bem-sucedida:', user);
            if (user && user.token) {
              localStorage.setItem('currentUser', JSON.stringify(user));
              this.currentUserSubject.next(user);
              console.log('AuthService: Usuário autenticado e token armazenado. Username:', user.username, 'Roles:', user.roles);
            } else {
              console.warn('AuthService: Resposta de login não continha token ou dados do usuário esperados.', user);
              // Lançar um erro aqui pode ser uma boa ideia se um token é sempre esperado
              // return throwError(() => new Error('Resposta de login inválida do servidor.'));
            }
          }),
          catchError(err => {
            console.error('AuthService: Erro na chamada HTTP POST /login:', err);
            return throwError(() => err); // Propagar o erro para o componente tratar
          })
        );
      }),
      catchError(err => { 
        console.error('AuthService: Erro capturado durante checkApprovalStatus ou switchMap:', err.message || err);
        return throwError(() => err); 
      })
    );
  }

  register(data: RegistrationData): Observable<any> {
    const requestData = {
      ...data,
      role: Array.from(data.role)
    };
    console.log('AuthService: Enviando requisição de registro:', requestData);
    return this.http.post(`${this.apiUrl}/register`, requestData).pipe(
      tap(response => {
        console.log('AuthService: Resposta do registro:', response);
      }),
      catchError(err => {
        console.error('AuthService: Erro no registro:', err);
        return throwError(() => err);
      })
    );
  }

  logout(): void {
    console.log('AuthService: Realizando logout para usuário:', this.currentUserValue?.username);
    localStorage.removeItem('currentUser');
    this.currentUserSubject.next(null);
    this.router.navigate(['/auth/login']);
  }

  isLoggedIn(): boolean {
    const user = this.currentUserValue;
    return !!user && !!user.token;
  }

  getToken(): string | null {
    return this.currentUserValue?.token || null;
  }

  getUserRole(): string | null {
    const user = this.currentUserValue;
    if (!user || !user.roles || user.roles.length === 0) {
      return null;
    }
    const normalizedRoles = user.roles.map(r => r.toUpperCase().replace(/^ROLE_/, ''));
    if (normalizedRoles.includes('ADMIN')) return 'ADMIN';
    if (normalizedRoles.includes('PROFESSOR')) return 'PROFESSOR';
    if (normalizedRoles.includes('ALUNO')) return 'ALUNO';
    return normalizedRoles[0] || null;
  }

  hasRole(role: string): boolean {
    const user = this.currentUserValue;
    if (!user || !user.roles) {
      return false;
    }
    const roleToCheckNormalized = role.toUpperCase().replace(/^ROLE_/, '');
    const userRolesNormalized = user.roles.map(r => r.toUpperCase().replace(/^ROLE_/, ''));
    return userRolesNormalized.includes(roleToCheckNormalized);
  }
}