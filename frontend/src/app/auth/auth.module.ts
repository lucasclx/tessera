import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms'; // Essencial para o loginForm

import { AuthRoutingModule } from './auth-routing.module';
import { LoginComponent } from './login/login.component';

@NgModule({
  declarations: [
    LoginComponent // LoginComponent é declarado aqui
  ],
  imports: [
    CommonModule,       // Para diretivas como *ngIf
    AuthRoutingModule,
    ReactiveFormsModule // Para [formGroup] e formControlName
  ]
})
export class AuthModule { }