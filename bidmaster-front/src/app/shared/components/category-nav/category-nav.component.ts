import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

interface Category {
  id: number;
  name: string;
  slug: string;
  featured?: boolean;
}

@Component({
  selector: 'app-category-nav',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <nav class="border-b bg-white shadow-sm">
      <div class="container mx-auto px-4">
        <div class="flex h-12 items-center space-x-8">
          <!-- Menú de todas las categorías -->
          <div class="relative">
            <button
              (click)="toggleMenu()"
              class="flex items-center space-x-2 rounded-md px-3 py-2 text-gray-700 hover:bg-gray-100"
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
                stroke-width="1.5"
                stroke="currentColor"
                class="h-6 w-6"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  d="M3.75 6.75h16.5M3.75 12h16.5m-16.5 5.25h16.5"
                />
              </svg>
              <span class="hidden sm:inline">Todas las categorías</span>
            </button>

            @if (isMenuOpen()) {
              <div
                class="absolute left-0 top-full z-50 mt-1 w-64 rounded-md border bg-white py-1 shadow-lg"
              >
                @for (category of categories(); track category.id) {
                  <a
                    [routerLink]="['/category', category.slug]"
                    class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                  >
                    {{ category.name }}
                  </a>
                }
              </div>
            }
          </div>

          <!-- Categorías destacadas -->
          <div class="flex space-x-8 overflow-x-auto">
            @for (category of featuredCategories(); track category.id) {
              <a
                [routerLink]="['/category', category.slug]"
                class="whitespace-nowrap text-sm font-medium text-gray-700 hover:text-blue-600"
              >
                {{ category.name }}
              </a>
            }
          </div>
        </div>
      </div>
    </nav>
  `,
})
export class CategoryNavComponent {
  private allCategories: Category[] = [
    { id: 1, name: 'Electrónica', slug: 'electronica', featured: true },
    { id: 2, name: 'Vehículos', slug: 'vehiculos', featured: true },
    { id: 3, name: 'Inmobiliaria', slug: 'inmobiliaria', featured: true },
    { id: 4, name: 'Deportes', slug: 'deportes', featured: true },
    { id: 5, name: 'Arte', slug: 'arte', featured: true },
    { id: 6, name: 'Coleccionables', slug: 'coleccionables' },
    { id: 7, name: 'Instrumentos Musicales', slug: 'instrumentos-musicales' },
    { id: 8, name: 'Libros', slug: 'libros' },
    { id: 9, name: 'Moda', slug: 'moda' },
    { id: 10, name: 'Joyería', slug: 'joyeria' },
  ];

  categories = signal<Category[]>(this.allCategories);
  featuredCategories = signal<Category[]>(
    this.allCategories.filter(cat => cat.featured)
  );
  isMenuOpen = signal(false);

  toggleMenu() {
    this.isMenuOpen.update(value => !value);
  }
}
