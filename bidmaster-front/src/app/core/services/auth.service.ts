import { Injectable, signal } from '@angular/core';

export interface User {
  id: string;
  name: string;
  email: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly userSignal = signal<User | null>(null);
  
  isAuthenticated = signal(false);
  currentUser = this.userSignal.asReadonly();

  login(user: User) {
    this.userSignal.set(user);
    this.isAuthenticated.set(true);
  }

  logout() {
    this.userSignal.set(null);
    this.isAuthenticated.set(false);
  }
}
