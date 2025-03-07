import { Routes } from '@angular/router';
import { MainLayoutComponent } from './layouts/main-layout/main-layout.component';
import { DashboardLayoutComponent } from './layouts/dashboard-layout/dashboard-layout.component';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    component: MainLayoutComponent,
    children: [
      {
        path: '',
        loadComponent: () => import('./pages/home/home.component')
      },
      {
        path: 'dashboard',
        component: DashboardLayoutComponent,
        canActivate: [authGuard],
        children: [
          // Rutas principales de usuario
          {
            path: 'profile',
            loadComponent: () => import('./pages/dashboard/profile/profile.component')
          },
          {
            path: 'my-bids',
            loadComponent: () => import('./pages/dashboard/my-bids/my-bids.component')
          },
          {
            path: 'favorites',
            loadComponent: () => import('./pages/dashboard/favorites/favorites.component')
          },
          {
            path: 'messages',
            loadComponent: () => import('./pages/dashboard/messages/messages.component')
          },
          {
            path: 'notifications',
            loadComponent: () => import('./pages/dashboard/notifications/notifications.component')
          },
          {
            path: 'create-auction',
            loadComponent: () => import('./pages/dashboard/create-auction/create-auction.component')
          },
          
          // Rutas de administración
          {
            path: 'admin',
            children: [
              {
                path: 'users',
                loadComponent: () => import('./pages/dashboard/admin/users/users.component')
              },
              {
                path: 'auctions',
                redirectTo: '/dashboard/profile' // change to loadComponent when component is created 
              }
            ]
          },
          
          // Redirección por defecto a perfil
          {
            path: '',
            redirectTo: 'profile',
            pathMatch: 'full'
          }
        ]
      },
      {
        path: 'auth',
        children: [
          {
            path: 'login',
            loadComponent: () => import('./pages/auth/login/login.component')
          },
          {
            path: 'register',
            loadComponent: () => import('./pages/auth/register/register.component')
          },
          {
            path: '',
            redirectTo: 'login',
            pathMatch: 'full'
          }
        ]
      },
      // Redirección para las rutas antiguas (compatibilidad)
      { path: 'profile', redirectTo: 'dashboard/profile', pathMatch: 'full' },
      { path: 'my-bids', redirectTo: 'dashboard/my-bids', pathMatch: 'full' }
    ]
  },
  // Ruta wildcard para 404
  { path: '**', redirectTo: '', pathMatch: 'full' }
];
