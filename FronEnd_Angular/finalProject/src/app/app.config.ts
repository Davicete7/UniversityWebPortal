import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';

// Importamos withInterceptors para poder añadir nuestro guardia
import { provideHttpClient, withInterceptors } from '@angular/common/http';
// Importamos la función del interceptor que acabamos de crear
import { authInterceptor } from './services/auth/authInterceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    // Activamos la antena HTTP y le acoplamos nuestro interceptor
    provideHttpClient(withInterceptors([authInterceptor])),
  ],
};
