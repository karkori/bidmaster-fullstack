import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-my-bids',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './my-bids.component.html'
})
export default class MyBidsComponent {
  private authService = inject(AuthService);
  user = this.authService.currentUserValue;
}
