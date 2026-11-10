
import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { AuctionListComponent } from '@shared/components/auction-list/auction-list.component';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterModule, AuctionListComponent],
  templateUrl: './home.component.html'
})
export default class HomeComponent {}
