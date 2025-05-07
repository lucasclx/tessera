// ... outros imports
import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { JwtInterceptor } from './core/jwt.interceptor'; // Ajuste o caminho se necessário

@NgModule({
  // ... declarations, imports
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: JwtInterceptor, multi: true }
  ],
  // ... bootstrap
})
export class AppModule { }