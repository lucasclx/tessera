// src/app/app.module.ts
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { JwtInterceptor } from './core/jwt.interceptor';
import { AuthModule } from './auth/auth.module'; // Necessário porque AuthComponent é usado pelo AppRoutingModule
import { HomeComponent } from './home/home.component'; // Necessário porque HomeComponent (standalone) é usado pelo AppRoutingModule

@NgModule({
  declarations: [
    // AppComponent é standalone, então não é declarado aqui.
    // Componentes de AuthModule são declarados/importados lá.
    // HomeComponent é standalone.
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    AuthModule,     // Importa AuthModule (contém AuthComponent)
    AppComponent,   // Importa AppComponent (standalone, bootstrap component)
    HomeComponent   // Importa HomeComponent (standalone, usado nas rotas)
  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: JwtInterceptor, multi: true }
  ],
  bootstrap: [AppComponent] // AppComponent é o componente raiz da aplicação
})
export class AppModule { }