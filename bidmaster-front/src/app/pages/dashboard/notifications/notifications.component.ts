import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

interface Notification {
  id: string;
  type: 'info' | 'success' | 'warning' | 'error';
  title: string;
  message: string;
  timestamp: Date;
  read: boolean;
}

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notifications.component.html'
})
export default class NotificationsComponent implements OnInit {
  notifications: Notification[] = [];
  filteredNotifications: Notification[] = [];
  loading = true;
  currentFilter = 'all';

  ngOnInit(): void {
    // Simulación de carga de datos
    setTimeout(() => {
      this.notifications = this.getMockNotifications();
      this.filteredNotifications = [...this.notifications];
      this.loading = false;
    }, 1000);
  }
  
  filterNotifications(filter: string): void {
    this.currentFilter = filter;
    
    switch (filter) {
      case 'all':
        this.filteredNotifications = [...this.notifications];
        break;
      case 'unread':
        this.filteredNotifications = this.notifications.filter(n => !n.read);
        break;
      case 'read':
        this.filteredNotifications = this.notifications.filter(n => n.read);
        break;
    }
  }
  
  markAsRead(id: string): void {
    const index = this.notifications.findIndex(n => n.id === id);
    if (index !== -1) {
      this.notifications[index].read = true;
      // También actualizar en la lista filtrada
      const filteredIndex = this.filteredNotifications.findIndex(n => n.id === id);
      if (filteredIndex !== -1) {
        this.filteredNotifications[filteredIndex].read = true;
      }
    }
  }
  
  markAllAsRead(): void {
    this.notifications.forEach(n => n.read = true);
    this.filteredNotifications.forEach(n => n.read = true);
  }
  
  deleteNotification(id: string): void {
    this.notifications = this.notifications.filter(n => n.id !== id);
    this.filteredNotifications = this.filteredNotifications.filter(n => n.id !== id);
  }
  
  getUnreadCount(): number {
    return this.notifications.filter(n => !n.read).length;
  }
  
  getIconClass(type: string): string {
    switch (type) {
      case 'info': return 'text-blue-500';
      case 'success': return 'text-green-500';
      case 'warning': return 'text-yellow-500';
      case 'error': return 'text-red-500';
      default: return 'text-gray-500';
    }
  }
  
  getTimeAgo(date: Date): string {
    const now = new Date();
    const diffInSeconds = Math.floor((now.getTime() - date.getTime()) / 1000);
    
    if (diffInSeconds < 60) {
      return 'hace unos segundos';
    }
    
    const diffInMinutes = Math.floor(diffInSeconds / 60);
    if (diffInMinutes < 60) {
      return `hace ${diffInMinutes} minuto${diffInMinutes !== 1 ? 's' : ''}`;
    }
    
    const diffInHours = Math.floor(diffInMinutes / 60);
    if (diffInHours < 24) {
      return `hace ${diffInHours} hora${diffInHours !== 1 ? 's' : ''}`;
    }
    
    const diffInDays = Math.floor(diffInHours / 24);
    if (diffInDays < 30) {
      return `hace ${diffInDays} día${diffInDays !== 1 ? 's' : ''}`;
    }
    
    const diffInMonths = Math.floor(diffInDays / 30);
    return `hace ${diffInMonths} mes${diffInMonths !== 1 ? 'es' : ''}`;
  }
  
  // Mock data for development
  private getMockNotifications(): Notification[] {
    return [
      {
        id: '1',
        type: 'success',
        title: 'Subasta ganada',
        message: '¡Felicidades! Has ganado la subasta "Cámara vintage Polaroid SX-70".',
        timestamp: new Date(new Date().getTime() - 2 * 60 * 60 * 1000), // 2 hours ago
        read: false
      },
      {
        id: '2',
        type: 'info',
        title: 'Nueva puja en tu subasta',
        message: 'Se ha realizado una nueva puja de $150 en tu subasta "Colección de vinilos años 80".',
        timestamp: new Date(new Date().getTime() - 5 * 60 * 60 * 1000), // 5 hours ago
        read: false
      },
      {
        id: '3',
        type: 'warning',
        title: 'Subasta a punto de finalizar',
        message: 'Tu subasta "Consola Nintendo Switch" finaliza en menos de 1 hora.',
        timestamp: new Date(new Date().getTime() - 12 * 60 * 60 * 1000), // 12 hours ago
        read: true
      },
      {
        id: '4',
        type: 'error',
        title: 'Pago rechazado',
        message: 'El pago para la subasta "Zapatillas deportivas" ha sido rechazado. Por favor, intenta con otro método de pago.',
        timestamp: new Date(new Date().getTime() - 24 * 60 * 60 * 1000), // 1 day ago
        read: true
      },
      {
        id: '5',
        type: 'info',
        title: 'Nuevo mensaje',
        message: 'Has recibido un nuevo mensaje de Juan García sobre tu subasta "Bicicleta de montaña".',
        timestamp: new Date(new Date().getTime() - 2 * 24 * 60 * 60 * 1000), // 2 days ago
        read: true
      }
    ];
  }
}
