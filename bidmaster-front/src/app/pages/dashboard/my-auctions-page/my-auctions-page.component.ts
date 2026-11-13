import { Component } from '@angular/core';
import MyAuctionsComponent from '../../../components/my-auctions/my-auctions.component';

@Component({
  selector: 'app-my-auctions-page',
  standalone: true,
  imports: [MyAuctionsComponent],
  template: `
    <div class="container mx-auto px-4 py-6">
      <app-my-auctions></app-my-auctions>
    </div>
  `
})
export default class MyAuctionsPageComponent {}
