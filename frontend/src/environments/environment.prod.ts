// src/app/app.module.ts
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http'; // Já deve estar aqui
// ReactiveFormsModule será importado no AuthModule

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { JwtInterceptor } from './core/jwt.interceptor'; // Certifique-se que o caminho está correto

@NgModule({
  declarations: [
    AppComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule
    // ReactiveFormsModule // Removido daqui, será adicionado ao AuthModule
  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: JwtInterceptor, multi: true } // REGISTRAR O INTERCEPTOR
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }