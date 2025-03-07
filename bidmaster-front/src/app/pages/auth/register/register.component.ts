import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '@core/services/auth.service';
import { registerSchema } from '@shared/validators/auth.schemas';
import { z } from 'zod';

@Component({
  selector: 'app-register',
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  template: `
    <div class="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div class="max-w-md w-full space-y-8">
        <div>
          <h2 class="mt-6 text-center text-3xl font-extrabold text-gray-900">
            Crea tu cuenta
          </h2>
          <p class="mt-2 text-center text-sm text-gray-600">
            ¿Ya tienes una cuenta?
            <a routerLink="/auth/login" class="font-medium text-indigo-600 hover:text-indigo-500">
              Inicia sesión aquí
            </a>
          </p>
        </div>
        
        @if (errorMessage) {
          <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative" role="alert">
            <span class="block sm:inline">{{ errorMessage }}</span>
          </div>
        }
        
        @if (successMessage) {
          <div class="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded relative" role="alert">
            <span class="block sm:inline">{{ successMessage }}</span>
          </div>
        }
        
        <form class="mt-8 space-y-6" [formGroup]="registerForm" (ngSubmit)="onSubmit()">
          <!-- Información de cuenta -->
          <div class="rounded-md shadow-sm -space-y-px">
            <div>
              <label for="username" class="sr-only">Nombre de usuario</label>
              <input id="username" name="username" type="text" formControlName="username" required 
                class="appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-t-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 focus:z-10 sm:text-sm" 
                placeholder="Nombre de usuario">
              @if (registerForm.get('username')?.invalid && (registerForm.get('username')?.dirty || registerForm.get('username')?.touched)) {
                <p class="mt-2 text-sm text-red-600">{{ getErrorMessage('username') }}</p>
              }
            </div>
            <div>
              <label for="email" class="sr-only">Correo electrónico</label>
              <input id="email" name="email" type="email" formControlName="email" required 
                class="appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 focus:z-10 sm:text-sm" 
                placeholder="Correo electrónico">
              @if (registerForm.get('email')?.invalid && (registerForm.get('email')?.dirty || registerForm.get('email')?.touched)) {
                <p class="mt-2 text-sm text-red-600">{{ getErrorMessage('email') }}</p>
              }
            </div>
            <div>
              <label for="password" class="sr-only">Contraseña</label>
              <input id="password" name="password" type="password" formControlName="password" required 
                class="appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 focus:z-10 sm:text-sm" 
                placeholder="Contraseña">
              @if (registerForm.get('password')?.invalid && (registerForm.get('password')?.dirty || registerForm.get('password')?.touched)) {
                <p class="mt-2 text-sm text-red-600">{{ getErrorMessage('password') }}</p>
              }
            </div>
            <div>
              <label for="confirmPassword" class="sr-only">Confirmar contraseña</label>
              <input id="confirmPassword" name="confirmPassword" type="password" formControlName="confirmPassword" required 
                class="appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-b-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 focus:z-10 sm:text-sm" 
                placeholder="Confirmar contraseña">
              @if (registerForm.get('confirmPassword')?.invalid && (registerForm.get('confirmPassword')?.dirty || registerForm.get('confirmPassword')?.touched)) {
                <p class="mt-2 text-sm text-red-600">{{ getErrorMessage('confirmPassword') }}</p>
              }
              @if (registerForm.errors?.['passwordMismatch']) {
                <p class="mt-2 text-sm text-red-600">Las contraseñas no coinciden</p>
              }
            </div>
          </div>
          
          <!-- Información personal (opcional) -->
          <div class="space-y-4 mt-4">
            <h3 class="text-lg font-medium text-gray-900">Información personal (opcional)</h3>
            <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <div>
                <label for="firstName" class="block text-sm font-medium text-gray-700">Nombre</label>
                <input id="firstName" name="firstName" type="text" formControlName="firstName" 
                  class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm" 
                  placeholder="Nombre">
              </div>
              <div>
                <label for="lastName" class="block text-sm font-medium text-gray-700">Apellidos</label>
                <input id="lastName" name="lastName" type="text" formControlName="lastName" 
                  class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm" 
                  placeholder="Apellidos">
              </div>
            </div>
            <div>
              <label for="phone" class="block text-sm font-medium text-gray-700">Teléfono</label>
              <input id="phone" name="phone" type="tel" formControlName="phone" 
                class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm" 
                placeholder="Teléfono">
            </div>
          </div>
          
          <div class="flex items-center">
            <input id="termsAccepted" name="termsAccepted" type="checkbox" formControlName="termsAccepted"
              class="h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded">
            <label for="termsAccepted" class="ml-2 block text-sm text-gray-900">
              Acepto los términos y condiciones
            </label>
          </div>
          @if (registerForm.get('termsAccepted')?.invalid && (registerForm.get('termsAccepted')?.dirty || registerForm.get('termsAccepted')?.touched)) {
            <p class="mt-2 text-sm text-red-600">{{ getErrorMessage('termsAccepted') }}</p>
          }

          <div>
            <button type="submit" [disabled]="loading || registerForm.invalid"
              class="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:bg-indigo-300">
              @if (loading) {
                <span>Creando cuenta...</span>
              } @else {
                <span>Registrarse</span>
              }
            </button>
          </div>
        </form>
      </div>
    </div>
  `,
  styleUrls: []
})
export default class RegisterComponent {
  registerForm: FormGroup;
  loading = false;
  errorMessage = '';
  successMessage = '';
  
  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.registerForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [
        Validators.required, 
        Validators.minLength(8),
        Validators.pattern(/^(?=.*[A-Z])(?=.*[0-9]).*$/)
      ]],
      confirmPassword: ['', [Validators.required]],
      firstName: [''],
      lastName: [''],
      phone: [''],
      termsAccepted: [false, [Validators.requiredTrue]]
    }, {
      validators: this.passwordMatchValidator
    });
  }
  
  passwordMatchValidator(formGroup: FormGroup) {
    const password = formGroup.get('password')?.value;
    const confirmPassword = formGroup.get('confirmPassword')?.value;
    
    if (password !== confirmPassword) {
      formGroup.get('confirmPassword')?.setErrors({ passwordMismatch: true });
      return { passwordMismatch: true };
    }
    
    return null;
  }
  
  onSubmit() {
    if (this.registerForm.invalid) {
      this.validateFormWithZod();
      return;
    }
    
    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';
    
    // Extraemos los datos del formulario excluyendo confirmPassword y termsAccepted
    const { confirmPassword, termsAccepted, ...userData } = this.registerForm.value;
    
    this.authService.register(userData).subscribe({
      next: () => {
        this.loading = false;
        this.successMessage = '¡Cuenta creada con éxito! Ahora puedes iniciar sesión.';
        setTimeout(() => {
          this.router.navigate(['/auth/login']);
        }, 3000);
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = error.error?.message || 'Error al crear la cuenta. Por favor, inténtalo de nuevo.';
      }
    });
  }
  
  validateFormWithZod() {
    try {
      registerSchema.parse(this.registerForm.value);
    } catch (error) {
      if (error instanceof z.ZodError) {
        error.errors.forEach(err => {
          const path = err.path[0] as string;
          const control = this.registerForm.get(path);
          if (control) {
            control.setErrors({ zod: err.message });
          }
        });
      }
    }
  }
  
  getErrorMessage(controlName: string): string {
    const control = this.registerForm.get(controlName);
    if (control?.errors) {
      if (control.errors['zod']) {
        return control.errors['zod'];
      }
      if (control.errors['required']) {
        return 'Este campo es obligatorio';
      }
      if (control.errors['requiredTrue']) {
        return 'Debes aceptar los términos y condiciones';
      }
      if (control.errors['minlength']) {
        return `Mínimo ${control.errors['minlength'].requiredLength} caracteres`;
      }
      if (control.errors['maxlength']) {
        return `Máximo ${control.errors['maxlength'].requiredLength} caracteres`;
      }
      if (control.errors['email']) {
        return 'Por favor, ingresa un correo electrónico válido';
      }
      if (control.errors['pattern']) {
        if (controlName === 'password') {
          return 'La contraseña debe contener al menos una letra mayúscula y un número';
        }
        return 'Formato inválido';
      }
      if (control.errors['passwordMismatch']) {
        return 'Las contraseñas no coinciden';
      }
    }
    return '';
  }
}
