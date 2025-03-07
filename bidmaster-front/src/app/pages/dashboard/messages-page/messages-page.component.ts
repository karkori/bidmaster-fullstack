import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '@core/services/auth.service';

// Importar componente de messages
import MessagesComponent from '../../../components/messages/messages.component';

@Component({
  selector: 'app-messages-page',
  standalone: true,
  imports: [
    CommonModule,
    MessagesComponent
  ],
  template: `
    <div class="container mx-auto px-4 py-6">
      <app-messages></app-messages>
    </div>
  `,
  styleUrls: []
})
export default class MessagesPageComponent implements OnInit {
  // Usando inject() en lugar de inyección por constructor
  private authService = inject(AuthService);
  
  constructor() {}
  
  ngOnInit(): void {
    // Lógica de inicialización a nivel de página si es necesaria
  }
}
