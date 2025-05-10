// src/app/app.component.ts
import { Component, OnInit, OnDestroy } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from './core/auth.service';
import { Subscription } from 'rxjs';
import { MaterialModule } from './material.module';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MaterialModule
  ],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit, OnDestroy {
  title = 'Sistema Acadêmico';
  isLoggedIn = false;
  username: string | null = null;
  userRole: string | null = null;

  private authSubscription?: Subscription;

  constructor(
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.authSubscription = this.authService.currentUser.subscribe(user => {
      this.isLoggedIn = !!user;
      this.username = user?.username || null;
      this.userRole = this.authService.getUserRole();
    });

    this.isLoggedIn = this.authService.isLoggedIn();
    if (this.isLoggedIn) {
      this.username = this.authService.currentUserValue?.username || null;
      this.userRole = this.authService.getUserRole();
    }
  }

  ngOnDestroy() {
    if (this.authSubscription) {
      this.authSubscription.unsubscribe();
    }
  }

  logout(event: Event) {
    event.preventDefault();
    this.authService.logout();
  }

  getDashboardLink(): string {
    const role = this.authService.getUserRole();
    switch (role) {
      case 'ADMIN': return '/dashboard/admin';
      case 'PROFESSOR': return '/dashboard/professor';
      case 'ALUNO': return '/dashboard/aluno';
      default: return '/home';
    }
  }

  getRoleBadgeClass(): string {
    const role = this.authService.getUserRole()?.toLowerCase();
    return role ? `badge-${role}` : '';
  }
}