import { CommonModule } from '@angular/common';
import { Component, Input, OnInit, inject } from '@angular/core';
import { AuctionService } from '@core/services/auction.service';
import { Auction, AuctionFilters } from '@shared/models/auction.model';
import { catchError, of } from 'rxjs';
import { AuctionCardComponent } from '../auction-card/auction-card.component';

@Component({
  selector: 'app-auction-list',
  standalone: true,
  imports: [CommonModule, AuctionCardComponent],
  templateUrl: './auction-list.component.html'
})
export class AuctionListComponent implements OnInit {
  @Input() title: string = 'Subastas';
  @Input() subtitle: string = '';
  @Input() filters?: AuctionFilters;
  @Input() limit: number = 0;
  
  auctions: Auction[] = [];
  loading: boolean = true;
  error: string | null = null;
  
  private auctionService = inject(AuctionService);
  
  ngOnInit(): void {
    this.loadAuctions();
  }
  
  loadAuctions(): void {
    this.loading = true;
    this.auctionService.findAuctions(this.filters)
      .pipe(
        catchError(err => {
          this.error = 'Error al cargar las subastas. Por favor, inténtalo de nuevo.';
          console.error('Error cargando subastas:', err);
          return of([]);
        })
      )
      .subscribe(auctions => {
        console.log(`auctions: `,auctions);
        this.auctions = this.limit > 0 ? auctions.slice(0, this.limit) : auctions;
        this.loading = false;
      });
  }
}
