import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';

import {
  provideClientHydration,
  withEventReplay,
} from '@angular/platform-browser';
import { routes } from './app.routes';
import { authInterceptor } from './core/interceptors/auth.interceptor';

// Importamos el interceptor de autenticación

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    // Registramos el interceptor HTTP para todas las solicitudes
    provideHttpClient(withInterceptors([authInterceptor])),
    provideClientHydration(withEventReplay()),
  ],
};
