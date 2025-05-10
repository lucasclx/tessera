// src/app/core/admin.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface User {
  id: number;
  nome: string;
  username: string;
  email: string;
  institution: string;
  role: string;
  requestedRole: string | null;
  approved: boolean;
  approvalDate: string | null;
  adminComments: string | null;
  enabled: boolean;
  createdAt: string;
}

export interface UserApprovalRequest {
  approved: boolean;
  role?: string;
  adminComments?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private apiUrl = `${environment.apiUrl}/admin`;

  constructor(private http: HttpClient) { }

  /**
   * Obtém todos os usuários
   */
  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}/users`);
  }

  /**
   * Obtém usuários pendentes de aprovação
   */
  getPendingUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}/users/pending`);
  }

  /**
   * Obtém detalhes de um usuário específico
   */
  getUserDetails(userId: number): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/users/${userId}`);
  }

  /**
   * Atualiza o status de aprovação de um usuário
   */
  updateUserApproval(userId: number, approvalData: UserApprovalRequest): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/users/${userId}/approval`, approvalData);
  }

  /**
   * Atualiza o status de ativação de um usuário
   */
  updateUserStatus(userId: number, enabled: boolean): Observable<User> {
    const params = new HttpParams().set('enabled', enabled.toString());
    return this.http.put<User>(`${this.apiUrl}/users/${userId}/status`, {}, { params });
  }

  /**
   * Deleta um usuário
   */
  deleteUser(userId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/users/${userId}`);
  }
}