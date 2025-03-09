import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Auction } from '@shared/models/auction.model';
import { differenceInDays, differenceInHours, differenceInMinutes, isPast } from 'date-fns';

@Component({
  selector: 'app-auction-card',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './auction-card.component.html'
})
export class AuctionCardComponent {
  @Input() auction!: Auction;
  
  // Calcular tiempo restante para la subasta usando date-fns
  get timeLeft(): string {
    if (!this.auction.endDate) return 'Finalizada';
    
    const endDate = new Date(this.auction.endDate);
    
    if (isPast(endDate)) return 'Finalizada';
    
    const now = new Date();
    const days = differenceInDays(endDate, now);
    const hours = differenceInHours(endDate, now) % 24;
    const minutes = differenceInMinutes(endDate, now) % 60;
    
    if (days > 0) return `${days}d ${hours}h`;
    if (hours > 0) return `${hours}h ${minutes}m`;
    return `${minutes}m`;
  }
  
  // Obtener la URL de la imagen principal o una imagen por defecto
  get imageUrl(): string {
    const primaryImage = this.auction.images?.find(img => img.primary);
    return primaryImage?.url || 'assets/images/no-image.png';
  }
}
