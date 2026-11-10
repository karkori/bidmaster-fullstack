import { Component } from '@angular/core';

import { RouterLink } from '@angular/router';
import { SearchBarComponent } from '@shared/components/search-bar/search-bar.component';
import { AuthButtonComponent } from '@shared/components/auth-button/auth-button.component';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [RouterLink, SearchBarComponent, AuthButtonComponent],
  template: `
    <header class="sticky top-0 z-40 border-b border-gray-100 bg-white/95 shadow-xs backdrop-blur-md transition-all">
      <div class="container mx-auto px-4 py-2.5 sm:py-3.5">
        <div class="flex flex-wrap items-center justify-between gap-y-2.5 gap-x-4 md:flex-nowrap">
          
          <!-- Logo & Brand (Order 1: top-left on mobile, left on desktop) -->
          <a routerLink="/" class="order-1 flex items-center space-x-2.5 shrink-0 group">
            <div class="flex h-9 w-9 sm:h-10 sm:w-10 items-center justify-center rounded-xl bg-gradient-to-br from-emerald-500 to-emerald-700 text-lg sm:text-xl font-black text-white shadow-xs transition-transform group-hover:scale-105">
              B
            </div>
            <span class="text-xl font-bold tracking-tight text-emerald-600 sm:text-2xl">BidMaster</span>
          </a>

          <!-- Auth & User Actions (Order 2: top-right on mobile, right on desktop) -->
          <div class="order-2 flex items-center shrink-0 md:order-3">
            <app-auth-button />
          </div>

          <!-- Search Bar (Order 3: full-width on mobile row 2, centered flex-1 on desktop) -->
          <div class="order-3 w-full md:order-2 md:mx-4 md:max-w-xl md:flex-1">
            <app-search-bar class="block w-full" />
          </div>

        </div>
      </div>
    </header>
  `,
})
export class HeaderComponent {}
