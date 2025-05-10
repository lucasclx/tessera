import { NgModule, Optional, SkipSelf } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';

import { JwtInterceptor } from './interceptors/jwt.interceptor';
// Importe outros serviços e interceptors

@NgModule({
  imports: [
    CommonModule,
    HttpClientModule
  ],
  providers: [
    // Registre os interceptors
    { provide: HTTP_INTERCEPTORS, useClass: JwtInterceptor, multi: true },
    // Outros serviços
  ]
})
export class CoreModule {
  // Garantir que o CoreModule seja carregado apenas uma vez
  constructor(@Optional() @SkipSelf() parentModule: CoreModule) {
    if (parentModule) {
      throw new Error('CoreModule já foi importado. Importe apenas no AppModule.');
    }
  }
}