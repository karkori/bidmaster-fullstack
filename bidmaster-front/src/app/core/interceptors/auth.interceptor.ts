import {
  HttpErrorResponse,
  HttpEvent,
  HttpHandlerFn,
  HttpInterceptorFn,
  HttpRequest,
} from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from '@core/services/auth.service';

/**
 * Interceptor de autenticación para añadir el token JWT a las solicitudes HTTP
 * y manejar errores de autenticación
 */
export const authInterceptor: HttpInterceptorFn = (
  request: HttpRequest<unknown>,
  next: HttpHandlerFn
): Observable<HttpEvent<unknown>> => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // Obtener el usuario actual
  const currentUser = authService.currentUserValue;

  // Si el usuario está autenticado y tiene datos de autenticación, añadirlos al header
  // Nota: asumimos que el token está almacenado en localStorage por el AuthService
  const token = localStorage.getItem('auth_token');

  if (currentUser && token) {
    request = request.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
      },
    });
  }

  // Procesar la solicitud y manejar errores relacionados con la autenticación
  return next(request).pipe(
    catchError((err: HttpErrorResponse) => {
      // Si recibimos un 401 Unauthorized o 403 Forbidden, el token puede ser inválido
      if ([401, 403].includes(err.status)) {
        // Cerrar sesión automáticamente
        authService.logout();

        // Redirigir al login
        router.navigate(['/auth/login'], {
          queryParams: { returnUrl: router.url },
        });
      }

      return throwError(() => err);
    })
  );
};
