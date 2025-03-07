import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '@core/services/auth.service';

// Importar componentes compartidos
import EmptyStateComponent from '../../shared/components/empty-state/empty-state.component';
import FilterBarComponent from '../../shared/components/filter-bar/filter-bar.component';
import LoadingSpinnerComponent from '../../shared/components/loading-spinner/loading-spinner.component';
import PageHeaderComponent from '../../shared/components/page-header/page-header.component';
import StatusBadgeComponent from '../../shared/components/status-badge/status-badge.component';

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
  imports: [
    CommonModule,
    PageHeaderComponent,
    FilterBarComponent,
  ],
  templateUrl: './my-bids.component.html',
  styleUrls: []
})
export default class MyBidsComponent implements OnInit {
  bids: Bid[] = [];
  filteredBids: Bid[] = [];
  loading = true;
  currentFilter = 'all';
  searchTerm = '';
  
  // Usando inject() en lugar de inyección por constructor
  private authService = inject(AuthService);
  
  constructor() {}
  
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
  
  // Método para recibir el evento de búsqueda del componente FilterBar
  onSearchChange(value: string): void {
    this.searchTerm = value;
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
