import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import MyAuctionsComponent from './my-auctions.component';
import { AuctionService } from '@core/services/auction.service';
import { AuthService } from '@core/services/auth.service';
import { ImageService } from '@core/services/image.service';
import { Auction } from '@shared/models/auction.model';

describe('MyAuctionsComponent', () => {
  let component: MyAuctionsComponent;
  let fixture: ComponentFixture<MyAuctionsComponent>;
  let auctionServiceSpy: jasmine.SpyObj<AuctionService>;
  let authServiceMock: { currentUserValue: { id: string; username: string } | null };
  let imageServiceSpy: jasmine.SpyObj<ImageService>;

  const futureDate = new Date(Date.now() + 72 * 3600 * 1000);

  const mockAuctions: Auction[] = [
    {
      id: 'auc-draft',
      title: 'Cámara Leica Borrador',
      description: 'Cámara clásica en borrador para editar',
      slug: 'camara-leica-borrador',
      category: 'Coleccionables',
      initialPrice: 800,
      currentPrice: 800,
      endDate: futureDate,
      condition: 'like_new',
      shippingOptions: 'buyer_pays',
      status: 'DRAFT',
      allowPause: true,
      pausesUsed: 0,
      totalBids: 0,
      sellerId: 'seller-1'
    },
    {
      id: 'auc-active',
      title: 'MacBook Pro Activo',
      description: 'Portátil activo con opción de pausa',
      slug: 'macbook-pro-activo',
      category: 'Electrónica',
      initialPrice: 1500,
      currentPrice: 1650,
      endDate: futureDate,
      condition: 'good',
      shippingOptions: 'seller_pays',
      status: 'ACTIVE',
      allowPause: true,
      pausesUsed: 1,
      totalBids: 3,
      sellerId: 'seller-1'
    },
    {
      id: 'auc-paused',
      title: 'Bicicleta Carretera Pausada',
      description: 'Bicicleta pausada temporalmente',
      slug: 'bicicleta-carretera-pausada',
      category: 'Deportes',
      initialPrice: 600,
      currentPrice: 600,
      endDate: futureDate,
      condition: 'good',
      shippingOptions: 'pickup_only',
      status: 'PAUSED',
      allowPause: true,
      pausesUsed: 2,
      totalBids: 0,
      sellerId: 'seller-1'
    }
  ];

  beforeEach(async () => {
    auctionServiceSpy = jasmine.createSpyObj('AuctionService', [
      'findAuctions',
      'publishAuction',
      'pauseAuction',
      'resumeAuction',
      'cancelAuction',
      'deleteAuction'
    ]);
    auctionServiceSpy.findAuctions.and.returnValue(of(mockAuctions));

    authServiceMock = {
      currentUserValue: { id: 'seller-1', username: 'seller_test' }
    };

    imageServiceSpy = jasmine.createSpyObj('ImageService', ['resolveImageUrl']);
    imageServiceSpy.resolveImageUrl.and.returnValue('/no-image.svg');

    await TestBed.configureTestingModule({
      imports: [MyAuctionsComponent],
      providers: [
        provideRouter([]),
        { provide: AuctionService, useValue: auctionServiceSpy },
        { provide: AuthService, useValue: authServiceMock },
        { provide: ImageService, useValue: imageServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(MyAuctionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should load seller auctions on init and filter by status and search term', () => {
    expect(auctionServiceSpy.findAuctions).toHaveBeenCalledWith({ sellerId: 'seller-1' });
    expect(component.auctions().length).toBe(3);
    expect(component.filteredAuctions().length).toBe(3);

    component.onFilterChange('DRAFT');
    expect(component.filteredAuctions().length).toBe(1);
    expect(component.filteredAuctions()[0].id).toBe('auc-draft');

    component.onFilterChange('ALL');
    component.onSearchChange('MacBook');
    expect(component.filteredAuctions().length).toBe(1);
    expect(component.filteredAuctions()[0].id).toBe('auc-active');
  });

  it('should publish a draft auction and update its status in place', () => {
    const published: Auction = { ...mockAuctions[0], status: 'ACTIVE' };
    auctionServiceSpy.publishAuction.and.returnValue(of(published));

    component.publishAuction(mockAuctions[0]);

    expect(auctionServiceSpy.publishAuction).toHaveBeenCalledWith('auc-draft');
    expect(component.auctions().find(a => a.id === 'auc-draft')?.status).toBe('ACTIVE');
    expect(component.actionSuccess()).toContain('publicada');
  });

  it('should disable pause when allowPause is false or pausesUsed >= 2, and allow when pausesUsed < 2', () => {
    expect(component.canPauseAuction(mockAuctions[1])).toBeTrue();
    expect(component.canPauseAuction({ ...mockAuctions[1], pausesUsed: 2 })).toBeFalse();
    expect(component.canPauseAuction({ ...mockAuctions[1], allowPause: false })).toBeFalse();
  });

  it('should pause and resume auctions', () => {
    const paused: Auction = { ...mockAuctions[1], status: 'PAUSED', pausesUsed: 2 };
    auctionServiceSpy.pauseAuction.and.returnValue(of(paused));

    component.pauseAuction(mockAuctions[1]);
    expect(auctionServiceSpy.pauseAuction).toHaveBeenCalledWith('auc-active');
    expect(component.auctions().find(a => a.id === 'auc-active')?.status).toBe('PAUSED');

    const resumed: Auction = { ...mockAuctions[2], status: 'ACTIVE' };
    auctionServiceSpy.resumeAuction.and.returnValue(of(resumed));

    component.resumeAuction(mockAuctions[2]);
    expect(auctionServiceSpy.resumeAuction).toHaveBeenCalledWith('auc-paused');
    expect(component.auctions().find(a => a.id === 'auc-paused')?.status).toBe('ACTIVE');
  });

  it('should require inline confirmation before deleting draft or cancelling auction', () => {
    auctionServiceSpy.deleteAuction.and.returnValue(of(void 0));

    component.requestConfirmation('auc-draft', 'delete');
    expect(auctionServiceSpy.deleteAuction).not.toHaveBeenCalled();

    component.confirmPendingAction();
    expect(auctionServiceSpy.deleteAuction).toHaveBeenCalledWith('auc-draft');
    expect(component.auctions().some(a => a.id === 'auc-draft')).toBeFalse();
  });

  it('should surface ProblemDetail error message when a lifecycle call fails', () => {
    auctionServiceSpy.pauseAuction.and.returnValue(
      throwError(() => ({ error: { detail: 'No se pueden realizar más pausas' } }))
    );

    component.pauseAuction(mockAuctions[1]);
    expect(component.actionError()).toBe('No se pueden realizar más pausas');
  });
});
