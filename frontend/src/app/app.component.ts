// src/app/app.component.ts
import { Component, OnInit, OnDestroy, Renderer2, Inject, HostBinding } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule, DOCUMENT } from '@angular/common';
import { AuthService } from './core/auth.service';
import { Subscription } from 'rxjs';
import { MaterialModule } from './material.module'; // Importar para usar componentes no template, se necessário
import { OverlayContainer } from '@angular/cdk/overlay';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MaterialModule // Adicionado para <mat-toolbar>, <mat-icon>, <mat-menu> etc.
  ],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'] // Corrigido de styleUrl para styleUrls
})
export class AppComponent implements OnInit, OnDestroy {
  title = 'Sistema Acadêmico';
  isLoggedIn = false;
  username: string | null = null;
  userRole: string | null = null;

  private authSubscription?: Subscription;

  // Para alternar tema escuro
  @HostBinding('class') className = '';
  isDarkMode = false;

  constructor(
    private authService: AuthService,
    private renderer: Renderer2,
    @Inject(DOCUMENT) private document: Document,
    private overlayContainer: OverlayContainer
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

    // Opcional: verificar preferência de tema do sistema ou localStorage
    const storedTheme = localStorage.getItem('theme');
    if (storedTheme === 'dark') {
      this.enableDarkTheme(true);
    } else if (storedTheme === 'light') {
      this.enableDarkTheme(false);
    } else if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
      this.enableDarkTheme(true); // Padrão para tema escuro do sistema
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

  toggleTheme() {
    this.enableDarkTheme(!this.isDarkMode);
  }

  private enableDarkTheme(isDark: boolean) {
    this.isDarkMode = isDark;
    const themeClass = this.isDarkMode ? 'dark-theme-mode' : '';
    this.className = themeClass; // Aplica no host <app-root>
    
    // Aplica também no body para estilos globais e overlays do Material
    if (isDark) {
      this.renderer.addClass(this.document.body, 'dark-theme-mode');
      this.overlayContainer.getContainerElement().classList.add('dark-theme-mode');
    } else {
      this.renderer.removeClass(this.document.body, 'dark-theme-mode');
      this.overlayContainer.getContainerElement().classList.remove('dark-theme-mode');
    }
    localStorage.setItem('theme', isDark ? 'dark' : 'light');
  }

  getDashboardLink(): string {
    const role = this.authService.getUserRole();
    switch (role) {
      case 'ADMIN': return '/dashboard/admin';
      case 'PROFESSOR': return '/dashboard/professor';
      case 'ALUNO': return '/dashboard/aluno';
      default: return '/home'; // Redireciona para home se não houver dashboard
    }
  }

  getRoleBadgeClass(): string {
    const role = this.authService.getUserRole()?.toLowerCase();
    return role ? `badge-${role}` : '';
  }
}