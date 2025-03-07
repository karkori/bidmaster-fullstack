import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { User } from '@shared/models/user.model';
import { AuthService } from '@core/services/auth.service';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="mx-auto">
      <div class="bg-white rounded-lg shadow-md overflow-hidden">
        <div class="p-6 bg-gradient-to-r from-blue-700 to-blue-900 text-white">
          <h1 class="text-2xl font-bold">Mi perfil</h1>
          <p class="opacity-80">Gestiona tu información personal</p>
        </div>
        
        <div class="p-6">
          <div class="flex flex-col sm:flex-row items-start gap-6 mb-8">
            <div class="flex-shrink-0">
              <div class="w-32 h-32 rounded-full bg-blue-100 flex items-center justify-center text-3xl font-semibold text-blue-800">
                {{ getUserInitials() }}
              </div>
            </div>
            
            <div class="flex-1">
              <h2 class="text-xl font-semibold mb-2">{{ currentUser?.username }}</h2>
              <p class="text-gray-600 mb-1">{{ currentUser?.email }}</p>
              <p class="text-gray-600 mb-1">{{ currentUser?.phone || 'No phone' }}</p>
              <div class="mt-3 flex items-center">
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                  {{ currentUser?.role }}
                </span>
                <span class="ml-2 inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                  Reputación: {{ currentUser?.reputation || 0 }}
                </span>
              </div>
            </div>
          </div>
          
          <form [formGroup]="profileForm" (ngSubmit)="onSubmit()" class="space-y-6">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label for="firstName" class="block text-sm font-medium text-gray-700">Nombre</label>
                <input 
                  type="text" 
                  id="firstName" 
                  formControlName="firstName"
                  class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500" 
                />
              </div>
              
              <div>
                <label for="lastName" class="block text-sm font-medium text-gray-700">Apellido</label>
                <input 
                  type="text" 
                  id="lastName" 
                  formControlName="lastName"
                  class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500" 
                />
              </div>
              
              <div>
                <label for="phone" class="block text-sm font-medium text-gray-700">Teléfono</label>
                <input 
                  type="text" 
                  id="phone" 
                  formControlName="phone"
                  class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500" 
                />
              </div>
              
              <div>
                <label for="address" class="block text-sm font-medium text-gray-700">Dirección</label>
                <input 
                  type="text" 
                  id="address" 
                  formControlName="address"
                  class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500" 
                />
              </div>
            </div>
            
            <div>
              <h3 class="text-lg font-medium mb-2">Información de cuenta</h3>
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label for="username" class="block text-sm font-medium text-gray-700">Nombre de usuario</label>
                  <input 
                    type="text" 
                    id="username" 
                    formControlName="username"
                    class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500" 
                  />
                </div>
                
                <div>
                  <label for="email" class="block text-sm font-medium text-gray-700">Email</label>
                  <input 
                    type="email" 
                    id="email" 
                    formControlName="email"
                    class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500" 
                  />
                </div>
              </div>
            </div>
            
            <div class="pt-4 border-t border-gray-200">
              <div class="flex justify-end">
                <button
                  type="submit"
                  [disabled]="profileForm.invalid || !profileForm.dirty"
                  class="rounded-md bg-blue-600 px-4 py-2 text-sm font-semibold text-white shadow-sm hover:bg-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-600 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  Guardar cambios
                </button>
              </div>
            </div>
          </form>
        </div>
      </div>
      
      <div class="mt-8 bg-white rounded-lg shadow-md overflow-hidden">
        <div class="p-6 border-b border-gray-200">
          <h2 class="text-xl font-semibold">Cambiar contraseña</h2>
        </div>
        
        <div class="p-6">
          <form [formGroup]="passwordForm" (ngSubmit)="onPasswordChange()" class="space-y-6">
            <div class="grid grid-cols-1 gap-6">
              <div>
                <label for="currentPassword" class="block text-sm font-medium text-gray-700">Contraseña actual</label>
                <input 
                  type="password" 
                  id="currentPassword" 
                  formControlName="currentPassword"
                  class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500" 
                />
              </div>
              
              <div>
                <label for="newPassword" class="block text-sm font-medium text-gray-700">Nueva contraseña</label>
                <input 
                  type="password" 
                  id="newPassword" 
                  formControlName="newPassword"
                  class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500" 
                />
              </div>
              
              <div>
                <label for="confirmPassword" class="block text-sm font-medium text-gray-700">Confirmar nueva contraseña</label>
                <input 
                  type="password" 
                  id="confirmPassword" 
                  formControlName="confirmPassword"
                  class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500" 
                />
              </div>
            </div>
            
            <div class="flex justify-end">
              <button
                type="submit"
                [disabled]="passwordForm.invalid"
                class="rounded-md bg-blue-600 px-4 py-2 text-sm font-semibold text-white shadow-sm hover:bg-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-600 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Actualizar contraseña
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  `
})
export default class ProfileComponent implements OnInit {
  currentUser: User | null = null;
  profileForm!: FormGroup;
  passwordForm!: FormGroup;
  
  constructor(
    private authService: AuthService,
    private fb: FormBuilder
  ) {}
  
  ngOnInit(): void {
    this.currentUser = this.authService.currentUserValue;
    this.initForms();
  }
  
  initForms(): void {
    this.profileForm = this.fb.group({
      firstName: [this.currentUser?.firstName || '', Validators.required],
      lastName: [this.currentUser?.lastName || '', Validators.required],
      username: [this.currentUser?.username || '', Validators.required],
      email: [this.currentUser?.email || '', [Validators.required, Validators.email]],
      phone: [this.currentUser?.phone || ''],
      address: [this.currentUser?.address || '']
    });
    
    this.passwordForm = this.fb.group({
      currentPassword: ['', Validators.required],
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required]
    }, { validators: this.passwordMatchValidator });
  }
  
  passwordMatchValidator(form: FormGroup): { [key: string]: boolean } | null {
    const newPassword = form.get('newPassword')?.value;
    const confirmPassword = form.get('confirmPassword')?.value;
    
    if (newPassword !== confirmPassword) {
      return { 'passwordsDoNotMatch': true };
    }
    
    return null;
  }
  
  getUserInitials(): string {
    if (!this.currentUser) return '';
    
    const firstName = this.currentUser.firstName || '';
    const lastName = this.currentUser.lastName || '';
    
    if (firstName && lastName) {
      return `${firstName.charAt(0)}${lastName.charAt(0)}`.toUpperCase();
    } else if (firstName) {
      return firstName.charAt(0).toUpperCase();
    } else if (this.currentUser.username) {
      return this.currentUser.username.charAt(0).toUpperCase();
    }
    
    return '';
  }
  
  onSubmit(): void {
    if (this.profileForm.valid && this.profileForm.dirty) {
      // Aquí iría la lógica para actualizar el perfil
      console.log('Datos del perfil a actualizar:', this.profileForm.value);
    }
  }
  
  onPasswordChange(): void {
    if (this.passwordForm.valid) {
      // Aquí iría la lógica para cambiar la contraseña
      console.log('Cambio de contraseña:', this.passwordForm.value);
    }
  }
}
