// src/app/app.component.ts
import { Component, OnInit, OnDestroy } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from './core/auth.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit, OnDestroy {
  title = 'Sistema Acadêmico';
  isLoggedIn = false;
  username: string | null = null;
  userRole: string | null = null;
  
  private authSubscription?: Subscription;
  
  constructor(private authService: AuthService) {}
  
  ngOnInit() {
    // Monitorar mudanças no estado de autenticação
    this.authSubscription = this.authService.currentUser.subscribe(user => {
      this.isLoggedIn = !!user;
      this.username = user?.username || null;
      this.userRole = this.authService.getUserRole();
    });
    
    // Verificar estado inicial
    this.isLoggedIn = this.authService.isLoggedIn();
    if (this.isLoggedIn) {
      this.username = this.authService.currentUserValue?.username || null;
      this.userRole = this.authService.getUserRole();
    }
  }
  
  ngOnDestroy() {
    // Cancelar a inscrição para evitar memory leaks
    if (this.authSubscription) {
      this.authSubscription.unsubscribe();
    }
  }
  
  logout(event: Event) {
    event.preventDefault();
    this.authService.logout();
  }
  
  getDashboardLink(): string {
    // Retorna o link apropriado para o dashboard com base na role
    const role = this.authService.getUserRole();
    
    switch (role) {
      case 'ADMIN':
        return '/dashboard/admin';
      case 'PROFESSOR':
        return '/dashboard/professor';
      case 'ALUNO':
        return '/dashboard/aluno';
      default:
        return '/dashboard';
    }
  }
  
  getRoleBadgeClass(): string {
    // Retorna a classe CSS para o badge da role
    const role = this.authService.getUserRole();
    
    switch (role) {
      case 'ADMIN':
        return 'admin';
      case 'PROFESSOR':
        return 'professor';
      case 'ALUNO':
        return 'aluno';
      default:
        return '';
    }
  }
}