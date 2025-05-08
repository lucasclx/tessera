// src/app/dashboard/dashboard.module.ts
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { DashboardRoutingModule } from './dashboard-routing.module';
import { ProfessorDashboardComponent } from './professor-dashboard/professor-dashboard.component';
import { AlunoDashboardComponent } from './aluno-dashboard/aluno-dashboard.component';

@NgModule({
  declarations: [
    // Componentes standalone são importados, não declarados
  ],
  imports: [
    CommonModule,
    DashboardRoutingModule,
    ProfessorDashboardComponent, // Componente standalone importado
    AlunoDashboardComponent      // Componente standalone importado
  ]
})
export class DashboardModule { }