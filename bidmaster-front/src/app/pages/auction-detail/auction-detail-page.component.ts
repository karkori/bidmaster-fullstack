
import { Component } from '@angular/core';
import { AuctionDetailComponent } from '@components/auction-detail/auction-detail.component';

@Component({
  selector: 'app-auction-detail-page',
  standalone: true,
  imports: [AuctionDetailComponent],
  template: '<app-auction-detail></app-auction-detail>'
})
export default class AuctionDetailPageComponent {}
