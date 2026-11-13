import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { ImageService } from './image.service';
import { environment } from '@environments/environment';
import { ImageResponse } from '@shared/models/auction.model';

describe('ImageService', () => {
  let service: ImageService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ImageService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(ImageService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should upload valid image with FormData including isPrimary and displayOrder', () => {
    const file = new File([new Uint8Array([0x89, 0x50, 0x4e, 0x47])], 'cover.png', { type: 'image/png' });
    const mockResponse: ImageResponse = {
      id: 'img-1',
      filename: 'cover.png',
      originalFilename: 'cover.png',
      contentType: 'image/png',
      size: 4,
      imageUrl: '/api/images/img-1/raw',
      uploadDate: '2026-11-12T10:00:00',
      uploadedBy: 'user-1',
      type: 'AUCTION',
      referenceId: 'auc-1',
      primary: true,
      displayOrder: 0
    };

    service.uploadImage(file, 'AUCTION', 'auc-1', { isPrimary: true, displayOrder: 0 }).subscribe(res => {
      expect(res.id).toBe('img-1');
      expect(res.primary).toBeTrue();
    });

    const req = httpTestingController.expectOne(`${environment.apiUrl}/images`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body instanceof FormData).toBeTrue();
    const formData = req.request.body as FormData;
    expect(formData.get('type')).toBe('AUCTION');
    expect(formData.get('referenceId')).toBe('auc-1');
    expect(formData.get('isPrimary')).toBe('true');
    expect(formData.get('displayOrder')).toBe('0');
    req.flush(mockResponse);
  });

  it('should reject unsupported file types or files larger than 5MB before making HTTP call', () => {
    const badFile = new File(['<?php echo 1; ?>'], 'evil.php', { type: 'application/x-php' });

    service.uploadImage(badFile, 'AUCTION', 'auc-1').subscribe({
      next: () => fail('Expected validation error'),
      error: (err: Error) => {
        expect(err.message).toContain('Formato no soportado');
      }
    });

    const hugeFile = new File([new Uint8Array(5 * 1024 * 1024 + 1)], 'huge.png', { type: 'image/png' });
    service.uploadImage(hugeFile, 'AUCTION', 'auc-1').subscribe({
      next: () => fail('Expected size error'),
      error: (err: Error) => {
        expect(err.message).toContain('5 MB');
      }
    });

    httpTestingController.expectNone(`${environment.apiUrl}/images`);
  });

  it('should resolve relative /api URLs, empty URLs, and absolute URLs properly', () => {
    expect(service.resolveImageUrl('/api/images/img-1/raw', 'img-1'))
      .toBe('http://localhost:8080/api/images/img-1/raw');
    expect(service.resolveImageUrl('', 'img-2'))
      .toBe(`${environment.apiUrl}/images/img-2/raw`);
    expect(service.resolveImageUrl('https://cdn.example.com/photo.jpg', 'img-3'))
      .toBe('https://cdn.example.com/photo.jpg');
    expect(service.resolveImageUrl(null))
      .toBe('/no-image.svg');
  });

  it('should set an existing image as primary via PATCH', () => {
    service.setPrimaryImage('img-2').subscribe(res => {
      expect(res.id).toBe('img-2');
      expect(res.primary).toBeTrue();
    });

    const req = httpTestingController.expectOne(`${environment.apiUrl}/images/img-2/primary`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({});
    req.flush({ id: 'img-2', primary: true });
  });

  it('should reorder images via PATCH with type param and imageIds payload', () => {
    service.reorderImages('auc-1', ['img-2', 'img-1'], 'AUCTION').subscribe(res => {
      expect(res.length).toBe(2);
      expect(res[0].id).toBe('img-2');
    });

    const req = httpTestingController.expectOne(
      r => r.url === `${environment.apiUrl}/images/reference/auc-1/order` &&
           r.params.get('type') === 'AUCTION'
    );
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ imageIds: ['img-2', 'img-1'] });
    req.flush([
      { id: 'img-2', displayOrder: 0 },
      { id: 'img-1', displayOrder: 1 }
    ]);
  });
});
