import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from '../header/header.component';
import { CategoryNavComponent } from '../../shared/components/category-nav/category-nav.component';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, HeaderComponent, CategoryNavComponent],
  template: `
    <div class="min-h-screen bg-gray-50">
      <app-header />
      <app-category-nav />
      <main class="container mx-auto px-4 py-6">
        <router-outlet />
      </main>
    </div>
  `,
})
export class MainLayoutComponent {}
