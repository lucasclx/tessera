import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { DashboardRoutingModule } from './dashboard-routing.module';
import { ProfessorDashboardComponent } from './professor-dashboard/professor-dashboard.component'; // Certifique-se que o caminho está correto
import { AlunoDashboardComponent } from './aluno-dashboard/aluno-dashboard.component'; // Certifique-se que o caminho está correto

@NgModule({
  declarations: [
    // ProfessorDashboardComponent, // Remova daqui
    // AlunoDashboardComponent    // Remova daqui
  ],
  imports: [
    CommonModule,
    DashboardRoutingModule,
    ProfessorDashboardComponent, // Adicione aqui
    AlunoDashboardComponent     // Adicione aqui
  ]
})
export class DashboardModule { }