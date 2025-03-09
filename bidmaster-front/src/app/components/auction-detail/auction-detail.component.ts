import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Auction } from '@shared/models/auction.model';
import { AuctionService } from '@core/services/auction.service';
import { AuthService } from '@core/services/auth.service';
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
  loading: boolean = true;
  error: string | null = null;
  timeLeft: string = '';
  bidForm: FormGroup;
  isSubmittingBid: boolean = false;
  bidError: string | null = null;
  isAuthenticated: boolean = false;
  isOwner: boolean = false;
  
  private destroy$ = new Subject<void>();
  
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private fb = inject(FormBuilder);
  private auctionService = inject(AuctionService);
  private authService = inject(AuthService);
  
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
    // Usar el signal isLoggedIn directamente
    this.isAuthenticated = this.authService.isLoggedIn();
  }
  
  loadAuction(): void {
    this.route.paramMap.pipe(
      switchMap(params => {
        const idOrSlug = params.get('idOrSlug');
        if (!idOrSlug) {
          return of(null);
        }
        
        // Comprobar si es un UUID o un slug
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
        this.updateTimeLeft();
        // Actualizar el tiempo restante cada minuto
        const timeInterval = setInterval(() => this.updateTimeLeft(), 60000);
        this.destroy$.subscribe(() => clearInterval(timeInterval));
        
        // Configurar valor mínimo del formulario de puja
        const minBidAmount = (auction.currentPrice || auction.initialPrice) + 
                             (auction.minBidIncrement || 1);
        this.bidForm.get('amount')?.setValidators([
          Validators.required,
          Validators.min(minBidAmount)
        ]);
        this.bidForm.get('amount')?.setValue(minBidAmount);
        
        // Comprobar si el usuario es el propietario usando el getter currentUserValue
        const currentUser = this.authService.currentUserValue;
        if (currentUser && auction.sellerId) {
          this.isOwner = currentUser.id === auction.sellerId;
        }
      }
    });
  }
  
  updateTimeLeft(): void {
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
          this.bidError = err.error?.message || 'Error al realizar la puja. Por favor, inténtalo de nuevo.';
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
          
          // Actualizar valor mínimo para la siguiente puja
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
