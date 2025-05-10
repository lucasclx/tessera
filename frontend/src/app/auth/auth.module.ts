// src/app/auth/auth.module.ts
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms'; // Já estava aqui

import { AuthRoutingModule } from './auth-routing.module';
import { AuthComponent } from './auth.component';
// Componentes Standalone são importados individualmente onde são usados ou no routing module
// LoginComponent, RegisterComponent, PendingApprovalComponent são standalone.
import { MaterialModule } from '../material.module'; // Importar MaterialModule

@NgModule({
  declarations: [
    AuthComponent // AuthComponent é o wrapper, não é standalone
  ],
  imports: [
    CommonModule,
    AuthRoutingModule,
    ReactiveFormsModule, // Essencial para os formulários
    MaterialModule, // Adicionar para disponibilizar componentes Material para AuthComponent (se ele usar diretamente)
    // LoginComponent, RegisterComponent, PendingApprovalComponent são standalone,
    // então não precisam ser importados aqui se já estiverem nos 'imports' dos componentes que os utilizam
    // ou se forem carregados via rotas. Seus próprios arquivos .ts os importarão.
  ]
})
export class AuthModule { }