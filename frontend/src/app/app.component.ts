// src/app/app.component.ts
import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  // standalone: false, // Implícito se não declarado e está no AppModule
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'frontend';
}