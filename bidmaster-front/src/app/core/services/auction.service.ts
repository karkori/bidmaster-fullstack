import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Auction, AuctionFilters, CreateAuctionDto } from '@shared/models/auction.model';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

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
    let url = this.apiUrl;
    
    if (filters) {
      const params = new URLSearchParams();
      
      if (filters.category) params.append('category', filters.category);
      if (filters.status) params.append('status', filters.status);
      if (filters.minPrice) params.append('minPrice', filters.minPrice.toString());
      if (filters.maxPrice) params.append('maxPrice', filters.maxPrice.toString());
      if (filters.sellerId) params.append('sellerId', filters.sellerId);
      
      if (params.toString()) {
        url += `?${params.toString()}`;
      }
    }
    
    return this.http.get<Auction[]>(url);
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
