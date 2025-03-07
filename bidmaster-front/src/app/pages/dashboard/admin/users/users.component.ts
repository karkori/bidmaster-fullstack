import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';

interface User {
  id: string;
  username: string;
  email: string;
  fullName: string;
  role: 'USER' | 'ADMIN' | 'MODERATOR';
  status: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED';
  createdAt: Date;
  lastLogin: Date | null;
  avatarUrl: string;
}

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './users.component.html'
})
export default class UsersComponent implements OnInit {
  users = signal<User[]>([]);
  filteredUsers = signal<User[]>([]);
  loading = signal(true);
  searchTerm = signal('');
  statusFilter = signal<string>('all');
  roleFilter = signal<string>('all');
  
  currentPage = signal(1);
  itemsPerPage = 10;
  
  selectedUser = signal<User | null>(null);
  
  ngOnInit(): void {
    // Simulación de carga de datos
    setTimeout(() => {
      const mockUsers = this.getMockUsers();
      this.users.set(mockUsers);
      this.filteredUsers.set(mockUsers);
      this.loading.set(false);
    }, 1000);
  }
  
  get totalPages(): number {
    return Math.ceil(this.filteredUsers().length / this.itemsPerPage);
  }
  
  get paginatedUsers(): User[] {
    const startIndex = (this.currentPage() - 1) * this.itemsPerPage;
    const endIndex = startIndex + this.itemsPerPage;
    return this.filteredUsers().slice(startIndex, endIndex);
  }
  
  searchUsers(event: Event): void {
    const value = (event.target as HTMLInputElement).value.toLowerCase();
    this.searchTerm.set(value);
    this.applyFilters();
  }
  
  setStatusFilter(status: string): void {
    this.statusFilter.set(status);
    this.applyFilters();
  }
  
  setRoleFilter(role: string): void {
    this.roleFilter.set(role);
    this.applyFilters();
  }
  
  applyFilters(): void {
    let result = this.users();
    
    // Aplicar filtro de búsqueda
    if (this.searchTerm()) {
      result = result.filter(user => 
        user.username.toLowerCase().includes(this.searchTerm()) ||
        user.email.toLowerCase().includes(this.searchTerm()) ||
        user.fullName.toLowerCase().includes(this.searchTerm())
      );
    }
    
    // Aplicar filtro de estado
    if (this.statusFilter() !== 'all') {
      result = result.filter(user => user.status.toLowerCase() === this.statusFilter());
    }
    
    // Aplicar filtro de rol
    if (this.roleFilter() !== 'all') {
      result = result.filter(user => user.role.toLowerCase() === this.roleFilter());
    }
    
    this.filteredUsers.set(result);
    this.currentPage.set(1); // Resetear a la primera página
  }
  
