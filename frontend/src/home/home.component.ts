// src/home/home.component.ts
import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MaterialModule } from '../app/material.module'; // Importe se for usar componentes Material

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MaterialModule // Adicionado para <mat-card>, <button mat-stroked-button> etc.
  ],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss'] // Corrigido para styleUrls
})
export class HomeComponent {
  // Implementação básica, pode ser estendida conforme necessário
}