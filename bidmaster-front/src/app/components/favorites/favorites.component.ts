import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

interface Favorite {
  id: string;
  auctionId: string;
  title: string;
  currentPrice: number;
  endDate: Date;
  imageUrl: string;
  bids: number;
  isActive: boolean;
}

@Component({
  selector: 'app-favorites',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './favorites.component.html',
  styleUrls: []
})
export default class FavoritesComponent implements OnInit {
  favorites: Favorite[] = [];
  filteredFavorites: Favorite[] = [];
  loading = true;
  currentFilter = 'all';
  searchTerm = '';

  ngOnInit(): void {
    // Simulación de carga de datos
    setTimeout(() => {
      this.favorites = this.getMockFavorites();
      this.filteredFavorites = [...this.favorites];
      this.loading = false;
    }, 1000);
  }
  
  filterFavorites(filter: string): void {
    this.currentFilter = filter;
    
    switch (filter) {
      case 'all':
        this.filteredFavorites = this.favorites.filter(favorite => 
          favorite.title.toLowerCase().includes(this.searchTerm.toLowerCase())
        );
        break;
      case 'active':
        this.filteredFavorites = this.favorites.filter(favorite => 
          favorite.isActive && 
          favorite.title.toLowerCase().includes(this.searchTerm.toLowerCase())
        );
        break;
      case 'ended':
        this.filteredFavorites = this.favorites.filter(favorite => 
          !favorite.isActive && 
          favorite.title.toLowerCase().includes(this.searchTerm.toLowerCase())
        );
        break;
    }
  }
  
  onSearch(event: Event): void {
    this.searchTerm = (event.target as HTMLInputElement).value;
    this.filterFavorites(this.currentFilter);
  }
  
  removeFavorite(id: string): void {
    // En una aplicación real, aquí se eliminaría el favorito de la base de datos
    this.favorites = this.favorites.filter(favorite => favorite.id !== id);
    this.filteredFavorites = this.filteredFavorites.filter(favorite => favorite.id !== id);
  }
  
  placeBid(auctionId: string): void {
    console.log('Realizar puja en la subasta:', auctionId);
    // Aquí iría la lógica para realizar una puja
  }
  
  getRelativeTime(date: Date): string {
    const now = new Date();
    
    if (date < now) {
      return 'Finalizado';
    }
    
    const diffMs = date.getTime() - now.getTime();
    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));
    const diffHours = Math.floor((diffMs % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
    
    if (diffDays > 0) {
      return `${diffDays}d ${diffHours}h`;
    }
    
    const diffMinutes = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60));
    if (diffHours > 0) {
      return `${diffHours}h ${diffMinutes}m`;
    }
    
    const diffSeconds = Math.floor((diffMs % (1000 * 60)) / 1000);
    if (diffMinutes > 0) {
      return `${diffMinutes}m ${diffSeconds}s`;
    }
    
    return `${diffSeconds}s`;
  }
  
  // Datos de ejemplo para desarrollo
  private getMockFavorites(): Favorite[] {
    const now = new Date();
    const tomorrow = new Date(now);
    tomorrow.setDate(tomorrow.getDate() + 1);
    
    const nextWeek = new Date(now);
    nextWeek.setDate(nextWeek.getDate() + 7);
    
    const lastWeek = new Date(now);
    lastWeek.setDate(lastWeek.getDate() - 7);
    
    return [
      {
        id: '1',
        auctionId: 'a1',
        title: 'iPhone 15 Pro Max - 256GB - Titanio Natural',
        currentPrice: 950.00,
        endDate: tomorrow,
        imageUrl: 'https://placehold.co/600x400/3b82f6/white?text=iPhone+15',
        bids: 15,
        isActive: true
      },
      {
        id: '2',
        auctionId: 'a2',
        title: 'PlayStation 5 Digital Edition + Extra Controller',
        currentPrice: 420.50,
        endDate: nextWeek,
        imageUrl: 'https://placehold.co/600x400/6366f1/white?text=PS5',
        bids: 8,
        isActive: true
      },
      {
        id: '3',
        auctionId: 'a3',
        title: 'Bicicleta Montaña Trek Marlin 7 2024',
        currentPrice: 780.00,
        endDate: lastWeek,
        imageUrl: 'https://placehold.co/600x400/10b981/white?text=MTB+Trek',
        bids: 12,
        isActive: false
      },
      {
        id: '4',
        auctionId: 'a4',
        title: 'Reloj Apple Watch Series 9 GPS 45mm',
        currentPrice: 320.25,
        endDate: tomorrow,
        imageUrl: 'https://placehold.co/600x400/ef4444/white?text=Apple+Watch',
        bids: 5,
        isActive: true
      },
      {
        id: '5',
        auctionId: 'a5',
        title: 'MacBook Air M3 - 16GB RAM - 512GB SSD',
        currentPrice: 1250.00,
        endDate: lastWeek,
        imageUrl: 'https://placehold.co/600x400/64748b/white?text=MacBook+Air',
        bids: 20,
        isActive: false
      }
    ];
  }
}
