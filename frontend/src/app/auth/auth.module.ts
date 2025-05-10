// src/app/auth/auth.module.ts
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router'; // Adicionar RouterModule

import { AuthRoutingModule } from './auth-routing.module';
import { MaterialModule } from '../material.module';

// Importar componentes
import { AuthComponent } from './auth.component';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { PendingApprovalComponent } from './components/pending-approval/pending-approval.component';

@NgModule({
  declarations: [
    AuthComponent,
    // Remover os componentes standalone
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule, // Adicionar RouterModule para o router-outlet
    MaterialModule,
    AuthRoutingModule,
    // Importar os componentes standalone
    LoginComponent,
    RegisterComponent,
    PendingApprovalComponent
  ],
  providers: []
})
export class AuthModule { }