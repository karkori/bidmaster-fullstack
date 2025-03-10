import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { User } from '@shared/models/user.model';
import { environment } from '@environments/environment';
import { isPlatformBrowser } from '@angular/common';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  // Usamos signals en lugar de BehaviorSubject
  private currentUserSignal = signal<User | null>(null);
  
  // Computed signal para verificar si el usuario está autenticado
  public isLoggedIn = computed(() => !!this.currentUserSignal());
  
  // Getter para acceder al valor actual del usuario
  public get currentUserValue(): User | null {
    return this.currentUserSignal();
  }
  
  private apiUrl = `${environment.apiUrl}/users`;
  
  private isBrowser: boolean;

  constructor(private http: HttpClient) {
    // Verificar si estamos en un navegador
    this.isBrowser = typeof window !== 'undefined';
    
    // Intentar recuperar el usuario del localStorage al iniciar (solo en navegador)
    if (this.isBrowser) {
      const storedUser = localStorage.getItem('currentUser');
      if (storedUser) {
        this.currentUserSignal.set(JSON.parse(storedUser));
      }
    }
  }
  
  register(userData: any): Observable<User> {
    return this.http.post<User>(`${this.apiUrl}/register`, userData).pipe(
      tap(user => {
        // No almacenamos el usuario en localStorage después del registro
        // Sólo después del login exitoso
      })
    );
  }
  
  login(credentials: { username: string; password: string }): Observable<User> {
    return this.http.post<User>(`${this.apiUrl}/login`, credentials).pipe(
      tap(user => {
        // Guardar usuario en localStorage (solo en navegador) y actualizar el signal
        if (this.isBrowser) {
          localStorage.setItem('currentUser', JSON.stringify(user));
        }
        this.currentUserSignal.set(user);
      })
    );
  }
  
  logout(): void {
    // Eliminar usuario del localStorage (solo en navegador) y actualizar el signal
    if (this.isBrowser) {
      localStorage.removeItem('currentUser');
    }
    this.currentUserSignal.set(null);
  }
  
  // Los métodos equivalentes ahora son propiedades computadas o getters definidos arriba
}
