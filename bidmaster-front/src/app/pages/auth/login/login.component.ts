import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { z } from 'zod';
import { loginSchema, LoginFormType } from '@shared/validators/auth.schemas';
import { AuthService } from '@core/services/auth.service';

@Component({
  selector: 'app-login',
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  template: `
    <div class="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div class="max-w-md w-full space-y-8">
        <div>
          <h2 class="mt-6 text-center text-3xl font-extrabold text-gray-900">
            Inicia sesión en tu cuenta
          </h2>
          <p class="mt-2 text-center text-sm text-gray-600">
            ¿No tienes una cuenta?
            <a routerLink="/auth/register" class="font-medium text-indigo-600 hover:text-indigo-500">
              Regístrate aquí
            </a>
          </p>
        </div>
        
        @if (errorMessage) {
          <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative" role="alert">
            <span class="block sm:inline">{{ errorMessage }}</span>
          </div>
        }
        
        <form class="mt-8 space-y-6" [formGroup]="loginForm" (ngSubmit)="onSubmit()">
          <div class="rounded-md shadow-sm -space-y-px">
            <div>
              <label for="username" class="sr-only">Nombre de usuario</label>
              <input id="username" name="username" type="text" formControlName="username" required 
                class="appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-t-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 focus:z-10 sm:text-sm" 
                placeholder="Nombre de usuario">
              @if (loginForm.get('username')?.invalid && (loginForm.get('username')?.dirty || loginForm.get('username')?.touched)) {
                <p class="mt-2 text-sm text-red-600">{{ getErrorMessage('username') }}</p>
              }
            </div>
            <div>
              <label for="password" class="sr-only">Contraseña</label>
              <input id="password" name="password" type="password" formControlName="password" required 
                class="appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-b-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 focus:z-10 sm:text-sm" 
                placeholder="Contraseña">
              @if (loginForm.get('password')?.invalid && (loginForm.get('password')?.dirty || loginForm.get('password')?.touched)) {
                <p class="mt-2 text-sm text-red-600">{{ getErrorMessage('password') }}</p>
              }
            </div>
          </div>

          <div class="flex items-center justify-between">
            <div class="flex items-center">
              <input id="remember-me" name="remember-me" type="checkbox" 
                class="h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded">
              <label for="remember-me" class="ml-2 block text-sm text-gray-900">
                Recordarme
              </label>
            </div>

            <div class="text-sm">
              <a href="#" class="font-medium text-indigo-600 hover:text-indigo-500">
                ¿Olvidaste tu contraseña?
              </a>
            </div>
          </div>

          <div>
            <button type="submit" [disabled]="loading || loginForm.invalid"
              class="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:bg-indigo-300">
              @if (loading) {
                <span>Cargando...</span>
              } @else {
                <span>Iniciar sesión</span>
              }
            </button>
          </div>
        </form>
      </div>
    </div>
  `,
  styleUrls: []
})
export default class LoginComponent {
  loginForm: FormGroup;
  loading = false;
  errorMessage = '';
  
  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50)]],
      password: ['', [Validators.required, Validators.minLength(8)]]
    });
  }
  
  onSubmit() {
    if (this.loginForm.invalid) {
      this.validateFormWithZod();
      return;
    }
    
    this.loading = true;
    this.errorMessage = '';
    
    const credentials = this.loginForm.value as LoginFormType;
    
    this.authService.login(credentials).subscribe({
      next: () => {
        this.router.navigate(['/']);
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = error.error?.message || 'Error al iniciar sesión. Por favor, inténtalo de nuevo.';
      }
    });
  }
  
  validateFormWithZod() {
    try {
      loginSchema.parse(this.loginForm.value);
    } catch (error) {
      if (error instanceof z.ZodError) {
        error.errors.forEach(err => {
          const control = this.loginForm.get(err.path[0] as string);
          if (control) {
            control.setErrors({ zod: err.message });
          }
        });
      }
    }
  }
  
  getErrorMessage(controlName: string): string {
    const control = this.loginForm.get(controlName);
    if (control?.errors) {
      if (control.errors['zod']) {
        return control.errors['zod'];
      }
      if (control.errors['required']) {
        return 'Este campo es obligatorio';
      }
      if (control.errors['minlength']) {
        return `Mínimo ${control.errors['minlength'].requiredLength} caracteres`;
      }
      if (control.errors['maxlength']) {
        return `Máximo ${control.errors['maxlength'].requiredLength} caracteres`;
      }
    }
    return '';
  }
}
