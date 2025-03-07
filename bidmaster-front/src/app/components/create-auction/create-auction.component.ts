import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { FormControlDirective, FormLabelDirective } from '@shared/directives';
import { Router } from '@angular/router';
import { AuctionService } from '@core/services/auction.service';
import { CreateAuctionDto } from '@shared/models/auction.model';

@Component({
  selector: 'app-create-auction',
  standalone: true,
  imports: [
    CommonModule, 
    ReactiveFormsModule,
    FormControlDirective,
    FormLabelDirective
  ],
  templateUrl: './create-auction.component.html'
})
export default class CreateAuctionComponent {
  auctionForm: FormGroup;
  isSubmitting = false;
  errorMessage: string | null = null;
  
  // Usando inject() en lugar de inyección por constructor
  private fb = inject(FormBuilder);
  private auctionService = inject(AuctionService);
  private router = inject(Router);
  
  constructor() {
    // Obtener la fecha actual + 7 días para la fecha de finalización predeterminada
    const defaultEndDate = new Date();
    defaultEndDate.setDate(defaultEndDate.getDate() + 7);
    
    // Formatear fecha para input datetime-local
    const year = defaultEndDate.getFullYear();
    const month = String(defaultEndDate.getMonth() + 1).padStart(2, '0'); 
    const day = String(defaultEndDate.getDate()).padStart(2, '0');
    const hours = String(defaultEndDate.getHours()).padStart(2, '0');
    const minutes = String(defaultEndDate.getMinutes()).padStart(2, '0');
    const defaultEndDateStr = `${year}-${month}-${day}T${hours}:${minutes}`;
    
    this.auctionForm = this.fb.group({
      title: ['', [Validators.required]],
      description: ['', [Validators.required, Validators.minLength(20)]],
      category: ['', [Validators.required]],
      initialPrice: [1.00, [Validators.required, Validators.min(0.01)]],
      reservePrice: [0],
      endDate: [defaultEndDateStr, [Validators.required]],
      minBidIncrement: [1.00],
      condition: ['', [Validators.required]],
      shippingOptions: ['', [Validators.required]],
      terms: [false, [Validators.requiredTrue]]
    });
  }
  
  onSubmit(): void {
    if (this.auctionForm.valid) {
      this.isSubmitting = true;
      this.errorMessage = null;
      
      // Formatear la fecha para incluir segundos (requerido por el backend)
      const rawEndDate = this.auctionForm.value.endDate;
      let formattedEndDate = rawEndDate;
      
      // Si la fecha no incluye segundos, añadirlos
      if (rawEndDate && !rawEndDate.includes(':ss') && !rawEndDate.match(/\d{2}:\d{2}:\d{2}/)) {
        formattedEndDate = rawEndDate + ':00';
      }
      
      // Crear el DTO
      const auctionData: CreateAuctionDto = {
        title: this.auctionForm.value.title,
        description: this.auctionForm.value.description,
        category: this.auctionForm.value.category,
        initialPrice: this.auctionForm.value.initialPrice,
        reservePrice: this.auctionForm.value.reservePrice,
        endDate: formattedEndDate,
        minBidIncrement: this.auctionForm.value.minBidIncrement,
        condition: this.auctionForm.value.condition,
        shippingOptions: this.auctionForm.value.shippingOptions,
        allowPause: this.auctionForm.value.allowPause || false
      };
      
      // Enviar al backend
      this.auctionService.createAuction(auctionData).subscribe({
        next: (createdAuction) => {
          this.isSubmitting = false;
          console.log('Subasta creada exitosamente:', createdAuction);
          // Redireccionar a la página de detalle o listado
          this.router.navigate(['/dashboard']);
        },
        error: (error) => {
          this.isSubmitting = false;
          this.errorMessage = error.error?.message || 'Ha ocurrido un error al crear la subasta. Por favor, inténtalo de nuevo.';
          console.error('Error al crear subasta:', error);
        }
      });
    } else {
      // Marcar todos los campos como tocados para mostrar errores
      Object.keys(this.auctionForm.controls).forEach(key => {
        this.auctionForm.get(key)?.markAsTouched();
      });
    }
  }
  
  // Getters para acceder fácilmente a los controles del formulario en la plantilla
  get title() { return this.auctionForm.get('title'); }
  get description() { return this.auctionForm.get('description'); }
  get category() { return this.auctionForm.get('category'); }
  get initialPrice() { return this.auctionForm.get('initialPrice'); }
  get endDate() { return this.auctionForm.get('endDate'); }
  get condition() { return this.auctionForm.get('condition'); }
  get shippingOptions() { return this.auctionForm.get('shippingOptions'); }
  get terms() { return this.auctionForm.get('terms'); }
}
