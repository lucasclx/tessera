import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';

import { AuthRoutingModule } from './auth-routing.module';
import { LoginComponent } from './login/login.component'; // Certifique-se que o caminho está correto

@NgModule({
  declarations: [
    // LoginComponent // Remova daqui
  ],
  imports: [
    CommonModule,
    AuthRoutingModule,
    ReactiveFormsModule,
    LoginComponent // Adicione aqui
  ]
})
export class AuthModule { }