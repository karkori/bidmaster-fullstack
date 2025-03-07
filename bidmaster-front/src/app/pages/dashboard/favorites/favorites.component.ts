import { Component, OnInit } from '@angular/core';
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
  template: `
    <div class="max-w-6xl mx-auto">
      <div class="bg-white rounded-lg shadow-md overflow-hidden mb-6">
        <div class="p-6 bg-gradient-to-r from-pink-600 to-pink-800 text-white">
          <h1 class="text-2xl font-bold">Mis favoritos</h1>
          <p class="opacity-80">Subastas que estás siguiendo</p>
        </div>
        
        <div class="px-4 py-3 bg-pink-50 border-b border-pink-100">
          <div class="flex items-center justify-between">
            <div class="flex space-x-4">
              <button 
                (click)="filterFavorites('all')" 
                [class.bg-pink-100]="currentFilter === 'all'"
                class="px-3 py-1 rounded-md text-sm font-medium text-pink-800 hover:bg-pink-100"
              >
                Todos
              </button>
              <button 
                (click)="filterFavorites('active')" 
                [class.bg-pink-100]="currentFilter === 'active'"
                class="px-3 py-1 rounded-md text-sm font-medium text-pink-800 hover:bg-pink-100"
              >
                Activos
              </button>
              <button 
                (click)="filterFavorites('ended')" 
                [class.bg-pink-100]="currentFilter === 'ended'"
                class="px-3 py-1 rounded-md text-sm font-medium text-pink-800 hover:bg-pink-100"
              >
                Finalizados
              </button>
            </div>
            
            <div>
              <input
                type="text"
                placeholder="Buscar favoritos..."
                class="px-3 py-1 border border-gray-300 rounded-md text-sm"
                (input)="onSearch($event)"
              />
            </div>
          </div>
        </div>
        
        @if (loading) {
          <div class="p-8 text-center">
            <div class="inline-block h-8 w-8 animate-spin rounded-full border-4 border-solid border-pink-600 border-r-transparent"></div>
            <p class="mt-2 text-gray-600">Cargando tus favoritos...</p>
          </div>
        } @else if (filteredFavorites.length === 0) {
          <div class="p-8 text-center">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="mx-auto h-12 w-12 text-gray-400">
              <path stroke-linecap="round" stroke-linejoin="round" d="M21 8.25c0-2.485-2.099-4.5-4.688-4.5-1.935 0-3.597 1.126-4.312 2.733-.715-1.607-2.377-2.733-4.313-2.733C5.1 3.75 3 5.765 3 8.25c0 7.22 9 12 9 12s9-4.78 9-12Z" />
            </svg>
            <h3 class="mt-2 text-sm font-semibold text-gray-900">No tienes favoritos</h3>
            <p class="mt-1 text-sm text-gray-500">Agrega subastas a tus favoritos para seguirlas fácilmente.</p>
          </div>
        } @else {
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 p-6">
            @for (favorite of filteredFavorites; track favorite.id) {
              <div class="bg-white border border-gray-200 rounded-lg shadow-sm overflow-hidden hover:shadow-md transition-shadow">
                <div class="relative pb-[56.25%]">
                  <img 
                    [src]="favorite.imageUrl" 
                    [alt]="favorite.title"
                    class="absolute w-full h-full object-cover"
                  />
                  @if (!favorite.isActive) {
                    <div class="absolute inset-0 bg-black bg-opacity-60 flex items-center justify-center">
                      <span class="px-2 py-1 bg-red-500 text-white text-sm font-medium rounded">Finalizado</span>
                    </div>
                  }
                  <button 
                    (click)="removeFavorite(favorite.id)"
                    class="absolute top-2 right-2 w-8 h-8 bg-white bg-opacity-80 rounded-full flex items-center justify-center text-pink-600 hover:text-pink-800"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" fill="currentColor" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-5 h-5">
                      <path stroke-linecap="round" stroke-linejoin="round" d="M21 8.25c0-2.485-2.099-4.5-4.688-4.5-1.935 0-3.597 1.126-4.312 2.733-.715-1.607-2.377-2.733-4.313-2.733C5.1 3.75 3 5.765 3 8.25c0 7.22 9 12 9 12s9-4.78 9-12Z" />
                    </svg>
                  </button>
                </div>
                
                <div class="p-4">
                  <h3 class="text-lg font-semibold mb-2 line-clamp-2">{{ favorite.title }}</h3>
                  
                  <div class="flex justify-between items-baseline mb-1">
                    <span class="text-sm text-gray-500">Precio actual</span>
                    <span class="text-lg font-bold">\${{ favorite.currentPrice.toFixed(2) }}</span>
                  </div>
                  
                  <div class="flex justify-between items-baseline mb-3">
                    <span class="text-sm text-gray-500">{{ favorite.bids }} pujas</span>
                    <span class="text-sm text-gray-700">Termina: {{ getRelativeTime(favorite.endDate) }}</span>
                  </div>
                  
                  <div class="flex space-x-2">
                    <a 
                      [routerLink]="['/auction', favorite.auctionId]" 
                      class="flex-1 bg-blue-600 text-white text-center py-2 px-4 rounded-md text-sm font-medium hover:bg-blue-700 transition-colors"
                    >
                      Ver detalles
                    </a>
                    
                    @if (favorite.isActive) {
                      <button 
                        (click)="placeBid(favorite.auctionId)"
                        class="flex-1 bg-pink-600 text-white text-center py-2 px-4 rounded-md text-sm font-medium hover:bg-pink-700 transition-colors"
                      >
                        Pujar
                      </button>
                    }
                  </div>
                </div>
              </div>
            }
          </div>
        }
      </div>
    </div>
  `
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
