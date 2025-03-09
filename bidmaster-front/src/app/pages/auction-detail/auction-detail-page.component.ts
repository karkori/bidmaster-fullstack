import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { AuctionDetailComponent } from '@components/auction-detail/auction-detail.component';

@Component({
  selector: 'app-auction-detail-page',
  standalone: true,
  imports: [CommonModule, AuctionDetailComponent],
  template: '<app-auction-detail></app-auction-detail>'
})
export default class AuctionDetailPageComponent {}
