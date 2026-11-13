import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { AuctionCardComponent } from './auction-card.component';
import { Auction } from '@shared/models/auction.model';

describe('AuctionCardComponent', () => {
  let fixture: ComponentFixture<AuctionCardComponent>;
  let component: AuctionCardComponent;

  const baseAuction: Auction = {
    id: 'auc-1',
    slug: 'reloj-vintage',
    title: 'Reloj Vintage',
    description: 'Reloj mecánico suizo en perfecto estado de conservación.',
    category: 'Coleccionables',
    initialPrice: 100,
    currentPrice: 150,
    endDate: new Date(Date.now() + 86400000),
    condition: 'like_new',
    shippingOptions: 'buyer_pays',
    status: 'ACTIVE',
    totalBids: 2,
    images: []
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AuctionCardComponent],
      providers: [provideRouter([]), provideHttpClient()]
    }).compileComponents();

    fixture = TestBed.createComponent(AuctionCardComponent);
    component = fixture.componentInstance;
  });

  it('should fallback to /no-image.svg when images array is empty', () => {
    fixture.componentRef.setInput('auction', baseAuction);
    fixture.detectChanges();

    expect(component.imageUrl()).toBe('/no-image.svg');
    expect(fixture.nativeElement.textContent).toContain('Activa');
  });

  it('should fallback to first image when none is marked primary, and prefer primary when present', () => {
    const noPrimaryAuction: Auction = {
      ...baseAuction,
      images: [
        { id: 'img-1', url: 'https://cdn.example.com/1.jpg', description: null, displayOrder: 0, fileName: '1.jpg', contentType: 'image/jpeg', fileSize: 100, primary: false },
        { id: 'img-2', url: 'https://cdn.example.com/2.jpg', description: null, displayOrder: 1, fileName: '2.jpg', contentType: 'image/jpeg', fileSize: 100, primary: false }
      ]
    };
    fixture.componentRef.setInput('auction', noPrimaryAuction);
    fixture.detectChanges();
    expect(component.imageUrl()).toBe('https://cdn.example.com/1.jpg');

    const withPrimaryAuction: Auction = {
      ...baseAuction,
      images: [
        noPrimaryAuction.images![0],
        { ...noPrimaryAuction.images![1], primary: true }
      ]
    };
    fixture.componentRef.setInput('auction', withPrimaryAuction);
    fixture.detectChanges();
    expect(component.imageUrl()).toBe('https://cdn.example.com/2.jpg');
  });

  it('should render badges for FINISHED, PAUSED, and CANCELLED statuses', () => {
    fixture.componentRef.setInput('auction', { ...baseAuction, status: 'FINISHED' });
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Finalizada');

    fixture.componentRef.setInput('auction', { ...baseAuction, status: 'PAUSED' });
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Pausada');

    fixture.componentRef.setInput('auction', { ...baseAuction, status: 'CANCELLED' });
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Cancelada');
  });
});
