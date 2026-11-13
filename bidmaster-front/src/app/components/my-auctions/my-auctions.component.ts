import { CurrencyPipe, DatePipe, isPlatformBrowser } from '@angular/common';
import { Component, OnInit, PLATFORM_ID, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuctionService } from '@core/services/auction.service';
import { AuthService } from '@core/services/auth.service';
import { ImageService } from '@core/services/image.service';
import EmptyStateComponent from '@shared/components/empty-state/empty-state.component';
import FilterBarComponent from '@shared/components/filter-bar/filter-bar.component';
import LoadingSpinnerComponent from '@shared/components/loading-spinner/loading-spinner.component';
import PageHeaderComponent from '@shared/components/page-header/page-header.component';
import StatusBadgeComponent from '@shared/components/status-badge/status-badge.component';
import { Auction } from '@shared/models/auction.model';

type ConfirmationAction = {
  auctionId: string;
  type: 'delete' | 'cancel';
};

@Component({
  selector: 'app-my-auctions',
  standalone: true,
  imports: [
    RouterLink,
    CurrencyPipe,
    DatePipe,
    PageHeaderComponent,
    FilterBarComponent,
    StatusBadgeComponent,
    EmptyStateComponent,
    LoadingSpinnerComponent
  ],
  templateUrl: './my-auctions.component.html'
})
export default class MyAuctionsComponent implements OnInit {
  private auctionService = inject(AuctionService);
  private authService = inject(AuthService);
  private imageService = inject(ImageService);
  private platformId = inject(PLATFORM_ID);
  private isBrowser = isPlatformBrowser(this.platformId);

  auctions = signal<Auction[]>([]);
  loading = signal<boolean>(true);
  errorMessage = signal<string | null>(null);
  actionError = signal<string | null>(null);
  actionSuccess = signal<string | null>(null);
  selectedStatus = signal<string>('ALL');
  searchTerm = signal<string>('');
  busyAuctionId = signal<string | null>(null);
  pendingConfirmation = signal<ConfirmationAction | null>(null);

  readonly filterOptions = [
    { value: 'ALL', label: 'Todas' },
    { value: 'DRAFT', label: 'Borradores' },
    { value: 'ACTIVE', label: 'Activas' },
    { value: 'PAUSED', label: 'Pausadas' },
    { value: 'FINISHED', label: 'Finalizadas' },
    { value: 'CANCELLED', label: 'Canceladas' }
  ];

  filteredAuctions = computed(() => {
    const status = this.selectedStatus();
    const query = this.searchTerm().trim().toLowerCase();

    return this.auctions().filter(auction => {
      const matchesStatus = status === 'ALL' || auction.status === status;
      const matchesSearch = !query || auction.title.toLowerCase().includes(query);
      return matchesStatus && matchesSearch;
    });
  });

  draftCount = computed(() => this.auctions().filter(a => a.status === 'DRAFT').length);
  activeCount = computed(() => this.auctions().filter(a => a.status === 'ACTIVE').length);
  pausedCount = computed(() => this.auctions().filter(a => a.status === 'PAUSED').length);

  ngOnInit(): void {
    if (!this.isBrowser) {
      this.loading.set(false);
      return;
    }
    this.loadSellerAuctions();
  }

  loadSellerAuctions(): void {
    const currentUser = this.authService.currentUserValue;
    if (!currentUser?.id) {
      this.loading.set(false);
      this.errorMessage.set('No se pudo identificar al vendedor autenticado.');
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);

    this.auctionService.findAuctions({ sellerId: currentUser.id }).subscribe({
      next: (list) => {
        this.auctions.set(list ?? []);
        this.loading.set(false);
      },
      error: (err) => {
        this.errorMessage.set(this.extractProblemDetail(err, 'No se pudieron cargar tus subastas.'));
        this.loading.set(false);
      }
    });
  }

  onFilterChange(status: string): void {
    this.selectedStatus.set(status);
    this.pendingConfirmation.set(null);
  }

  onSearchChange(query: string): void {
    this.searchTerm.set(query);
  }

  getCoverImageUrl(auction: Auction): string {
    const images = auction.images ?? [];
    if (images.length === 0) {
      return '/no-image.svg';
    }
    const primary = images.find(img => img.primary) ?? images[0];
    return this.imageService.resolveImageUrl(primary.url, primary.id);
  }

  getStatusBadgeType(status?: string): 'success' | 'warning' | 'danger' | 'info' | 'primary' | 'secondary' {
    switch (status) {
      case 'ACTIVE':
        return 'success';
      case 'PAUSED':
        return 'warning';
      case 'CANCELLED':
        return 'danger';
      case 'FINISHED':
        return 'info';
      default:
        return 'secondary';
    }
  }

