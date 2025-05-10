// src/app/shared/components/header/header.component.ts
import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MaterialModule } from '../../material.module';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MaterialModule
  ],
  template: `
    <header class="header">
      <div class="header-container">
        <a class="logo" routerLink="/">Sistema Acadêmico</a>
        <nav class="nav-menu">
          <a routerLink="/home" routerLinkActive="active">Home</a>
          <a routerLink="/auth/login" routerLinkActive="active">Login</a>
          <a routerLink="/auth/register" routerLinkActive="active">Registrar</a>
        </nav>
      </div>
    </header>
  `,
  styles: [`
    .header {
      background-color: #3f51b5;
      color: white;
      padding: 1rem;
    }
    
    .header-container {
      display: flex;
      justify-content: space-between;
      align-items: center;
      max-width: 1200px;
      margin: 0 auto;
    }
    
    .logo {
      font-size: 1.5rem;
      font-weight: bold;
      color: white;
      text-decoration: none;
    }
    
    .nav-menu {
      display: flex;
      gap: 1rem;
    }
    
    .nav-menu a {
      color: white;
      text-decoration: none;
      padding: 0.5rem;
      border-radius: 4px;
      transition: background-color 0.3s;
    }
    
    .nav-menu a:hover, .nav-menu a.active {
      background-color: rgba(255, 255, 255, 0.1);
    }
  `]
})
export class HeaderComponent {}