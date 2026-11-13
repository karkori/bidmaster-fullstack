import { Component, computed, inject, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Auction } from '@shared/models/auction.model';
import { ImageService } from '@core/services/image.service';
import { differenceInDays, differenceInHours, differenceInMinutes, isPast } from 'date-fns';

@Component({
  selector: 'app-auction-card',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './auction-card.component.html'
})
export class AuctionCardComponent {
  auction = input.required<Auction>();
  private imageService = inject(ImageService);

  timeLeft = computed(() => {
    const auc = this.auction();
    if (auc.status === 'CANCELLED') return 'Cancelada';
    if (auc.status === 'FINISHED') return 'Finalizada';
    if (auc.status === 'PAUSED') return 'Pausada';
    if (auc.status === 'DRAFT') return 'Borrador';
    if (!auc.endDate) return 'Finalizada';

    const endDate = new Date(auc.endDate);
    if (isPast(endDate)) return 'Finalizada';

    const now = new Date();
    const days = differenceInDays(endDate, now);
    const hours = differenceInHours(endDate, now) % 24;
    const minutes = differenceInMinutes(endDate, now) % 60;

    if (days > 0) return `${days}d ${hours}h`;
    if (hours > 0) return `${hours}h ${minutes}m`;
    return `${minutes}m`;
  });

  imageUrl = computed(() => {
    const images = this.auction().images;
    if (!images || images.length === 0) {
      return '/no-image.svg';
    }
    const primaryOrFirst = images.find(img => img.primary) ?? images[0];
    return this.imageService.resolveImageUrl(primaryOrFirst.url, primaryOrFirst.id);
  });
}
