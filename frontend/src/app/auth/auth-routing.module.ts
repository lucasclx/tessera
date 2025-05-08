import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthComponent } from './auth.component';
import { LoginComponent } from './login/login.component'; // Importar o LoginComponent

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
        path: '', // Rota padrão dentro do módulo 'auth'
        redirectTo: 'login', // Redireciona 'auth/' para 'auth/login'
        pathMatch: 'full'
      }
      // Você pode adicionar outras rotas de autenticação aqui, como 'register'
      // { path: 'register', component: RegisterComponent },
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AuthRoutingModule { }