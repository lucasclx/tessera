// src/app/shared/shared.module.ts
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';

// Importar componentes
import { HeaderComponent } from './components/header/header.component';
// Outros componentes compartilhados

@NgModule({
  declarations: [
    HeaderComponent,
    // Outros componentes
  ],
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule
  ],
  exports: [
    // Exportar componentes e módulos para uso em outros módulos
    HeaderComponent,
    // Outros componentes
    CommonModule,
    RouterModule,
    ReactiveFormsModule
  ]
})
export class SharedModule { }