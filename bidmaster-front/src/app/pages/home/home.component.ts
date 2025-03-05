import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="space-y-6">
      <h1 class="text-3xl font-bold text-gray-900">Bienvenido a BidMaster</h1>
      <p class="text-lg text-gray-600">
        Encuentra los mejores productos y haz tus ofertas.
      </p>
    </div>
  `,
})
export default class HomeComponent {}
