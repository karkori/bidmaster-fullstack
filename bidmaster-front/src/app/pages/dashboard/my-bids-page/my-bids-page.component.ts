
import { Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '@core/services/auth.service';

// Importar componente de my-bids
import MyBidsComponent from '../../../components/my-bids/my-bids.component';

@Component({
  selector: 'app-my-bids-page',
  standalone: true,
  imports: [
    MyBidsComponent
],
  template: `
    <div class="container mx-auto px-4 py-6">
      <app-my-bids></app-my-bids>
    </div>
  `,
  styleUrls: []
})
export default class MyBidsPageComponent implements OnInit {
  // Usando inject() en lugar de inyección por constructor
  private authService = inject(AuthService);
  
  constructor() {}
  
  ngOnInit(): void {
    // Lógica de inicialización a nivel de página si es necesaria
  }
}
