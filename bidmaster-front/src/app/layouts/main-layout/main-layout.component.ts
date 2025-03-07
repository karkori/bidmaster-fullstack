import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { HeaderComponent } from '@layouts/header/header.component';
import { CategoryNavComponent } from '@shared/components/category-nav/category-nav.component';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, HeaderComponent, CategoryNavComponent],
  template: `
    <div class="min-h-screen bg-gray-50">
      <app-header />
      <div class="h-px bg-gradient-to-r from-transparent via-gray-200 to-transparent"></div>
      <app-category-nav />
      
      <!-- Ajuste dinámico de clases para evitar problemas en el Dashboard -->
      <main [class.container]="!isDashboard" [class.mx-auto]="!isDashboard" class="">
        <router-outlet />
      </main>
    </div>
  `,
})
export class MainLayoutComponent {
  isDashboard = false;

  constructor(private router: Router) {
    // Detecta si la ruta actual pertenece al dashboard
    this.router.events.subscribe(() => {
      this.isDashboard = this.router.url.startsWith('/dashboard');
    });
  }
}
