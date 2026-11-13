import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Auction, AuctionImage } from '@shared/models/auction.model';
import { AuctionService } from '@core/services/auction.service';
import { AuthService } from '@core/services/auth.service';
import { ImageService } from '@core/services/image.service';
import { FormControlDirective, FormLabelDirective } from '@shared/directives';
import { catchError, switchMap, of, takeUntil, Subject } from 'rxjs';
import {
  differenceInDays,
  differenceInHours,
  differenceInMinutes,
  isPast,
  format
} from 'date-fns';
import { es } from 'date-fns/locale';

@Component({
  selector: 'app-auction-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    FormControlDirective,
    FormLabelDirective
  ],
  templateUrl: './auction-detail.component.html'
})
export class AuctionDetailComponent implements OnInit, OnDestroy {
  auction: Auction | null = null;
  loading = true;
  error: string | null = null;
  timeLeft = '';
  bidForm: FormGroup;
  isSubmittingBid = false;
  bidError: string | null = null;
  isAuthenticated = false;
  isOwner = false;
  selectedImageIndex = signal(0);

  private destroy$ = new Subject<void>();

  private route = inject(ActivatedRoute);
  private fb = inject(FormBuilder);
  private auctionService = inject(AuctionService);
  private authService = inject(AuthService);
  private imageService = inject(ImageService);

  constructor() {
    this.bidForm = this.fb.group({
      amount: ['', [Validators.required, Validators.min(0.01)]]
    });
  }

  ngOnInit(): void {
    this.checkAuthentication();
    this.loadAuction();
  }

  checkAuthentication(): void {
    this.isAuthenticated = this.authService.isLoggedIn();
  }

  loadAuction(): void {
    this.route.paramMap.pipe(
      switchMap(params => {
        const idOrSlug = params.get('idOrSlug');
        if (!idOrSlug) {
          return of(null);
        }

        const isUuid = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(idOrSlug);

        return isUuid
          ? this.auctionService.getAuctionById(idOrSlug)
          : this.auctionService.getAuctionBySlug(idOrSlug);
      }),
      catchError(err => {
        this.error = 'No se pudo cargar la subasta. Por favor, inténtalo de nuevo.';
        console.error('Error cargando subasta:', err);
        return of(null);
      }),
      takeUntil(this.destroy$)
    ).subscribe(auction => {
      this.auction = auction;
      this.loading = false;

      if (auction) {
        const imgs = auction.images ?? [];
        const primaryIndex = imgs.findIndex(img => img.primary);
        this.selectedImageIndex.set(primaryIndex >= 0 ? primaryIndex : 0);

        this.updateTimeLeft();
        const timeInterval = setInterval(() => this.updateTimeLeft(), 60000);
        this.destroy$.subscribe(() => clearInterval(timeInterval));

        const minBidAmount = (auction.currentPrice || auction.initialPrice) +
                             (auction.minBidIncrement || 1);
        this.bidForm.get('amount')?.setValidators([
          Validators.required,
          Validators.min(minBidAmount)
        ]);
        this.bidForm.get('amount')?.setValue(minBidAmount);

        const currentUser = this.authService.currentUserValue;
        if (currentUser && auction.sellerId) {
          this.isOwner = currentUser.id === auction.sellerId;
        }
      }
    });
  }

  selectImage(index: number): void {
    const imgs = this.auction?.images;
    if (!imgs || index < 0 || index >= imgs.length) {
      return;
    }
    this.selectedImageIndex.set(index);
  }

  getActiveImageUrl(): string {
    const imgs = this.auction?.images;
    if (!imgs || imgs.length === 0) {
      return '/no-image.svg';
    }
    const idx = Math.min(Math.max(0, this.selectedImageIndex()), imgs.length - 1);
    return this.getImageUrl(imgs[idx]);
  }

  getImageUrl(image: AuctionImage): string {
    return this.imageService.resolveImageUrl(image.url, image.id);
  }

  updateTimeLeft(): void {
    if (this.auction?.status === 'FINISHED') {
      this.timeLeft = 'Finalizada';
      return;
    }
    if (this.auction?.status === 'CANCELLED') {
      this.timeLeft = 'Cancelada';
      return;
    }
    if (this.auction?.status === 'PAUSED') {
      this.timeLeft = 'Pausada';
      return;
    }
    if (!this.auction?.endDate) {
      this.timeLeft = 'Finalizada';
      return;
    }

    const endDate = new Date(this.auction.endDate);

    if (isPast(endDate)) {
      this.timeLeft = 'Finalizada';
      return;
    }

    const now = new Date();
    const days = differenceInDays(endDate, now);
    const hours = differenceInHours(endDate, now) % 24;
    const minutes = differenceInMinutes(endDate, now) % 60;

    if (days > 0) {
      this.timeLeft = `${days} días, ${hours} horas`;
    } else if (hours > 0) {
      this.timeLeft = `${hours} horas, ${minutes} minutos`;
    } else {
      this.timeLeft = `${minutes} minutos`;
    }
  }

  formatDate(date: string | Date | undefined): string {
    if (!date) return 'No disponible';
    return format(new Date(date), 'PPpp', { locale: es });
  }

  submitBid(): void {
    if (!this.auction || !this.bidForm.valid || !this.isAuthenticated) return;

    this.isSubmittingBid = true;
    this.bidError = null;

    const amount = this.bidForm.value.amount;

    this.auctionService.placeBid(this.auction.id!, amount)
      .pipe(
        catchError(err => {
          this.bidError = err.error?.detail || err.error?.message || 'Error al realizar la puja. Por favor, inténtalo de nuevo.';
          console.error('Error realizando puja:', err);
          return of(null);
        }),
        takeUntil(this.destroy$)
      )
      .subscribe(updatedAuction => {
        this.isSubmittingBid = false;

        if (updatedAuction) {
          this.auction = updatedAuction;
          this.updateTimeLeft();

          const minBidAmount = (updatedAuction.currentPrice || updatedAuction.initialPrice) +
                               (updatedAuction.minBidIncrement || 1);
          this.bidForm.get('amount')?.setValue(minBidAmount);
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
