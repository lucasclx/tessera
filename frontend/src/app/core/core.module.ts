import { NgModule, Optional, SkipSelf, Injector } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';

import { JwtInterceptor } from './interceptors/jwt.interceptor';
import { AuthService } from './auth.service';
// Importe outros serviços e interceptors

@NgModule({
  imports: [
    CommonModule,
    HttpClientModule
  ],
  providers: [
    AuthService, // Adicione o AuthService explicitamente
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