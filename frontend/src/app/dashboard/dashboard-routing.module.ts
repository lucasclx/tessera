import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ProfessorDashboardComponent } from './professor-dashboard/professor-dashboard.component';
import { AlunoDashboardComponent } from './aluno-dashboard/aluno-dashboard.component';
import { RoleGuard } from '../core/role.guard'; // << IMPORTAR RoleGuard (ajuste o caminho se necessário)

const routes: Routes = [
  {
    path: 'professor',
    component: ProfessorDashboardComponent,
    canActivate: [RoleGuard], // AuthGuard já foi aplicado na rota pai ('/dashboard')
    data: { expectedRole: 'PROFESSOR' } // Passar a role esperada para o guard
  },
  {
    path: 'aluno',
    component: AlunoDashboardComponent,
    canActivate: [RoleGuard],
    data: { expectedRole: 'ALUNO' } // Passar a role esperada para o guard
  },
  // Você pode adicionar um redirecionamento padrão ou um componente de dashboard "home" aqui
  { path: '', redirectTo: 'professor', pathMatch: 'full' } // Exemplo: redireciona para professor por padrão
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DashboardRoutingModule { }