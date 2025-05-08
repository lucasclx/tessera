// src/app/home/home.component.ts
import { Component } from '@angular/core';
import { RouterLink } from '@angular/router'; // Necessário para routerLink

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    RouterLink // Adicione RouterLink aos imports
  ],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent {

}