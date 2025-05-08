import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common'; // Essencial para *ngIf, etc.

import { DashboardRoutingModule } from './dashboard-routing.module';
import { ProfessorDashboardComponent } from './professor-dashboard/professor-dashboard.component';
import { AlunoDashboardComponent } from './aluno-dashboard/aluno-dashboard.component';

@NgModule({
  declarations: [
    ProfessorDashboardComponent, // Declarados aqui
    AlunoDashboardComponent
  ],
  imports: [
    CommonModule, // Para diretivas como *ngIf
    DashboardRoutingModule
  ]
})
export class DashboardModule { }