import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '@core/services/auth.service';

// Importar componente de create-auction
import CreateAuctionComponent from '../../../components/create-auction/create-auction.component';

@Component({
  selector: 'app-create-auction-page',
  standalone: true,
  imports: [
    CommonModule,
    CreateAuctionComponent
  ],
  template: `
    <div class="container mx-auto px-4 py-6">
      <app-create-auction></app-create-auction>
    </div>
  `,
  styleUrls: []
})
export default class CreateAuctionPageComponent implements OnInit {
  // Usando inject() en lugar de inyección por constructor
  private authService = inject(AuthService);
  
  constructor() {}
  
  ngOnInit(): void {
    // Lógica de inicialización a nivel de página si es necesaria
  }
}
