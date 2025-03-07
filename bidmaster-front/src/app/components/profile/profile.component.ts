import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { User } from '@shared/models/user.model';
import { AuthService } from '@core/services/auth.service';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

// Importar componentes compartidos
import PageHeaderComponent from '../../shared/components/page-header/page-header.component';
import LoadingSpinnerComponent from '../../shared/components/loading-spinner/loading-spinner.component';
import StatusBadgeComponent from '../../shared/components/status-badge/status-badge.component';

// Importar directivas de formulario
import { FormControlDirective, FormLabelDirective } from '../../shared/directives';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [
    CommonModule, 
    ReactiveFormsModule,
    PageHeaderComponent,
    LoadingSpinnerComponent,
    StatusBadgeComponent,
    FormControlDirective,
    FormLabelDirective
  ],
  templateUrl: './profile.component.html',
  styleUrls: []
})
export default class ProfileComponent implements OnInit {
  currentUser: User | null = null;
  profileForm!: FormGroup;
  passwordForm!: FormGroup;
  loading = true;
  saving = false;
  savingPassword = false;
  saveSuccess = false;
  savePasswordSuccess = false;
  
  // Usando inject() en lugar de inyección por constructor
  private authService = inject(AuthService);
  private fb = inject(FormBuilder);
  
  constructor() {}
  
  ngOnInit(): void {
    // Simular carga de datos del usuario
    setTimeout(() => {
      this.currentUser = this.authService.currentUserValue;
      this.initForms();
      this.loading = false;
    }, 1000);
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
      this.saving = true;
      this.saveSuccess = false;
      
      // Simular guardado de datos con un retraso
      setTimeout(() => {
        // Aquí iría la lógica para actualizar el perfil
        console.log('Datos del perfil a actualizar:', this.profileForm.value);
        
        this.saving = false;
        this.saveSuccess = true;
        
        // Ocultar mensaje de éxito después de un tiempo
        setTimeout(() => {
          this.saveSuccess = false;
        }, 3000);
      }, 1500);
    }
  }
  
  onPasswordChange(): void {
    if (this.passwordForm.valid) {
      this.savingPassword = true;
      this.savePasswordSuccess = false;
      
      // Simular cambio de contraseña con un retraso
      setTimeout(() => {
        // Aquí iría la lógica para cambiar la contraseña
        console.log('Cambio de contraseña:', this.passwordForm.value);
        
        this.savingPassword = false;
        this.savePasswordSuccess = true;
        this.passwordForm.reset();
        
        // Ocultar mensaje de éxito después de un tiempo
        setTimeout(() => {
          this.savePasswordSuccess = false;
        }, 3000);
      }, 1500);
    }
  }
}
