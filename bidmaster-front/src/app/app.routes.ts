import { Routes } from '@angular/router';
import { MainLayoutComponent } from './layouts/main-layout/main-layout.component';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    component: MainLayoutComponent,
    children: [
      {
        path: '',
        loadComponent: () => 
          import('./pages/home/home.component')
      },
      {
        path: 'profile',
        canActivate: [authGuard],
        loadComponent: () =>
          //import('./pages/profile/profile.component')
          import('./pages/profile/profile.component')
      },
      {
        path: 'my-bids',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./pages/my-bids/my-bids.component')
      }
    ]
  },
  {
    path: 'auth',
    children: [
      {
        path: 'login',
        loadComponent: () =>
          import('./pages/auth/login/login.component')
      },
      {
        path: 'register',
        loadComponent: () =>
          import('./pages/auth/register/register.component')
      },
      {
        path: '',
        redirectTo: 'login',
        pathMatch: 'full'
      }
    ]
  }
];
