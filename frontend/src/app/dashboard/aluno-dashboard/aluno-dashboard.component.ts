import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../core/auth.service';
import { environment } from '../../../environments/environment';
import { CommonModule } from '@angular/common'; // Importe CommonModule se usar *ngIf, etc.

@Component({
  selector: 'app-aluno-dashboard',
  standalone: true, // Assumindo que é standalone
  imports: [
    CommonModule // Adicione se usar diretivas do CommonModule no template
  ],
  templateUrl: './aluno-dashboard.component.html',
  styleUrl: './aluno-dashboard.component.scss'
})
export class AlunoDashboardComponent implements OnInit {
  username: string | null = null;
  dashboardData: string | null = null;
  errorMessage: string | null = null;

  constructor(private authService: AuthService, private http: HttpClient) {}

  ngOnInit(): void {
    this.username = this.authService.currentUserValue?.username || 'Aluno';
    this.loadAlunoData();
  }

  loadAlunoData(): void {
    this.http.get(`${environment.apiUrl}/dashboard/aluno/data`, { responseType: 'text' })
      .subscribe({
        next: data => {
          this.dashboardData = data;
          this.errorMessage = null;
        },
        error: err => {
          console.error('Erro ao buscar dados do aluno:', err);
          this.dashboardData = null;
          this.errorMessage = `Erro ao carregar dados do aluno: ${err.error?.message || err.message || 'Serviço indisponível'}`;
        }
      });
  }

  logout(): void {
    this.authService.logout();
  }
}