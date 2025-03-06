import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { SearchBarComponent } from '../../shared/components/search-bar/search-bar.component';
import { AuthButtonComponent } from '../../shared/components/auth-button/auth-button.component';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterLink, SearchBarComponent, AuthButtonComponent],
  template: `
    <header class="bg-white shadow">
      <div class="container mx-auto px-4">
        <div class="flex h-20 items-center justify-between">
          <!-- Logo -->
          <a routerLink="/" class="flex items-center space-x-2">
            <img src="/logo.svg" alt="BidMaster" class="h-10 w-auto" />
            <span class="hidden text-xl font-bold text-blue-900 sm:block">BidMaster</span>
          </a>

          <!-- Search and Auth container -->
          <div class="flex flex-1 items-center justify-end space-x-4 sm:justify-between">
            <app-search-bar class="hidden flex-1 px-4 sm:block sm:px-6" />
            <app-auth-button />
          </div>
        </div>
      </div>
    </header>
  `,
})
export class HeaderComponent {}
