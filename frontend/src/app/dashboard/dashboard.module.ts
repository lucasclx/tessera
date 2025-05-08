import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { DashboardRoutingModule } from './dashboard-routing.module';
import { ProfessorDashboardComponent } from './professor-dashboard/professor-dashboard.component';
import { AlunoDashboardComponent } from './aluno-dashboard/aluno-dashboard.component';
// DashboardComponent (o wrapper, se houver) seria declarado aqui se standalone: false
// import { DashboardComponent } from './dashboard.component';

@NgModule({
  declarations: [
    // DashboardComponent, // Se DashboardComponent for standalone: false
  ],
  imports: [
    CommonModule,
    DashboardRoutingModule,
    ProfessorDashboardComponent, // Importado pois é standalone
    AlunoDashboardComponent     // Importado pois é standalone
  ]
})
export class DashboardModule { }