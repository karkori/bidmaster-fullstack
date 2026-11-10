
import { Component, OnInit, inject, input, signal, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { AuctionService } from '@core/services/auction.service';
import { Auction, AuctionFilters } from '@shared/models/auction.model';
import { catchError, of } from 'rxjs';
import { AuctionCardComponent } from '../auction-card/auction-card.component';

@Component({
  selector: 'app-auction-list',
  standalone: true,
  imports: [AuctionCardComponent],
  templateUrl: './auction-list.component.html'
})
export class AuctionListComponent implements OnInit {
  title = input<string>('Subastas');
  subtitle = input<string>('');
  filters = input<AuctionFilters | undefined>(undefined);
  limit = input<number>(0);
  
  auctions = signal<Auction[]>([]);
  loading = signal<boolean>(true);
  error = signal<string | null>(null);
  
  private auctionService = inject(AuctionService);
  private platformId = inject(PLATFORM_ID);
  
  ngOnInit(): void {
    this.loadAuctions();
  }
  
  loadAuctions(): void {
    if (!isPlatformBrowser(this.platformId)) {
      this.loading.set(false);
      return;
    }
    this.loading.set(true);
    this.auctionService.findAuctions(this.filters())
      .pipe(
        catchError(err => {
          this.error.set('Error al cargar las subastas. Por favor, inténtalo de nuevo.');
          console.error('Error cargando subastas:', err);
          return of([]);
        })
      )
      .subscribe(auctions => {
        const lim = this.limit();
        this.auctions.set(lim > 0 ? auctions.slice(0, lim) : auctions);
        this.loading.set(false);
      });
  }
}
