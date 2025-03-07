import { Component, inject, signal, HostListener, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-auth-button',
  imports: [CommonModule, RouterLink],
  template: `
    <div class="flex items-center space-x-4">
      @if (!authService.isLoggedIn()) {
        <div class="relative">
          <button
            type="button"
            (click)="toggleDropdown()"
            class="rounded-lg bg-blue-600 px-4 py-2 text-sm font-semibold text-white shadow-sm hover:bg-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-600 focus:ring-offset-2"
          >
            Inicia sesión / Registrar
          </button>
          
          @if (isDropdownOpen()) {
            <div class="absolute right-0 z-10 mt-2 w-56 origin-top-right rounded-md bg-white shadow-lg focus:outline-none">
              <div class="py-1" role="none">
                <a
                  routerLink="/auth/login"
                  (click)="closeDropdown()"
                  class="block px-4 py-3 text-sm text-gray-700 hover:bg-gray-100"
                  role="menuitem"
                >
                  Iniciar sesión
                </a>
                
                <hr class="my-1 border-gray-200">
                
                <a
                  routerLink="/auth/register"
                  (click)="closeDropdown()"
                  class="block px-4 py-3 text-sm text-gray-700 hover:bg-gray-100"
                  role="menuitem"
                >
                  Registrarse
                </a>
              </div>
            </div>
          }
        </div>
      } @else {
        <div class="relative">
          <button
            type="button"
            (click)="toggleUserMenu()"
            class="flex items-center space-x-2 rounded-lg px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-600 focus:ring-offset-2"
          >
            <span>{{ authService.currentUserValue?.username }}</span>
            <svg
              xmlns="http://www.w3.org/2000/svg"
              fill="none"
              viewBox="0 0 24 24"
              stroke-width="1.5"
              stroke="currentColor"
              class="h-5 w-5"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                d="M15.75 6a3.75 3.75 0 1 1-7.5 0 3.75 3.75 0 0 1 7.5 0ZM4.501 20.118a7.5 7.5 0 0 1 14.998 0A17.933 17.933 0 0 1 12 21.75c-2.676 0-5.216-.584-7.499-1.632Z"
              />
            </svg>
          </button>
          
          @if (isUserMenuOpen()) {
            <div class="absolute right-0 z-10 mt-2 w-48 origin-top-right rounded-md bg-white py-1 shadow-lg focus:outline-none">
              <a
                href="#"
                class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                role="menuitem"
              >
                Mi perfil
              </a>
              <a
                href="#"
                class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                role="menuitem"
              >
                Configuración
              </a>
              <button
                (click)="logout()"
                class="block w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                role="menuitem"
              >
                Cerrar sesión
              </button>
            </div>
          }
        </div>
      }
    </div>
  `,
})
export class AuthButtonComponent {
  authService = inject(AuthService);
  router = inject(Router);
  elementRef = inject(ElementRef);
  
  // Usamos signals para el estado de los menús desplegables
  isDropdownOpen = signal(false);
  isUserMenuOpen = signal(false);
  
  // Escuchar clicks en el documento para cerrar menús al hacer clic fuera
  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    // Verificar si el clic fue fuera del componente
    if (!this.elementRef.nativeElement.contains(event.target)) {
      this.isDropdownOpen.set(false);
      this.isUserMenuOpen.set(false);
    }
  }
  
  toggleDropdown() {
    this.isDropdownOpen.update(value => !value);
    this.isUserMenuOpen.set(false); // Cerramos el otro menú si está abierto
  }
  
  toggleUserMenu() {
    this.isUserMenuOpen.update(value => !value);
    this.isDropdownOpen.set(false); // Cerramos el otro menú si está abierto
  }
  
  closeDropdown() {
    this.isDropdownOpen.set(false);
  }
  
  logout() {
    this.authService.logout();
    this.isUserMenuOpen.set(false);
    this.router.navigate(['/']);
  }
}
