import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

@Component({
  selector: 'app-create-auction',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="max-w-4xl mx-auto">
      <div class="bg-white rounded-lg shadow-md overflow-hidden">
        <div class="p-6 bg-gradient-to-r from-emerald-600 to-emerald-800 text-white">
          <h1 class="text-2xl font-bold">Crear nueva subasta</h1>
          <p class="opacity-80">Publica tu artículo para que otros usuarios pujen por él</p>
        </div>
        
        <div class="p-6">
          <form [formGroup]="auctionForm" (ngSubmit)="onSubmit()" class="space-y-6">
            <div>
              <h3 class="text-lg font-medium mb-4">Información básica</h3>
              
              <div class="grid grid-cols-1 gap-6 mb-6">
                <div>
                  <label for="title" class="block text-sm font-medium text-gray-700">Título del artículo *</label>
                  <input 
                    type="text" 
                    id="title" 
                    formControlName="title"
                    class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-emerald-500 focus:ring-emerald-500" 
                  />
                  @if (auctionForm.get('title')?.invalid && auctionForm.get('title')?.touched) {
                    <p class="mt-1 text-sm text-red-600">El título es obligatorio</p>
                  }
                </div>
                
                <div>
                  <label for="description" class="block text-sm font-medium text-gray-700">Descripción detallada *</label>
                  <textarea 
                    id="description" 
                    formControlName="description"
                    rows="4"
                    class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-emerald-500 focus:ring-emerald-500" 
                  ></textarea>
                  @if (auctionForm.get('description')?.invalid && auctionForm.get('description')?.touched) {
                    <p class="mt-1 text-sm text-red-600">La descripción es obligatoria y debe tener al menos 20 caracteres</p>
                  }
                </div>
                
                <div>
                  <label for="category" class="block text-sm font-medium text-gray-700">Categoría *</label>
                  <select 
                    id="category" 
                    formControlName="category"
                    class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-emerald-500 focus:ring-emerald-500" 
                  >
                    <option value="">Selecciona una categoría</option>
                    <option value="electronics">Electrónica</option>
                    <option value="fashion">Moda</option>
                    <option value="home">Hogar y Jardín</option>
                    <option value="collectibles">Coleccionables</option>
                    <option value="art">Arte</option>
                    <option value="sports">Deportes</option>
                    <option value="toys">Juguetes</option>
                    <option value="vehicles">Vehículos</option>
                    <option value="others">Otros</option>
                  </select>
                  @if (auctionForm.get('category')?.invalid && auctionForm.get('category')?.touched) {
                    <p class="mt-1 text-sm text-red-600">Selecciona una categoría</p>
                  }
                </div>
              </div>
            </div>
            
            <div class="pt-4 border-t border-gray-200">
              <h3 class="text-lg font-medium mb-4">Detalles de la subasta</h3>
              
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
                <div>
                  <label for="startingPrice" class="block text-sm font-medium text-gray-700">Precio inicial ($) *</label>
                  <input 
                    type="number" 
                    id="startingPrice" 
                    formControlName="startingPrice"
                    min="0.01"
                    step="0.01"
                    class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-emerald-500 focus:ring-emerald-500" 
                  />
                  @if (auctionForm.get('startingPrice')?.invalid && auctionForm.get('startingPrice')?.touched) {
                    <p class="mt-1 text-sm text-red-600">El precio inicial debe ser mayor que 0</p>
                  }
                </div>
                
                <div>
                  <label for="reservePrice" class="block text-sm font-medium text-gray-700">Precio de reserva ($)</label>
                  <input 
                    type="number" 
                    id="reservePrice" 
                    formControlName="reservePrice"
                    min="0"
                    step="0.01"
                    class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-emerald-500 focus:ring-emerald-500" 
                  />
                  <p class="mt-1 text-xs text-gray-500">Precio mínimo para que la venta se concrete (opcional)</p>
                </div>
                
                <div>
                  <label for="endDate" class="block text-sm font-medium text-gray-700">Fecha de finalización *</label>
                  <input 
                    type="datetime-local" 
                    id="endDate" 
                    formControlName="endDate"
                    class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-emerald-500 focus:ring-emerald-500" 
                  />
                  @if (auctionForm.get('endDate')?.invalid && auctionForm.get('endDate')?.touched) {
                    <p class="mt-1 text-sm text-red-600">Selecciona una fecha y hora válida</p>
                  }
                </div>
                
                <div>
                  <label for="minBidIncrement" class="block text-sm font-medium text-gray-700">Incremento mínimo ($)</label>
                  <input 
                    type="number" 
                    id="minBidIncrement" 
                    formControlName="minBidIncrement"
                    min="0.01"
                    step="0.01"
                    class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-emerald-500 focus:ring-emerald-500" 
                  />
                  <p class="mt-1 text-xs text-gray-500">Cantidad mínima que debe aumentar cada puja</p>
                </div>
              </div>
            </div>
            
            <div class="pt-4 border-t border-gray-200">
              <h3 class="text-lg font-medium mb-4">Imágenes</h3>
              
              <div class="mb-6">
                <label class="block text-sm font-medium text-gray-700 mb-2">Sube imágenes de tu artículo</label>
                <div class="border-2 border-dashed border-gray-300 rounded-md p-6 text-center">
                  <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="mx-auto h-12 w-12 text-gray-400">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M2.25 15.75l5.159-5.159a2.25 2.25 0 013.182 0l5.159 5.159m-1.5-1.5l1.409-1.409a2.25 2.25 0 013.182 0l2.909 2.909m-18 3.75h16.5a1.5 1.5 0 001.5-1.5V6a1.5 1.5 0 00-1.5-1.5H3.75A1.5 1.5 0 002.25 6v12a1.5 1.5 0 001.5 1.5zm10.5-11.25h.008v.008h-.008V8.25zm.375 0a.375.375 0 11-.75 0 .375.375 0 01.75 0z" />
                  </svg>
                  <div class="mt-2">
                    <p class="text-sm text-gray-500">PNG, JPG, GIF hasta 5MB</p>
                  </div>
                  <div class="mt-3">
                    <label for="file-upload" class="relative cursor-pointer rounded-md bg-white font-medium text-emerald-600 focus-within:outline-none focus-within:ring-2 focus-within:ring-emerald-500 focus-within:ring-offset-2 hover:text-emerald-500">
                      <span>Subir archivos</span>
                      <input id="file-upload" name="file-upload" type="file" class="sr-only" multiple accept="image/*" />
                    </label>
                  </div>
                  <p class="text-xs text-gray-500 mt-2">Arrastra y suelta archivos aquí, o haz clic para seleccionar</p>
                </div>
              </div>
            </div>
            
            <div class="pt-4 border-t border-gray-200">
              <h3 class="text-lg font-medium mb-4">Condiciones</h3>
              
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
                <div>
                  <label for="condition" class="block text-sm font-medium text-gray-700">Estado del artículo *</label>
                  <select 
                    id="condition" 
                    formControlName="condition"
                    class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-emerald-500 focus:ring-emerald-500" 
                  >
                    <option value="">Selecciona un estado</option>
                    <option value="new">Nuevo</option>
                    <option value="like_new">Como nuevo</option>
                    <option value="good">Buen estado</option>
                    <option value="fair">Estado aceptable</option>
                    <option value="poor">Necesita reparación</option>
                  </select>
                  @if (auctionForm.get('condition')?.invalid && auctionForm.get('condition')?.touched) {
                    <p class="mt-1 text-sm text-red-600">Selecciona el estado del artículo</p>
                  }
                </div>
                
                <div>
                  <label for="shippingOptions" class="block text-sm font-medium text-gray-700">Opciones de envío *</label>
                  <select 
                    id="shippingOptions" 
                    formControlName="shippingOptions"
                    class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-emerald-500 focus:ring-emerald-500" 
                  >
                    <option value="">Selecciona una opción</option>
                    <option value="seller_pays">El vendedor paga el envío</option>
                    <option value="buyer_pays">El comprador paga el envío</option>
                    <option value="pickup_only">Solo recogida en persona</option>
                  </select>
                  @if (auctionForm.get('shippingOptions')?.invalid && auctionForm.get('shippingOptions')?.touched) {
                    <p class="mt-1 text-sm text-red-600">Selecciona una opción de envío</p>
                  }
                </div>
              </div>
            </div>
            
            <div class="pt-4 border-t border-gray-200">
              <div class="flex items-center">
                <input 
                  id="terms" 
                  formControlName="terms"
                  type="checkbox" 
                  class="h-4 w-4 rounded border-gray-300 text-emerald-600 focus:ring-emerald-500" 
                />
                <label for="terms" class="ml-2 block text-sm text-gray-900">
                  Acepto los <a href="#" class="text-emerald-600 hover:text-emerald-500">Términos y Condiciones</a> para la publicación de subastas
                </label>
              </div>
              @if (auctionForm.get('terms')?.invalid && auctionForm.get('terms')?.touched) {
                <p class="mt-1 text-sm text-red-600">Debes aceptar los términos y condiciones</p>
              }
            </div>
            
            <div class="pt-4 border-t border-gray-200">
              <div class="flex justify-end">
                <button
                  type="button"
                  class="mr-3 rounded-md bg-white px-4 py-2 text-sm font-semibold text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 hover:bg-gray-50"
                >
                  Guardar borrador
                </button>
                <button
                  type="submit"
                  [disabled]="auctionForm.invalid"
                  class="rounded-md bg-emerald-600 px-4 py-2 text-sm font-semibold text-white shadow-sm hover:bg-emerald-500 focus:outline-none focus:ring-2 focus:ring-emerald-600 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  Publicar subasta
                </button>
              </div>
            </div>
          </form>
        </div>
      </div>
    </div>
  `
})
export default class CreateAuctionComponent {
  auctionForm: FormGroup;
  
  constructor(private fb: FormBuilder) {
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
