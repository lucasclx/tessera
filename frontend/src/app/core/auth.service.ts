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
  id: number; // <--- ADICIONADO ID DO USUÁRIO
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
    console.log(`Verificando status de aprovação para: ${username}`);

    return this.http.get<any>(`${this.apiUrl}/check-approval/${username}`).pipe(
      map(response => {
        console.log('Resposta do check-approval:', response);
        if (response && response.message === 'APPROVED') {
          return ApprovalStatus.APPROVED;
        } else if (response && response.message === 'ROLE_MISSING') {
          return ApprovalStatus.ROLE_MISSING;
        }
        return ApprovalStatus.UNKNOWN;
      }),
      catchError(error => {
        console.log('Erro ao verificar status de aprovação:', error);

        if (error.status === 403) {
          if (error.error && error.error.message === 'PENDING_APPROVAL') {
            return of(ApprovalStatus.PENDING_APPROVAL);
          } else if (error.error && error.error.message === 'ACCOUNT_DISABLED') {
            return of(ApprovalStatus.ACCOUNT_DISABLED);
          } else if (error.error && error.error.message === 'ROLE_MISSING') {
            return of(ApprovalStatus.ROLE_MISSING);
          }
        } else if (error.status === 404) {
          console.log('Usuário não encontrado');
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
        console.log('Status de aprovação retornado:', status);

        if (status === ApprovalStatus.PENDING_APPROVAL) {
          // Redirecionar para página de "aguardando aprovação"
          this.router.navigate(['/auth/pending-approval'], {
            queryParams: { username: credentials.username }
          });
          return throwError(() => new Error('Sua conta está aguardando aprovação do administrador.'));
        } else if (status === ApprovalStatus.ACCOUNT_DISABLED) {
          return throwError(() => new Error('Sua conta está desativada. Entre em contato com o administrador.'));
        } else if (status === ApprovalStatus.ROLE_MISSING) {
          return throwError(() => new Error('Sua conta foi aprovada mas não possui um papel atribuído. Entre em contato com o administrador.'));
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
                console.log('Token e roles armazenadas:', user.token, user.roles);
              }
            }),
            catchError(err => {
              console.error('Erro durante login:', err);

              // Verificar se o erro é devido a aprovação pendente
              if (err.status === 403 && err.error?.message?.includes('aguardando aprovação')) {
                this.router.navigate(['/auth/pending-approval'], {
                  queryParams: { username: credentials.username }
                });
                return throwError(() => new Error('Sua conta está aguardando aprovação do administrador.'));
              }

              // Propagar outros erros
              return throwError(() => err);
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

    return this.http.post(`${this.apiUrl}/register`, requestData).pipe(
      tap(response => {
        console.log('Resposta do registro:', response);
      }),
      catchError(err => {
        console.error('Erro no registro:', err);
        return throwError(() => err);
      })
    );
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

    // Normalizar as roles para comparação
    const userRoles = this.currentUserValue.roles.map(r => 
      r.toUpperCase().replace('ROLE_', '')
    );
    const roleToCheck = role.toUpperCase().replace('ROLE_', '');
    
    const hasRole = userRoles.includes(roleToCheck);

    console.log(`hasRole check para ${role}:`, hasRole, 'Role principal:', this.getUserRole());
    console.log('Roles disponíveis (normalizadas):', userRoles);
    console.log('Role verificada (normalizada):', roleToCheck);
    
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

    console.log('Roles originais disponíveis:', this.currentUserValue.roles);
    
    // Normalizar as roles removendo o prefixo "ROLE_" se existir
    const normalizedRoles = this.currentUserValue.roles.map(r => 
      r.toUpperCase().replace('ROLE_', '')
    );
    
    console.log('Roles normalizadas:', normalizedRoles);
    
    // Ordem de prioridade: ADMIN > PROFESSOR > ALUNO
    if (normalizedRoles.includes('ADMIN')) {
      return 'ADMIN';
    } else if (normalizedRoles.includes('PROFESSOR')) {
      return 'PROFESSOR';
    } else if (normalizedRoles.includes('ALUNO')) {
      return 'ALUNO';
    }

    // Retorna a primeira role disponível se nenhuma das principais
    return normalizedRoles[0];
  }

  getToken(): string | null {
    const token = this.currentUserValue?.token || null;
    // console.log('getToken:', token ? token.substring(0, 15) + '...' : 'null'); // Removido log frequente
    return token;
  }
}