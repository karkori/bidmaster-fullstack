import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '@core/services/auth.service';

interface Bid {
  id: string;
  auctionId: string;
  auctionTitle: string;
  amount: number;
  timestamp: Date;
  status: 'winning' | 'outbid' | 'won' | 'lost';
}

@Component({
  selector: 'app-my-bids',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="mx-auto">
      <div class="bg-white rounded-lg shadow-md overflow-hidden mb-6">
        <div class="p-6 bg-gradient-to-r from-blue-700 to-blue-900 text-white">
          <h1 class="text-2xl font-bold">Mis pujas</h1>
          <p class="opacity-80">Administra todas tus ofertas en subastas</p>
        </div>
        
        <div class="px-4 py-3 bg-blue-50 border-b border-blue-100">
          <div class="flex items-center justify-between">
            <div class="flex space-x-4">
              <button 
                (click)="filterBids('all')" 
                [class.bg-blue-100]="currentFilter === 'all'"
                class="px-3 py-1 rounded-md text-sm font-medium text-blue-800 hover:bg-blue-100"
              >
                Todas
              </button>
              <button 
                (click)="filterBids('active')" 
                [class.bg-blue-100]="currentFilter === 'active'"
                class="px-3 py-1 rounded-md text-sm font-medium text-blue-800 hover:bg-blue-100"
              >
                Activas
              </button>
              <button 
                (click)="filterBids('winning')" 
                [class.bg-blue-100]="currentFilter === 'winning'"
                class="px-3 py-1 rounded-md text-sm font-medium text-blue-800 hover:bg-blue-100"
              >
                Ganando
              </button>
              <button 
                (click)="filterBids('completed')" 
                [class.bg-blue-100]="currentFilter === 'completed'"
                class="px-3 py-1 rounded-md text-sm font-medium text-blue-800 hover:bg-blue-100"
              >
                Completadas
              </button>
            </div>
            
            <div>
              <input
                type="text"
                placeholder="Buscar subastas..."
                class="px-3 py-1 border border-gray-300 rounded-md text-sm"
                (input)="onSearch($event)"
              />
            </div>
          </div>
        </div>
        
        @if (loading) {
          <div class="p-8 text-center">
            <div class="inline-block h-8 w-8 animate-spin rounded-full border-4 border-solid border-blue-600 border-r-transparent"></div>
            <p class="mt-2 text-gray-600">Cargando tus pujas...</p>
          </div>
        } @else if (filteredBids.length === 0) {
          <div class="p-8 text-center">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="mx-auto h-12 w-12 text-gray-400">
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v3.75m9-.75a9 9 0 1 1-18 0 9 9 0 0 1 18 0Zm-9 3.75h.008v.008H12v-.008Z" />
            </svg>
            <h3 class="mt-2 text-sm font-semibold text-gray-900">No se encontraron pujas</h3>
            <p class="mt-1 text-sm text-gray-500">Aún no has realizado ninguna puja o no hay coincidencias con tu búsqueda.</p>
          </div>
        } @else {
          <div class="overflow-x-auto">
            <table class="min-w-full divide-y divide-gray-200">
              <thead class="bg-gray-50">
                <tr>
                  <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Subasta</th>
                  <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Monto</th>
                  <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Fecha</th>
                  <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Estado</th>
                  <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Acciones</th>
                </tr>
              </thead>
              <tbody class="bg-white divide-y divide-gray-200">
                @for (bid of filteredBids; track bid.id) {
                  <tr>
                    <td class="px-6 py-4 whitespace-nowrap">
                      <div class="text-sm font-medium text-gray-900">{{ bid.auctionTitle }}</div>
                      <div class="text-sm text-gray-500">ID: {{ bid.auctionId }}</div>
                    </td>
                    <td class="px-6 py-4 whitespace-nowrap">
                      <div class="text-sm font-medium text-gray-900">\${{ bid.amount.toFixed(2) }}</div>
                    </td>
                    <td class="px-6 py-4 whitespace-nowrap">
                      <div class="text-sm text-gray-900">{{ formatDate(bid.timestamp) }}</div>
                    </td>
                    <td class="px-6 py-4 whitespace-nowrap">
                      @if (bid.status === 'winning') {
                        <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                          Ganando
                        </span>
                      } @else if (bid.status === 'outbid') {
                        <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">
                          Superado
                        </span>
                      } @else if (bid.status === 'won') {
                        <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                          Ganado
                        </span>
                      } @else if (bid.status === 'lost') {
                        <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800">
                          Perdido
                        </span>
                      }
                    </td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
                      <button 
                        (click)="viewAuction(bid.auctionId)"
                        class="text-blue-600 hover:text-blue-900 mr-3"
                      >
                        Ver subasta
                      </button>
                      @if (bid.status === 'outbid') {
                        <button 
                          (click)="placeBid(bid.auctionId)"
                          class="text-green-600 hover:text-green-900"
                        >
                          Pujar de nuevo
                        </button>
                      }
                    </td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        }
      </div>
      
      <!-- Estadísticas -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
        <div class="bg-white p-6 rounded-lg shadow-md">
          <h3 class="text-lg font-semibold mb-2">Pujas activas</h3>
          <p class="text-3xl font-bold text-blue-600">{{ getActiveBidsCount() }}</p>
          <p class="text-sm text-gray-500 mt-1">Pujas en subastas actualmente en curso</p>
        </div>
        
        <div class="bg-white p-6 rounded-lg shadow-md">
          <h3 class="text-lg font-semibold mb-2">Pujas ganadas</h3>
          <p class="text-3xl font-bold text-green-600">{{ getWonBidsCount() }}</p>
          <p class="text-sm text-gray-500 mt-1">Subastas en las que has ganado</p>
        </div>
        
        <div class="bg-white p-6 rounded-lg shadow-md">
          <h3 class="text-lg font-semibold mb-2">Total invertido</h3>
          <p class="text-3xl font-bold text-indigo-600">\${{ getTotalBidAmount().toFixed(2) }}</p>
          <p class="text-sm text-gray-500 mt-1">Suma de todas tus pujas realizadas</p>
        </div>
      </div>
    </div>
  `
})
export default class MyBidsComponent implements OnInit {
  bids: Bid[] = [];
  filteredBids: Bid[] = [];
  loading = true;
  currentFilter = 'all';
  searchTerm = '';
  
  constructor(private authService: AuthService) {}
  
  ngOnInit(): void {
    // Simulación de carga de datos (en una aplicación real, esto sería un servicio)
    setTimeout(() => {
      this.bids = this.getMockBids();
      this.filteredBids = [...this.bids];
      this.loading = false;
    }, 1000);
  }
  
  filterBids(filter: string): void {
    this.currentFilter = filter;
    
    switch (filter) {
      case 'all':
        this.filteredBids = this.bids.filter(bid => 
          bid.auctionTitle.toLowerCase().includes(this.searchTerm.toLowerCase())
        );
        break;
      case 'active':
        this.filteredBids = this.bids.filter(bid => 
          (bid.status === 'winning' || bid.status === 'outbid') &&
          bid.auctionTitle.toLowerCase().includes(this.searchTerm.toLowerCase())
        );
        break;
      case 'winning':
        this.filteredBids = this.bids.filter(bid => 
          bid.status === 'winning' &&
          bid.auctionTitle.toLowerCase().includes(this.searchTerm.toLowerCase())
        );
        break;
      case 'completed':
        this.filteredBids = this.bids.filter(bid => 
          (bid.status === 'won' || bid.status === 'lost') &&
          bid.auctionTitle.toLowerCase().includes(this.searchTerm.toLowerCase())
        );
        break;
    }
  }
  
  onSearch(event: Event): void {
    this.searchTerm = (event.target as HTMLInputElement).value;
    this.filterBids(this.currentFilter);
  }
  
  formatDate(date: Date): string {
    return date.toLocaleDateString() + ' ' + date.toLocaleTimeString();
  }
  
  viewAuction(auctionId: string): void {
    console.log('Ver subasta:', auctionId);
    // Aquí iría la navegación a la página de la subasta
  }
  
  placeBid(auctionId: string): void {
    console.log('Pujar de nuevo en la subasta:', auctionId);
    // Aquí iría la lógica para realizar una nueva puja
  }
  
  getActiveBidsCount(): number {
    return this.bids.filter(bid => bid.status === 'winning' || bid.status === 'outbid').length;
  }
  
  getWonBidsCount(): number {
    return this.bids.filter(bid => bid.status === 'won').length;
  }
  
  getTotalBidAmount(): number {
    return this.bids.reduce((total, bid) => total + bid.amount, 0);
  }
  
  // Datos de ejemplo para desarrollo
  private getMockBids(): Bid[] {
    return [
      {
        id: '1',
        auctionId: 'a1',
        auctionTitle: 'Smartphone Samsung Galaxy S21',
        amount: 650.00,
        timestamp: new Date(2025, 2, 5, 14, 30),
        status: 'winning'
      },
      {
        id: '2',
        auctionId: 'a2',
        auctionTitle: 'Laptop MacBook Pro 2024',
        amount: 1200.00,
        timestamp: new Date(2025, 2, 4, 10, 15),
        status: 'outbid'
      },
      {
        id: '3',
        auctionId: 'a3',
        auctionTitle: 'Cámara Sony Alpha A7 III',
        amount: 1800.00,
        timestamp: new Date(2025, 2, 3, 9, 45),
        status: 'won'
      },
      {
        id: '4',
        auctionId: 'a4',
        auctionTitle: 'Consola PlayStation 5',
        amount: 450.00,
        timestamp: new Date(2025, 2, 1, 16, 20),
        status: 'lost'
      },
      {
        id: '5',
        auctionId: 'a5',
        auctionTitle: 'Smart TV LG OLED 65"',
        amount: 1100.00,
        timestamp: new Date(2025, 1, 28, 12, 10),
        status: 'won'
      }
    ];
  }
}
