import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

interface Message {
  id: string;
  senderId: string;
  senderName: string;
  senderAvatar: string;
  content: string;
  timestamp: Date;
  read: boolean;
}

@Component({
  selector: 'app-messages',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './messages.component.html'
})
export default class MessagesComponent implements OnInit {
  messages: Message[] = [];
  loading = true;
  selectedMessage: Message | null = null;

  ngOnInit(): void {
    // Simulación de carga de datos
    setTimeout(() => {
      this.messages = this.getMockMessages();
      this.loading = false;
    }, 1000);
  }

  selectMessage(message: Message): void {
    this.selectedMessage = message;
    // Marcar como leído
    const index = this.messages.findIndex(m => m.id === message.id);
    if (index !== -1 && !this.messages[index].read) {
      this.messages[index].read = true;
    }
  }

  deleteMessage(id: string): void {
    this.messages = this.messages.filter(m => m.id !== id);
    if (this.selectedMessage?.id === id) {
      this.selectedMessage = null;
    }
  }

  getUnreadCount(): number {
    return this.messages.filter(m => !m.read).length;
  }

  // Mock data for development
  private getMockMessages(): Message[] {
    return [
      {
        id: '1',
        senderId: 'user1',
        senderName: 'Ana García',
        senderAvatar: 'https://i.pravatar.cc/150?img=1',
        content: 'Hola, estoy interesada en tu subasta de la cámara vintage. ¿Podrías darme más detalles sobre su estado?',
        timestamp: new Date(new Date().getTime() - 24 * 60 * 60 * 1000), // 1 day ago
        read: false
      },
      {
        id: '2',
        senderId: 'user2',
        senderName: 'Carlos Rodríguez',
        senderAvatar: 'https://i.pravatar.cc/150?img=2',
        content: 'Ya realicé el pago por el artículo. ¿Cuándo podrías enviarlo?',
        timestamp: new Date(new Date().getTime() - 2 * 24 * 60 * 60 * 1000), // 2 days ago
        read: true
      },
      {
        id: '3',
        senderId: 'user3',
        senderName: 'Elena Martín',
        senderAvatar: 'https://i.pravatar.cc/150?img=3',
        content: 'Tengo una pregunta sobre la garantía del producto. ¿Ofreces algún tipo de garantía después de la compra?',
        timestamp: new Date(new Date().getTime() - 5 * 24 * 60 * 60 * 1000), // 5 days ago
        read: true
      },
      {
        id: '4',
        senderId: 'admin',
        senderName: 'Soporte BidMaster',
        senderAvatar: '/logo.svg',
        content: 'Bienvenido a BidMaster. Si tienes alguna pregunta o necesitas ayuda, no dudes en contactarnos.',
        timestamp: new Date(new Date().getTime() - 14 * 24 * 60 * 60 * 1000), // 14 days ago
        read: true
      }
    ];
  }
}
