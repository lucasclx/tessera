import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router'; // Importar se for usar rotas

import { routes } from './app.routes'; // Importa as definições de rotas

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }), // Configuração padrão do Zone.js
    provideRouter(routes) // Fornece as rotas para a aplicação
  ]
};