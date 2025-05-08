// src/app/dashboard/dashboard.module.ts
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { DashboardRoutingModule } from './dashboard-routing.module';
import { ProfessorDashboardComponent } from './professor-dashboard/professor-dashboard.component';
import { AlunoDashboardComponent } from './aluno-dashboard/aluno-dashboard.component';
import { AdminDashboardComponent } from './admin-dashboard/admin-dashboard.component';

@NgModule({
  declarations: [
    // Não declarar componentes standalone aqui
  ],
  imports: [
    CommonModule,
    DashboardRoutingModule,
    // Importar componentes standalone
    ProfessorDashboardComponent,
    AlunoDashboardComponent,
    AdminDashboardComponent
  ]
})
export class DashboardModule { }