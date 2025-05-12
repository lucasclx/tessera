// src/app/app-routing.module.ts
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { AuthComponent } from './auth/auth.component';
import { HomeComponent } from '../home/home.component'; // Ajuste o caminho se HomeComponent estiver em src/app/home
import { AuthGuard } from './core/guards/auth.guard';

const routes: Routes = [
  { path: 'home', component: HomeComponent },
  {
    path: 'auth',
    component: AuthComponent, // AuthComponent atua como um layout para as rotas de autenticação
    loadChildren: () => import('./auth/auth.module').then(m => m.AuthModule) // auth-routing.module é carregado por AuthModule
  },
  {
    path: 'dashboard',
    loadChildren: () => import('./dashboard/dashboard.module').then(m => m.DashboardModule),
    canActivate: [AuthGuard]
  },
  {
    path: 'monografia', // Rota base para funcionalidades de monografia
    loadChildren: () => import('./monografia/monografia.module').then(m => m.MonografiaModule),
    canActivate: [AuthGuard]
  },
  { path: '', redirectTo: '/home', pathMatch: 'full' },
  { path: '**', redirectTo: '/home' } // Rota curinga para páginas não encontradas
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }

// Para suporte standalone, caso o app.config.ts seja usado no futuro
// export { routes };