
import { Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '@core/services/auth.service';

// Importar componente de profile
import ProfileComponent from '../../../components/profile/profile.component';

@Component({
  selector: 'app-profile-page',
  standalone: true,
  imports: [
    ProfileComponent
],
  template: `
    <div class="container mx-auto px-4 py-6">
      <app-profile></app-profile>
    </div>
  `,
  styleUrls: []
})
export default class ProfilePageComponent implements OnInit {
  // Usando inject() en lugar de inyección por constructor
  private authService = inject(AuthService);
  
  constructor() {}
  
  ngOnInit(): void {
    // Lógica de inicialización a nivel de página si es necesaria
  }
}