  getStatusLabel(status?: string): string {
    switch (status) {
      case 'DRAFT':
        return 'Borrador';
      case 'ACTIVE':
        return 'Activa';
      case 'PAUSED':
        return 'Pausada';
      case 'FINISHED':
        return 'Finalizada';
      case 'CANCELLED':
        return 'Cancelada';
      default:
        return status || 'Desconocido';
    }
  }

  canPauseAuction(auction: Auction): boolean {
    return auction.status === 'ACTIVE' && Boolean(auction.allowPause) && (auction.pausesUsed ?? 0) < 2;
  }

  requestConfirmation(auctionId: string, type: 'delete' | 'cancel'): void {
    this.actionError.set(null);
    this.actionSuccess.set(null);
    this.pendingConfirmation.set({ auctionId, type });
  }

  cancelConfirmation(): void {
    this.pendingConfirmation.set(null);
  }

  confirmPendingAction(): void {
    const pending = this.pendingConfirmation();
    if (!pending) {
      return;
    }

    if (pending.type === 'delete') {
      this.executeDelete(pending.auctionId);
    } else {
      this.executeCancel(pending.auctionId);
    }
  }

  publishAuction(auction: Auction): void {
    if (!auction.id) return;
    this.startAction(auction.id);

    this.auctionService.publishAuction(auction.id).subscribe({
      next: (updated) => {
        this.replaceAuctionInList(updated);
        this.finishAction(`Subasta "${updated.title}" publicada correctamente.`);
      },
      error: (err) => {
        this.failAction(err, 'No se pudo publicar la subasta.');
      }
    });
  }

  pauseAuction(auction: Auction): void {
    if (!auction.id || !this.canPauseAuction(auction)) return;
    this.startAction(auction.id);

    this.auctionService.pauseAuction(auction.id).subscribe({
      next: (updated) => {
        this.replaceAuctionInList(updated);
        this.finishAction(`Subasta "${updated.title}" pausada.`);
      },
      error: (err) => {
        this.failAction(err, 'No se pudo pausar la subasta.');
      }
    });
  }

  resumeAuction(auction: Auction): void {
    if (!auction.id) return;
    this.startAction(auction.id);

    this.auctionService.resumeAuction(auction.id).subscribe({
      next: (updated) => {
        this.replaceAuctionInList(updated);
        this.finishAction(`Subasta "${updated.title}" reanudada.`);
      },
      error: (err) => {
        this.failAction(err, 'No se pudo reanudar la subasta.');
      }
    });
  }

  clearFeedback(): void {
    this.actionError.set(null);
    this.actionSuccess.set(null);
  }

  private executeCancel(auctionId: string): void {
    this.startAction(auctionId);

    this.auctionService.cancelAuction(auctionId).subscribe({
      next: (updated) => {
        this.replaceAuctionInList(updated);
        this.pendingConfirmation.set(null);
        this.finishAction(`Subasta "${updated.title}" cancelada.`);
      },
      error: (err) => {
        this.pendingConfirmation.set(null);
        this.failAction(err, 'No se pudo cancelar la subasta.');
      }
    });
  }

  private executeDelete(auctionId: string): void {
    this.startAction(auctionId);

    this.auctionService.deleteAuction(auctionId).subscribe({
      next: () => {
        this.auctions.update(list => list.filter(a => a.id !== auctionId));
        this.pendingConfirmation.set(null);
        this.finishAction('Borrador eliminado correctamente.');
      },
      error: (err) => {
        this.pendingConfirmation.set(null);
        this.failAction(err, 'No se pudo eliminar el borrador.');
      }
    });
  }

  private startAction(auctionId: string): void {
    this.busyAuctionId.set(auctionId);
    this.actionError.set(null);
    this.actionSuccess.set(null);
  }

  private finishAction(successMsg: string): void {
    this.busyAuctionId.set(null);
    this.actionSuccess.set(successMsg);
  }

  private failAction(err: unknown, fallback: string): void {
    this.busyAuctionId.set(null);
    this.actionError.set(this.extractProblemDetail(err, fallback));
  }

  private replaceAuctionInList(updated: Auction): void {
    this.auctions.update(list =>
      list.map(item => (item.id === updated.id ? updated : item))
    );
  }

  private extractProblemDetail(err: unknown, fallback: string): string {
    const errObj = err as { error?: { detail?: string; message?: string }; message?: string };
    return errObj?.error?.detail || errObj?.error?.message || errObj?.message || fallback;
  }
}
