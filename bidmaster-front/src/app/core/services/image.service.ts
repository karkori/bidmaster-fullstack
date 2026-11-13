import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { environment } from '@environments/environment';
import { ImageResponse, ImageType, ImageUploadOptions, ReorderImagesRequest } from '@shared/models/auction.model';
import { validateImageFile } from '@shared/validators/auction.schemas';
import { Observable, throwError } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ImageService {
  private http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/images`;
  private readonly backendOrigin = environment.apiUrl.replace(/\/api\/?$/, '');

  validateFile(file: File): string | null {
    return validateImageFile(file);
  }

  uploadImage(
    file: File,
    type: ImageType,
    referenceId?: string,
    options?: ImageUploadOptions
  ): Observable<ImageResponse> {
    const validationError = this.validateFile(file);
    if (validationError) {
      return throwError(() => new Error(validationError));
    }

    const formData = new FormData();
    formData.append('file', file, file.name);
    formData.append('type', type);

    if (referenceId) {
      formData.append('referenceId', referenceId);
    }
    if (options?.userId) {
      formData.append('userId', options.userId);
    }
    formData.append('isPrimary', String(Boolean(options?.isPrimary)));
    formData.append('displayOrder', String(options?.displayOrder ?? 0));

    return this.http.post<ImageResponse>(this.apiUrl, formData);
  }

  getImagesByReference(referenceId: string, type: ImageType = 'AUCTION'): Observable<ImageResponse[]> {
    const params = new HttpParams().set('type', type);
    return this.http.get<ImageResponse[]>(`${this.apiUrl}/reference/${referenceId}`, { params });
  }

  setPrimaryImage(imageId: string): Observable<ImageResponse> {
    return this.http.patch<ImageResponse>(`${this.apiUrl}/${imageId}/primary`, {});
  }

  reorderImages(
    referenceId: string,
    imageIds: string[],
    type: ImageType = 'AUCTION'
  ): Observable<ImageResponse[]> {
    const params = new HttpParams().set('type', type);
    const body: ReorderImagesRequest = { imageIds };
    return this.http.patch<ImageResponse[]>(`${this.apiUrl}/reference/${referenceId}/order`, body, { params });
  }

  deleteImage(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  resolveImageUrl(url?: string | null, imageId?: string): string {
    if (!url || url.trim() === '' || url.includes('/api/images/raw/')) {
      return imageId ? `${this.apiUrl}/${imageId}/raw` : '/no-image.svg';
    }
    if (url.startsWith('/api/')) {
      return `${this.backendOrigin}${url}`;
    }
    return url;
  }
}
