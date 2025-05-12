// src/app/monografia/monografia.module.ts
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';

import { MonografiaRoutingModule } from './monografia-routing.module';
import { MaterialModule } from '../material.module'; // Módulo com os componentes do Angular Material

// Componentes Standalone são importados diretamente pelos componentes que os utilizam.
// EditorMonografiaComponent é o componente principal desta rota e já importa os outros componentes standalone necessários.
import { EditorMonografiaComponent } from './pages/editor-monografia/editor-monografia.component';
// Não é necessário importar RichTextEditorComponent, VersaoTimelineComponent, etc. aqui,
// pois são standalone e já importados por EditorMonografiaComponent.

@NgModule({
  declarations: [
    // EditorMonografiaComponent não é declarado aqui porque é standalone.
    // Se você tivesse componentes não-standalone pertencentes a este módulo, eles seriam declarados aqui.
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule, // Importante para os formulários no EditorMonografiaComponent (titleForm, saveForm)
    MaterialModule,      // Para os componentes do Angular Material usados
    MonografiaRoutingModule, // Contém as rotas específicas deste módulo

    // Como EditorMonografiaComponent é standalone e é o componente da rota,
    // ele deve ser importado aqui para que o Angular o reconheça ao carregar este módulo.
    EditorMonografiaComponent
  ]
})
export class MonografiaModule { }