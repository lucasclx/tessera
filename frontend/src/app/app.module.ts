// src/app/app.module.ts
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations'; // Importado

import { AppRoutingModule } from './app-routing.module';
import { JwtInterceptor } from './core/jwt.interceptor';
import { AuthModule } from './auth/auth.module';
import { AppComponent } from './app.component';
import { MaterialModule } from './material.module'; // Importado
import { HomeComponent } from '../home/home.component'; // Importar HomeComponent se for standalone e usado aqui
// Se HomeComponent não for standalone, ele deve ser declarado em um módulo e importado aqui

@NgModule({
  declarations: [
    // AppComponent é standalone, então não é declarado aqui.
    // Se HomeComponent não for standalone, declare-o no seu módulo respectivo.
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule, // Adicionado
    AppRoutingModule,
    HttpClientModule,
    MaterialModule, // Adicionado
    AuthModule, // AuthModule já importa ReactiveFormsModule
    AppComponent, // Importado como standalone
    HomeComponent // Importar HomeComponent se for standalone e usado no app.module ou app-routing
  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: JwtInterceptor, multi: true }
  ],
  bootstrap: [AppComponent] // AppComponent é o componente raiz
})
export class AppModule { }