import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

@Component({
  selector: 'app-create-auction',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './create-auction.component.html'
})
export default class CreateAuctionComponent {
  auctionForm: FormGroup;
  
  // Usando inject() en lugar de inyección por constructor
  private fb = inject(FormBuilder);
  
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
      startingPrice: [1.00, [Validators.required, Validators.min(0.01)]],
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
      console.log('Datos de la subasta:', this.auctionForm.value);
      // Aquí iría la lógica para crear la subasta
    } else {
      // Marcar todos los campos como tocados para mostrar errores
      Object.keys(this.auctionForm.controls).forEach(key => {
        this.auctionForm.get(key)?.markAsTouched();
      });
    }
  }
}
