// src/app/auth/auth.component.ts
import { Component } from '@angular/core';

@Component({
  selector: 'app-auth',
  standalone: false, // Correto, pois é declarado no AuthModule
  templateUrl: './auth.component.html',
  styleUrl: './auth.component.scss' // Certifique-se que aponta para o arquivo SCSS
})
export class AuthComponent {

}