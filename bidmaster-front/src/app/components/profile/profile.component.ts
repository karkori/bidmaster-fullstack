import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { User } from '@shared/models/user.model';
import { AuthService } from '@core/services/auth.service';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './profile.component.html',
  styleUrls: []
})
export default class ProfileComponent implements OnInit {
  currentUser: User | null = null;
  profileForm!: FormGroup;
  passwordForm!: FormGroup;
  
  // Usando inject() en lugar de inyección por constructor
  private authService = inject(AuthService);
  private fb = inject(FormBuilder);
  
  constructor() {}
  
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
