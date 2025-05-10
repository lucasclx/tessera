// src/app/app.module.ts
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { CoreModule } from './core/core.module';
import { SharedModule } from './shared/shared.module';

@NgModule({
  declarations: [
    AppComponent,
    // Outros componentes que ainda não foram migrados
  ],
  imports: [
    BrowserModule,
    CoreModule, // Importante: Core deve ser importado antes dos outros módulos
    SharedModule,
    AppRoutingModule // Importante: AppRoutingModule deve ser o último
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }