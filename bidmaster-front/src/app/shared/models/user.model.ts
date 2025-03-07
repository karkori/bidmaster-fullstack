export interface User {
  id?: string;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  phone?: string;
  address?: {
    street?: string;
    city?: string;
    state?: string;
    zipCode?: string;
    country?: string;
  };
  status?: 'ACTIVE' | 'BLOCKED' | 'SUSPENDED' | 'BANNED';
  role?: 'USER' | 'ADMIN';
  balance?: number;
  reputation?: number;
}

export interface LoginCredentials {
  username: string;
  password: string;
}
