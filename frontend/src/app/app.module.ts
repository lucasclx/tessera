// src/app/app.module.ts
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientModule } from '@angular/common/http';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { CoreModule } from './core/core.module';
import { AuthModule } from './auth/auth.module';
import { MaterialModule } from './material.module';

@NgModule({
  declarations: [
    // AppComponent é standalone
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    HttpClientModule,
    MaterialModule,
    CoreModule, // Importante: Core deve ser importado antes dos outros módulos
    AuthModule,
    AppRoutingModule, // AppRoutingModule deve ser o último
    AppComponent // AppComponent como standalone
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }