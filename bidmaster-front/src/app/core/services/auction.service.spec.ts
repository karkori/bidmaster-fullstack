import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { AuctionService } from './auction.service';
import { Auction, CreateAuctionDto } from '@shared/models/auction.model';
import { environment } from '@environments/environment';

describe('AuctionService', () => {
  let service: AuctionService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AuctionService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(AuctionService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should create an auction', () => {
    const dto: CreateAuctionDto = {
      title: 'Vintage Camera',
      description: 'Classic 35mm film camera in great condition',
      category: 'ELECTRONICS',
      initialPrice: 50.0,
      endDate: '2026-12-01T12:00:00',
      condition: 'good',
      shippingOptions: 'standard'
    };

    const mockResponse: Auction = {
      id: 'auc-1',
      title: 'Vintage Camera',
      description: dto.description,
      category: dto.category,
      initialPrice: 50.0,
      endDate: new Date(dto.endDate),
      condition: 'good',
      shippingOptions: 'standard',
      status: 'DRAFT'
    };

    service.createAuction(dto).subscribe(res => {
      expect(res.id).toBe('auc-1');
      expect(res.title).toBe('Vintage Camera');
    });

    const req = httpTestingController.expectOne(`${environment.apiUrl}/auctions`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(dto);
    req.flush(mockResponse);
  });

  it('should get auction by id', () => {
    const mockAuction: Auction = {
      id: 'auc-123',
      title: 'Rolex Watch',
      description: 'Luxury watch',
      category: 'COLLECTIBLES',
      initialPrice: 1000,
      endDate: new Date('2026-12-31T23:59:59'),
      condition: 'like_new',
      shippingOptions: 'insured'
    };

    service.getAuctionById('auc-123').subscribe(res => {
      expect(res.id).toBe('auc-123');
      expect(res.title).toBe('Rolex Watch');
    });

    const req = httpTestingController.expectOne(`${environment.apiUrl}/auctions/auc-123`);
    expect(req.request.method).toBe('GET');
    req.flush(mockAuction);
  });

  it('should find auctions with filter params', () => {
    service.findAuctions({ category: 'COLLECTIBLES', status: 'ACTIVE' }).subscribe(auctions => {
      expect(auctions.length).toBe(1);
    });

    const req = httpTestingController.expectOne(
      req => req.url === `${environment.apiUrl}/auctions` &&
             req.params.get('category') === 'COLLECTIBLES' &&
             req.params.get('status') === 'ACTIVE'
    );
    expect(req.request.method).toBe('GET');
    req.flush([{ id: 'auc-1', title: 'Coin' }]);
  });

  it('should place a bid', () => {
    const updatedAuction: Auction = {
      id: 'auc-123',
      title: 'Rolex Watch',
      description: 'Luxury watch',
      category: 'COLLECTIBLES',
      initialPrice: 1000,
      currentPrice: 1100,
      endDate: new Date(),
      condition: 'good',
      shippingOptions: 'standard',
      totalBids: 1
    };

    service.placeBid('auc-123', 1100).subscribe(res => {
      expect(res.currentPrice).toBe(1100);
      expect(res.totalBids).toBe(1);
    });

    const req = httpTestingController.expectOne(`${environment.apiUrl}/auctions/auc-123/bids`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ amount: 1100 });
    req.flush(updatedAuction);
  });
});
