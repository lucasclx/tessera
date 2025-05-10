// src/app/shared/shared.module.ts
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';
import { MaterialModule } from '../material.module';

// Crie um componente HeaderComponent para substituir a importação ausente
// ou remova a referência se não for usado

@NgModule({
  declarations: [
    // Remova a referência ao HeaderComponent se não existir
  ],
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    MaterialModule
  ],
  exports: [
    // Exportar componentes e módulos para uso em outros módulos
    // Remova a referência ao HeaderComponent se não existir
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    MaterialModule
  ]
})
export class SharedModule { }