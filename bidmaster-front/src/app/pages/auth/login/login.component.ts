import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { z } from 'zod';
import { loginSchema, LoginFormType } from '@shared/validators/auth.schemas';
import { AuthService } from '@core/services/auth.service';

// Importar directivas de formulario
import { FormControlDirective, FormLabelDirective } from '@shared/directives';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule, 
    ReactiveFormsModule, 
    RouterModule,
    FormControlDirective,
    FormLabelDirective
  ],
  templateUrl: './login.component.html',
  styleUrls: []
})
export default class LoginComponent {
  loginForm: FormGroup;
  loading = false;
  errorMessage = '';
  
  // Usando inject() en lugar de inyección por constructor
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  
  constructor() {
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
