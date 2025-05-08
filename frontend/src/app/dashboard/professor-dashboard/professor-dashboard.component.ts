import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../core/auth.service';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-professor-dashboard',
  // REMOVA a linha 'standalone: true,' daqui
  // REMOVA a linha 'imports: [CommonModule],' daqui
  templateUrl: './professor-dashboard.component.html',
  styleUrl: './professor-dashboard.component.scss' // ou styleUrls
})
export class ProfessorDashboardComponent implements OnInit {
  username: string | null = null;
  dashboardData: string | null = null;
  errorMessage: string | null = null;

  constructor(private authService: AuthService, private http: HttpClient) {}

  ngOnInit(): void {
    this.username = this.authService.currentUserValue?.username || 'Professor';
    this.loadProfessorData();
  }

  loadProfessorData(): void {
    this.http.get(`${environment.apiUrl}/dashboard/professor/data`, { responseType: 'text' })
      .subscribe({
        next: data => {
          this.dashboardData = data;
          this.errorMessage = null;
        },
        error: err => {
          console.error('Erro ao buscar dados do professor:', err);
          this.dashboardData = null;
          this.errorMessage = `Erro ao carregar dados do professor: ${err.error?.message || err.message || 'Serviço indisponível'}`;
        }
      });
  }

  logout(): void {
    this.authService.logout();
  }
}