  setPage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage.set(page);
    }
  }
  
  openUserDetails(user: User): void {
    this.selectedUser.set(user);
  }
  
  closeUserDetails(): void {
    this.selectedUser.set(null);
  }
  
  changeUserStatus(userId: string, newStatus: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED'): void {
    // Update users array
    const updatedUsers = this.users().map(user => {
      if (user.id === userId) {
        return { ...user, status: newStatus };
      }
      return user;
    });
    
    this.users.set(updatedUsers);
    this.applyFilters();
    
    // Update selected user if it's the one being modified
    if (this.selectedUser()?.id === userId) {
      this.selectedUser.set({
        ...this.selectedUser()!,
        status: newStatus
      });
    }
  }
  
  // Mock data for development
  private getMockUsers(): User[] {
    return [
      {
        id: '1',
        username: 'admin',
        email: 'admin@bidmaster.com',
        fullName: 'Admin User',
        role: 'ADMIN',
        status: 'ACTIVE',
        createdAt: new Date(2024, 0, 15),
        lastLogin: new Date(2025, 2, 6),
        avatarUrl: 'https://i.pravatar.cc/150?img=1'
      },
      {
        id: '2',
        username: 'johndoe',
        email: 'john.doe@example.com',
        fullName: 'John Doe',
        role: 'USER',
        status: 'ACTIVE',
        createdAt: new Date(2024, 1, 5),
        lastLogin: new Date(2025, 2, 5),
        avatarUrl: 'https://i.pravatar.cc/150?img=2'
      },
      {
        id: '3',
        username: 'janesmith',
        email: 'jane.smith@example.com',
        fullName: 'Jane Smith',
        role: 'USER',
        status: 'ACTIVE',
        createdAt: new Date(2024, 1, 10),
        lastLogin: new Date(2025, 2, 1),
        avatarUrl: 'https://i.pravatar.cc/150?img=3'
      },
      {
        id: '4',
        username: 'moderator',
        email: 'moderator@bidmaster.com',
        fullName: 'Mod User',
        role: 'MODERATOR',
        status: 'ACTIVE',
        createdAt: new Date(2024, 0, 20),
        lastLogin: new Date(2025, 2, 4),
        avatarUrl: 'https://i.pravatar.cc/150?img=4'
      },
      {
        id: '5',
        username: 'suspended_user',
        email: 'suspended@example.com',
        fullName: 'Suspended User',
        role: 'USER',
        status: 'SUSPENDED',
        createdAt: new Date(2024, 1, 15),
        lastLogin: new Date(2024, 2, 20),
        avatarUrl: 'https://i.pravatar.cc/150?img=5'
      },
      {
        id: '6',
        username: 'inactive_user',
        email: 'inactive@example.com',
        fullName: 'Inactive User',
        role: 'USER',
        status: 'INACTIVE',
        createdAt: new Date(2024, 1, 25),
        lastLogin: null,
        avatarUrl: 'https://i.pravatar.cc/150?img=6'
      },
      {
        id: '7',
        username: 'marketingseller',
        email: 'seller1@example.com',
        fullName: 'Marketing Seller',
        role: 'USER',
        status: 'ACTIVE',
        createdAt: new Date(2024, 2, 1),
        lastLogin: new Date(2025, 2, 2),
        avatarUrl: 'https://i.pravatar.cc/150?img=7'
      },
      {
        id: '8',
        username: 'premiummember',
        email: 'premium@example.com',
        fullName: 'Premium Member',
        role: 'USER',
        status: 'ACTIVE',
        createdAt: new Date(2024, 0, 10),
        lastLogin: new Date(2025, 2, 3),
        avatarUrl: 'https://i.pravatar.cc/150?img=8'
      },
      {
        id: '9',
        username: 'newuser',
        email: 'new@example.com',
        fullName: 'New User',
        role: 'USER',
        status: 'ACTIVE',
        createdAt: new Date(2025, 2, 1),
        lastLogin: new Date(2025, 2, 1),
        avatarUrl: 'https://i.pravatar.cc/150?img=9'
      },
      {
        id: '10',
        username: 'inactiveadmin',
        email: 'inactive.admin@bidmaster.com',
        fullName: 'Inactive Admin',
        role: 'ADMIN',
        status: 'INACTIVE',
        createdAt: new Date(2024, 0, 5),
        lastLogin: new Date(2024, 5, 10),
        avatarUrl: 'https://i.pravatar.cc/150?img=10'
      },
      {
        id: '11',
        username: 'tempuser',
        email: 'temp@example.com',
        fullName: 'Temporary User',
        role: 'USER',
        status: 'ACTIVE',
        createdAt: new Date(2024, 3, 15),
        lastLogin: new Date(2024, 4, 20),
        avatarUrl: 'https://i.pravatar.cc/150?img=11'
      },
      {
        id: '12',
        username: 'supportmod',
        email: 'support@bidmaster.com',
        fullName: 'Support Moderator',
        role: 'MODERATOR',
        status: 'ACTIVE',
        createdAt: new Date(2024, 1, 1),
        lastLogin: new Date(2025, 2, 5),
        avatarUrl: 'https://i.pravatar.cc/150?img=12'
      }
    ];
  }
}
