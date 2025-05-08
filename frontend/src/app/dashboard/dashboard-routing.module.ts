// src/app/dashboard/dashboard-routing.module.ts
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ProfessorDashboardComponent } from './professor-dashboard/professor-dashboard.component';
import { AlunoDashboardComponent } from './aluno-dashboard/aluno-dashboard.component';
import { AdminDashboardComponent } from './admin-dashboard/admin-dashboard.component';
import { RoleGuard } from '../core/role.guard';

const routes: Routes = [
  {
    path: 'professor',
    component: ProfessorDashboardComponent,
    canActivate: [RoleGuard],
    data: { expectedRole: 'PROFESSOR' }
  },
  {
    path: 'aluno',
    component: AlunoDashboardComponent,
    canActivate: [RoleGuard],
    data: { expectedRole: 'ALUNO' }
  },
  {
    path: 'admin',
    component: AdminDashboardComponent,
    canActivate: [RoleGuard],
    data: { expectedRole: 'ADMIN' }
  },
  // Redirecionamento inteligente baseado no papel do usuário
  { path: '', redirectTo: 'admin', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DashboardRoutingModule { }