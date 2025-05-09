// src/app/dashboard/professor-dashboard/professor-dashboard.component.ts
import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../core/auth.service';
import { environment } from '../../../environments/environment';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-professor-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule
  ],
  templateUrl: './professor-dashboard.component.html',
  styleUrl: './professor-dashboard.component.scss'
})
export class ProfessorDashboardComponent implements OnInit {
  username: string | null = null;
  dashboardData: string | null = null;
  errorMessage: string | null = null;
  loading: boolean = false;

  constructor(private authService: AuthService, private http: HttpClient) {}

  ngOnInit(): void {
    // Obter o nome de usuário autenticado
    this.username = this.authService.currentUserValue?.username || 'Professor';
    // Carregar dados do servidor
    this.loadProfessorData();
  }

  loadProfessorData(): void {
    this.loading = true;
    this.http.get(`${environment.apiUrl}/dashboard/professor/data`, { responseType: 'text' })
      .subscribe({
        next: data => {
          this.dashboardData = data;
          this.errorMessage = null;
          this.loading = false;
        },
        error: err => {
          console.error('Erro ao buscar dados do professor:', err);
          this.dashboardData = null;
          this.errorMessage = `Erro ao carregar dados do professor: ${err.error?.message || err.message || 'Serviço indisponível'}`;
          this.loading = false;
        }
      });
  }

  logout(): void {
    this.authService.logout();
  }
}