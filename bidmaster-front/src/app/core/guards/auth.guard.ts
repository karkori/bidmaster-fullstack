import { isPlatformBrowser } from '@angular/common';
import { inject, PLATFORM_ID } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '@core/services/auth.service';

/**
 * Guard para proteger rutas que requieren autenticación
 * En Angular >19, se recomienda usar el enfoque funcional para los guards
 */
export const authGuard: CanActivateFn = (route, state) => {
  const platformId = inject(PLATFORM_ID);
  if (!isPlatformBrowser(platformId)) {
    return true;
  }

  const authService = inject(AuthService);
  const router = inject(Router);
  
  // Si el usuario está autenticado, permite el acceso a la ruta
  if (authService.isLoggedIn()) {
    return true;
  }
  
  // Si no está autenticado, redirige a la página de login
  // y almacena la URL a la que intentaba acceder para redirigir después del login
  return router.createUrlTree(['/auth/login'], { 
    queryParams: { returnUrl: state.url }
  });
};
