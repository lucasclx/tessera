// src/app/monografia/monografia-routing.module.ts
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from '../core/guards/auth.guard';
import { EditorMonografiaComponent } from './pages/editor-monografia/editor-monografia.component';

const routes: Routes = [
  {
    path: 'editor/:id',
    component: EditorMonografiaComponent,
    canActivate: [AuthGuard]
  }
  // Outras rotas relacionadas a monografias poderiam ser adicionadas aqui.
  // Ex: { path: 'visualizar/:id', component: VisualizadorMonografiaComponent, canActivate: [AuthGuard] },
  //     { path: '', component: ListaMonografiasComponent, canActivate: [AuthGuard] } // Uma página para listar monografias
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class MonografiaRoutingModule { }