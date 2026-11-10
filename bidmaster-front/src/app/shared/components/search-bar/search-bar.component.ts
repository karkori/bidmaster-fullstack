import { Component, signal } from '@angular/core';

import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-search-bar',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="relative w-full">
      <div class="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3.5 text-gray-400">
        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" class="h-4 w-4">
          <path stroke-linecap="round" stroke-linejoin="round" d="m21 21-5.197-5.197m0 0A7.5 7.5 0 1 0 5.196 5.196a7.5 7.5 0 0 0 10.607 10.607Z" />
        </svg>
      </div>
      <input
        type="search"
        [ngModel]="searchQuery()"
        (ngModelChange)="updateSearch($event)"
        placeholder="Buscar por marca, modelo o categoría..."
        class="w-full rounded-xl border border-gray-200 bg-gray-50/70 py-2 sm:py-2.5 pl-10 pr-4 text-xs sm:text-sm text-gray-900 placeholder:text-gray-400 transition-all hover:bg-white focus:border-emerald-500 focus:bg-white focus:outline-none focus:ring-2 focus:ring-emerald-200"
      />
    </div>
  `,
})
export class SearchBarComponent {
  searchQuery = signal('');

  updateSearch(query: string) {
    this.searchQuery.set(query);
    // Aquí podríamos implementar debounce para la búsqueda
  }
}
