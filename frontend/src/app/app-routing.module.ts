// src/app/app-routing.module.ts
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { AuthComponent } from './auth/auth.component';
import { LoginComponent } from './auth/login/login.component';
import { RegisterComponent } from './auth/register/register.component';
import { HomeComponent } from './home/home.component'; // Importe o HomeComponent

const routes: Routes = [
  { path: 'home', component: HomeComponent },
  {
    path: 'auth', // Define o segmento 'auth'
    component: AuthComponent, // AuthComponent é o container para as rotas filhas de 'auth'
    children: [
      { path: 'login', component: LoginComponent },       // Rota para /auth/login
      { path: 'register', component: RegisterComponent }, // Rota para /auth/register
      { path: '', redirectTo: 'login', pathMatch: 'full' } // Redireciona /auth para /auth/login
    ]
  },
  { path: '', redirectTo: '/home', pathMatch: 'full' }, // Redireciona a rota raiz para /home
  // Opcional: uma rota curinga para páginas não encontradas
  // { path: '**', component: PageNotFoundComponent }, // Crie este componente se desejar
  { path: '**', redirectTo: '/home' } // Ou redirecione para home como fallback
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }