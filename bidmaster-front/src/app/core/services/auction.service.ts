import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Auction, AuctionFilters, CreateAuctionDto } from '@shared/models/auction.model';
import { Observable } from 'rxjs';
import { environment } from '@environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuctionService {
  private http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/auctions`;

  // Crear una nueva subasta
  createAuction(auction: CreateAuctionDto): Observable<Auction> {
    return this.http.post<Auction>(this.apiUrl, auction);
  }

  // Obtener subasta por ID
  getAuctionById(id: string): Observable<Auction> {
    return this.http.get<Auction>(`${this.apiUrl}/${id}`);
  }

  // Obtener subasta por slug
  getAuctionBySlug(slug: string): Observable<Auction> {
    return this.http.get<Auction>(`${this.apiUrl}/slug/${slug}`);
  }

  // Buscar subastas con filtros
  findAuctions(filters?: AuctionFilters): Observable<Auction[]> {
    let params = new HttpParams();
    
    if (filters) {
      if (filters.category) params = params.set('category', filters.category);
      if (filters.status) params = params.set('status', filters.status);
      if (filters.minPrice !== undefined && filters.minPrice !== null) params = params.set('minPrice', filters.minPrice.toString());
      if (filters.maxPrice !== undefined && filters.maxPrice !== null) params = params.set('maxPrice', filters.maxPrice.toString());
      if (filters.sellerId) params = params.set('sellerId', filters.sellerId);
    }
    
    return this.http.get<Auction[]>(this.apiUrl, { params });
  }

  // Actualizar una subasta
  updateAuction(id: string, auction: CreateAuctionDto): Observable<Auction> {
    return this.http.put<Auction>(`${this.apiUrl}/${id}`, auction);
  }

  // Publicar una subasta (cambiar estado de DRAFT a ACTIVE)
  publishAuction(id: string): Observable<Auction> {
    return this.http.post<Auction>(`${this.apiUrl}/${id}/publish`, {});
  }

  // Eliminar una subasta
  deleteAuction(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // Realizar una puja en una subasta
  placeBid(auctionId: string, amount: number): Observable<Auction> {
    return this.http.post<Auction>(`${this.apiUrl}/${auctionId}/bids`, { amount });
  }
}
