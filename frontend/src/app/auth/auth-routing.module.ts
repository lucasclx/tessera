import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthComponent } from './auth.component';
import { LoginComponent } from './login/login.component';
import { RegisterComponent } from './register/register.component'; 
import { PendingApprovalComponent } from './pending-approval/pending-approval.component';

const routes: Routes = [
  {
    path: '', // O prefixo 'auth' já foi definido no AppRoutingModule
    component: AuthComponent, // AuthComponent contém o <router-outlet> para as rotas abaixo
    children: [
      {
        path: 'login', // Rota para /auth/login
        component: LoginComponent
      },
      {
        path: 'register', // Rota para /auth/register
        component: RegisterComponent
      },
      {
        path: 'pending-approval', // Rota para /auth/pending-approval
        component: PendingApprovalComponent
      },
      {
        path: '', // Rota padrão dentro do módulo 'auth'
        redirectTo: 'login', // Redireciona 'auth/' para 'auth/login'
        pathMatch: 'full'
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AuthRoutingModule { }