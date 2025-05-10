// src/app/app.module.ts
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientModule } from '@angular/common/http';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { CoreModule } from './core/core.module';
import { AuthModule } from './auth/auth.module';

@NgModule({
  declarations: [
    // Remover AppComponent da lista de declarações, pois é um componente standalone
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    HttpClientModule,
    CoreModule, // Importante: Core deve ser importado antes dos outros módulos
    AuthModule,
    AppRoutingModule, // Importante: AppRoutingModule deve ser o último
    AppComponent // Importar AppComponent como componente standalone
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }