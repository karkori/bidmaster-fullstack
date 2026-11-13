import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, ActivatedRoute, convertToParamMap } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { of } from 'rxjs';
import { AuctionDetailComponent } from './auction-detail.component';
import { AuctionService } from '@core/services/auction.service';
import { AuthService } from '@core/services/auth.service';
import { Auction } from '@shared/models/auction.model';

describe('AuctionDetailComponent', () => {
  let fixture: ComponentFixture<AuctionDetailComponent>;
  let component: AuctionDetailComponent;
  let auctionServiceSpy: jasmine.SpyObj<AuctionService>;

  const mockAuction: Auction = {
    id: '11111111-1111-1111-1111-111111111111',
    slug: 'subasta-prueba',
    title: 'Subasta de Prueba',
    description: 'Descripción completa de prueba para la galería de detalle.',
    category: 'Electrónica',
    initialPrice: 100,
    currentPrice: 120,
    minBidIncrement: 5,
    endDate: new Date(Date.now() + 86400000),
    condition: 'good',
    shippingOptions: 'buyer_pays',
    status: 'FINISHED',
    winnerId: 'winner-1',
    winnerUsername: 'ganador',
    images: [
      { id: 'img-1', url: 'https://cdn.example.com/1.jpg', description: null, displayOrder: 0, fileName: '1.jpg', contentType: 'image/jpeg', fileSize: 100, primary: false },
      { id: 'img-2', url: 'https://cdn.example.com/2.jpg', description: null, displayOrder: 1, fileName: '2.jpg', contentType: 'image/jpeg', fileSize: 100, primary: true }
    ]
  };

  beforeEach(async () => {
    auctionServiceSpy = jasmine.createSpyObj('AuctionService', ['getAuctionById', 'getAuctionBySlug', 'placeBid']);
    auctionServiceSpy.getAuctionBySlug.and.returnValue(of(mockAuction));

    await TestBed.configureTestingModule({
      imports: [AuctionDetailComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        { provide: AuctionService, useValue: auctionServiceSpy },
        {
          provide: AuthService,
          useValue: {
            isLoggedIn: () => false,
            currentUserValue: null
          }
        },
        {
          provide: ActivatedRoute,
          useValue: {
            paramMap: of(convertToParamMap({ idOrSlug: 'subasta-prueba' }))
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AuctionDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should initialize active image to primary and allow switching thumbnails', () => {
    expect(component.selectedImageIndex()).toBe(1);
    expect(component.getActiveImageUrl()).toBe('https://cdn.example.com/2.jpg');

    component.selectImage(0);
    expect(component.selectedImageIndex()).toBe(0);
    expect(component.getActiveImageUrl()).toBe('https://cdn.example.com/1.jpg');
  });

  it('should display finished auction state and winner info', () => {
    expect(fixture.nativeElement.textContent).toContain('Subasta finalizada');
    expect(fixture.nativeElement.textContent).toContain('ganador');
  });
});
