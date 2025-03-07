import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '@core/services/auth.service';

// Importar componente de notifications
import NotificationsComponent from '../../../components/notifications/notifications.component';

@Component({
  selector: 'app-notifications-page',
  standalone: true,
  imports: [
    CommonModule,
    NotificationsComponent
  ],
  template: `
    <div class="container mx-auto px-4 py-6">
      <app-notifications></app-notifications>
    </div>
  `,
  styleUrls: []
})
export default class NotificationsPageComponent implements OnInit {
  // Usando inject() en lugar de inyección por constructor
  private authService = inject(AuthService);
  
  constructor() {}
  
  ngOnInit(): void {
    // Lógica de inicialización a nivel de página si es necesaria
  }
}
