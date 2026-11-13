import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import CreateAuctionComponent from './create-auction.component';
import { AuctionService } from '@core/services/auction.service';
import { AuthService } from '@core/services/auth.service';
import { ImageService } from '@core/services/image.service';
import { Auction, ImageResponse } from '@shared/models/auction.model';

describe('CreateAuctionComponent', () => {
  let component: CreateAuctionComponent;
  let fixture: ComponentFixture<CreateAuctionComponent>;
  let auctionServiceSpy: jasmine.SpyObj<AuctionService>;
  let imageServiceSpy: jasmine.SpyObj<ImageService>;
  let authServiceMock: { currentUserValue: { id: string; username: string } | null };
  let router: Router;

  const mockCreatedAuction: Auction = {
    id: 'auc-1',
    title: 'Cámara Leica M6',
    description: 'Cámara telemétrica clásica de 35mm en estado impecable con funda original.',
    slug: 'camara-leica-m6',
    category: 'COLLECTIBLES',
    initialPrice: 1500,
    endDate: new Date(Date.now() + 7 * 86400000),
    condition: 'like_new',
    shippingOptions: 'buyer_pays',
    status: 'DRAFT'
  };

  function setupTestBed(routeId: string | null = null): void {
    auctionServiceSpy = jasmine.createSpyObj('AuctionService', [
      'createAuction',
      'updateAuction',
      'getAuctionById',
      'publishAuction'
    ]);
    imageServiceSpy = jasmine.createSpyObj('ImageService', [
      'validateFile',
      'uploadImage',
      'deleteImage',
      'setPrimaryImage',
      'reorderImages',
      'resolveImageUrl'
    ]);
    imageServiceSpy.validateFile.and.returnValue(null);
    imageServiceSpy.resolveImageUrl.and.callFake((url?: string | null, id?: string) => url || `/api/images/${id}/raw`);

    authServiceMock = {
      currentUserValue: { id: 'seller-1', username: 'seller_test' }
    };

    TestBed.configureTestingModule({
      imports: [CreateAuctionComponent],
      providers: [
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap(routeId ? { id: routeId } : {}) } }
        },
        { provide: AuctionService, useValue: auctionServiceSpy },
        { provide: AuthService, useValue: authServiceMock },
        { provide: ImageService, useValue: imageServiceSpy }
      ]
    });
  }

  describe('Create mode', () => {
    beforeEach(async () => {
      setupTestBed(null);
      await TestBed.compileComponents();
      fixture = TestBed.createComponent(CreateAuctionComponent);
      component = fixture.componentInstance;
      router = TestBed.inject(Router);
      spyOn(router, 'navigate').and.resolveTo(true);
      fixture.detectChanges();
    });

    function fillValidForm(): void {
      const future = new Date(Date.now() + 48 * 3600 * 1000);
      const futureStr = future.toISOString().slice(0, 16);
      component.auctionForm.patchValue({
        title: 'Cámara Leica M6',
        description: 'Cámara telemétrica clásica de 35mm en estado impecable con funda original.',
        category: 'COLLECTIBLES',
        initialPrice: 1500,
        minBidIncrement: 25,
        depositRequired: 50,
        endDate: futureStr,
        condition: 'like_new',
        shippingOptions: 'buyer_pays',
        allowPause: true,
        terms: true
      });
    }

    it('should reject invalid form via Zod schema without calling backend', async () => {
      component.auctionForm.patchValue({ title: 'Abc', description: 'Corta', terms: false });
      await component.submitAuction(true);

      expect(auctionServiceSpy.createAuction).not.toHaveBeenCalled();
      expect(component.getControlError('title')).toContain('al menos 5 caracteres');
    });

    it('should handle image queue selection, primary toggling, and removal', () => {
      const f1 = new File([new Uint8Array([1, 2, 3])], 'foto1.png', { type: 'image/png' });
      const f2 = new File([new Uint8Array([4, 5, 6])], 'foto2.png', { type: 'image/png' });

      component.addFiles([f1, f2]);
      expect(component.pendingImages().length).toBe(2);
      expect(component.pendingImages()[0].isPrimary).toBeTrue();
      expect(component.pendingImages()[1].isPrimary).toBeFalse();

      const secondId = component.pendingImages()[1].id;
      component.setPrimaryImage(secondId);
      expect(component.pendingImages()[0].isPrimary).toBeFalse();
      expect(component.pendingImages()[1].isPrimary).toBeTrue();

      component.removeImage(secondId);
      expect(component.pendingImages().length).toBe(1);
      expect(component.pendingImages()[0].isPrimary).toBeTrue();
    });

    it('should save as DRAFT with images without calling publishAuction', async () => {
      fillValidForm();
      const f1 = new File([new Uint8Array([1, 2, 3])], 'foto1.png', { type: 'image/png' });
      component.addFiles([f1]);

      auctionServiceSpy.createAuction.and.returnValue(of(mockCreatedAuction));
      imageServiceSpy.uploadImage.and.returnValue(of({ id: 'img-1' } as ImageResponse));

      await component.submitAuction(false);

      expect(auctionServiceSpy.createAuction).toHaveBeenCalled();
      expect(imageServiceSpy.uploadImage).toHaveBeenCalledWith(f1, 'AUCTION', 'auc-1', {
        isPrimary: true,
        displayOrder: 0
      });
      expect(auctionServiceSpy.publishAuction).not.toHaveBeenCalled();
      expect(router.navigate).toHaveBeenCalledWith(['/auctions', 'camara-leica-m6']);
    });

    it('should create, upload images, publish immediately, and navigate to detail', async () => {
      fillValidForm();
      const f1 = new File([new Uint8Array([1, 2, 3])], 'foto1.png', { type: 'image/png' });
      component.addFiles([f1]);

      auctionServiceSpy.createAuction.and.returnValue(of(mockCreatedAuction));
      imageServiceSpy.uploadImage.and.returnValue(of({ id: 'img-1' } as ImageResponse));
      auctionServiceSpy.publishAuction.and.returnValue(of({ ...mockCreatedAuction, status: 'ACTIVE' }));

      await component.submitAuction(true);

      expect(auctionServiceSpy.createAuction).toHaveBeenCalled();
      expect(imageServiceSpy.uploadImage).toHaveBeenCalled();
      expect(auctionServiceSpy.publishAuction).toHaveBeenCalledWith('auc-1');
      expect(router.navigate).toHaveBeenCalledWith(['/auctions', 'camara-leica-m6']);
    });

    it('should retain createdDraftAuction when image upload fails so retry does not duplicate', async () => {
      fillValidForm();
      const f1 = new File([new Uint8Array([1, 2, 3])], 'foto1.png', { type: 'image/png' });
      component.addFiles([f1]);

      auctionServiceSpy.createAuction.and.returnValue(of(mockCreatedAuction));
      imageServiceSpy.uploadImage.and.returnValue(throwError(() => ({ error: { detail: 'Imagen corrupta' } })));

      await component.submitAuction(true);

      expect(component.createdDraftAuction()?.id).toBe('auc-1');
      expect(component.errorMessage).toContain('Imagen corrupta');
      expect(component.isSubmitting).toBeFalse();
    });
  });

  describe('Edit mode (/dashboard/edit-auction/:id)', () => {
    const futureDate = new Date(Date.now() + 5 * 86400000);
    const existingDraft: Auction = {
      id: 'auc-edit-1',
      title: 'Portátil ThinkPad X1 Carbon',
      description: 'Portátil empresarial ultraligero en estado excelente con cargador USB-C.',
      slug: 'portatil-thinkpad-x1-carbon',
      category: 'Electrónica',
      initialPrice: 900,
      minBidIncrement: 15,
      depositRequired: 20,
      endDate: futureDate,
      condition: 'like_new',
      shippingOptions: 'seller_pays',
      allowPause: true,
      status: 'DRAFT',
      sellerId: 'seller-1',
      images: [
        {
          id: 'img-a',
          url: '/api/images/img-a/raw',
          description: null,
          displayOrder: 0,
          fileName: 'frontal.png',
          contentType: 'image/png',
          fileSize: 1024,
          primary: true
        },
        {
          id: 'img-b',
          url: '/api/images/img-b/raw',
          description: null,
          displayOrder: 1,
          fileName: 'lateral.png',
          contentType: 'image/png',
          fileSize: 2048,
          primary: false
        }
      ]
    };

    beforeEach(() => {
      setupTestBed('auc-edit-1');
    });

    it('should load DRAFT auction, map displayName category to enum, and save changes with image updates', async () => {
      auctionServiceSpy.getAuctionById.and.returnValue(of(existingDraft));
      auctionServiceSpy.updateAuction.and.returnValue(of({ ...existingDraft, title: 'ThinkPad X1 Gen 11 Editado' }));
      imageServiceSpy.deleteImage.and.returnValue(of(void 0));
      imageServiceSpy.setPrimaryImage.and.returnValue(of({ id: 'img-b', primary: true } as ImageResponse));
      imageServiceSpy.reorderImages.and.returnValue(of([{ id: 'img-b' } as ImageResponse]));

      await TestBed.compileComponents();
      fixture = TestBed.createComponent(CreateAuctionComponent);
      component = fixture.componentInstance;
      router = TestBed.inject(Router);
      spyOn(router, 'navigate').and.resolveTo(true);
      fixture.detectChanges();

      expect(component.isEditMode()).toBeTrue();
      expect(component.auctionForm.get('category')?.value).toBe('ELECTRONICS');
      expect(component.existingImages().length).toBe(2);

      // Cambiar portada a img-b y eliminar img-a
      component.setExistingPrimaryImage('img-b');
      component.removeExistingImage('img-a');
      component.auctionForm.patchValue({ title: 'ThinkPad X1 Gen 11 Editado' });

      await component.submitAuction(false);

      expect(auctionServiceSpy.updateAuction).toHaveBeenCalledWith('auc-edit-1', jasmine.objectContaining({
        title: 'ThinkPad X1 Gen 11 Editado',
        category: 'ELECTRONICS'
      }));
      expect(imageServiceSpy.deleteImage).toHaveBeenCalledWith('img-a');
      expect(imageServiceSpy.reorderImages).toHaveBeenCalledWith('auc-edit-1', ['img-b'], 'AUCTION');
      expect(imageServiceSpy.setPrimaryImage).toHaveBeenCalledWith('img-b');
      expect(auctionServiceSpy.publishAuction).not.toHaveBeenCalled();
      expect(router.navigate).toHaveBeenCalledWith(['/dashboard/my-auctions']);
    });

    it('should block editing if auction is not in DRAFT status', async () => {
      auctionServiceSpy.getAuctionById.and.returnValue(of({ ...existingDraft, status: 'ACTIVE' }));

      await TestBed.compileComponents();
      fixture = TestBed.createComponent(CreateAuctionComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();

      expect(component.editBlockedReason()).toContain('borrador');
      await component.submitAuction(false);
      expect(auctionServiceSpy.updateAuction).not.toHaveBeenCalled();
    });
  });
});
