import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '@core/services/auth.service';

// Importar componente de favorites
import FavoritesComponent from '../../../components/favorites/favorites.component';

@Component({
  selector: 'app-favorites-page',
  standalone: true,
  imports: [
    CommonModule,
    FavoritesComponent
  ],
  template: `
    <div class="container mx-auto px-4 py-6">
      <app-favorites></app-favorites>
    </div>
  `,
  styleUrls: []
})
export default class FavoritesPageComponent implements OnInit {
  // Usando inject() en lugar de inyección por constructor
  private authService = inject(AuthService);
  
  constructor() {}
  
  ngOnInit(): void {
    // Lógica de inicialización a nivel de página si es necesaria
  }
}
