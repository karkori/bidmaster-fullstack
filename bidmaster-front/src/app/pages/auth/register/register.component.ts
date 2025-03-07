import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '@core/services/auth.service';
import { registerSchema } from '@shared/validators/auth.schemas';
import { z } from 'zod';

// Importar directivas de formulario
import { FormControlDirective, FormLabelDirective } from '@shared/directives';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule, 
    ReactiveFormsModule, 
    RouterModule,
    FormControlDirective,
    FormLabelDirective
  ],
  templateUrl: './register.component.html',
  styleUrls: []
})
export default class RegisterComponent {
  registerForm: FormGroup;
  loading = false;
  errorMessage = '';
  successMessage = '';
  
  // Usando inject() en lugar de inyección por constructor
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  
  constructor() {
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
