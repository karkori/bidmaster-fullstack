import { Component, inject, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { FormControlDirective, FormLabelDirective } from '@shared/directives';
import { Router } from '@angular/router';
import { AuctionService } from '@core/services/auction.service';
import { ImageService } from '@core/services/image.service';
import { CreateAuctionDto } from '@shared/models/auction.model';
import { UploadedImage } from '@core/services/image.service';

@Component({
  selector: 'app-create-auction',
  standalone: true,
  imports: [
    CommonModule, 
    ReactiveFormsModule,
    FormControlDirective,
    FormLabelDirective
  ],
  templateUrl: './create-auction.component.html',
  styles: [`
    .image-preview {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
      gap: 1rem;
      margin-top: 1rem;
    }
    .image-preview-item {
      position: relative;
      padding-top: 100%;
      overflow: hidden;
      border-radius: 0.375rem;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
    }
    .image-preview-item img {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    .image-preview-item .remove-btn {
      position: absolute;
      top: 0.25rem;
      right: 0.25rem;
      background: rgba(0, 0, 0, 0.5);
      color: white;
      border-radius: 9999px;
      width: 1.5rem;
      height: 1.5rem;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      transition: background-color 0.2s;
    }
    .image-preview-item .remove-btn:hover {
      background: rgba(220, 38, 38, 0.8);
    }
    .dropzone {
      position: relative;
      border: 2px dashed #d1d5db;
      border-radius: 0.375rem;
      padding: 1.5rem;
      text-align: center;
      transition: border-color 0.2s, background-color 0.2s;
    }
    .dropzone.active {
      border-color: #10b981;
      background-color: rgba(16, 185, 129, 0.05);
    }
    .loading-overlay {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background: rgba(255, 255, 255, 0.8);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 10;
    }
    .spinner {
      width: 2rem;
      height: 2rem;
      border: 3px solid #f3f3f3;
      border-top: 3px solid #10b981;
      border-radius: 50%;
      animation: spin 1s linear infinite;
    }
    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }
  `]
})
export default class CreateAuctionComponent {
  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;
  
  auctionForm: FormGroup;
  isSubmitting = false;
  errorMessage: string | null = null;
  
  uploadedImages: UploadedImage[] = [];
  isUploading = false;
  uploadError: string | null = null;
  isDragging = false;
  maxFileSize = 5 * 1024 * 1024; // 5MB en bytes
  
  // Usando inject() en lugar de inyección por constructor
  private fb = inject(FormBuilder);
  private auctionService = inject(AuctionService);
  private imageService = inject(ImageService);
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
        allowPause: this.auctionForm.value.allowPause || false,
        images: this.uploadedImages.map(img => img.id)
      };
      
      // Enviar al backend
      this.auctionService.createAuction(auctionData).subscribe({
        next: (createdAuction) => {
          this.isSubmitting = false;
          console.log('Subasta creada exitosamente:', createdAuction);
          
          // Asociar las imágenes con la subasta creada
          if (createdAuction && createdAuction.id && this.uploadedImages.length > 0) {
            const imageIds = this.uploadedImages.map(img => img.id);
            console.log('Asociando imágenes con la subasta:', createdAuction.id, imageIds);
            this.auctionService.associateImagesToAuction(createdAuction.id, imageIds).subscribe({
              next: (updatedAuction) => {
                console.log('Imágenes asociadas correctamente:', updatedAuction);
                if (updatedAuction.images && updatedAuction.images.length > 0) {
                  console.log('Subasta ahora tiene', updatedAuction.images.length, 'imágenes');
                }
              },
              error: (error) => console.error('Error al asociar imágenes:', error)
            });
          }
          
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
  
  /**
   * Maneja la selección de archivos a través del input file
   */
  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.processFiles(Array.from(input.files));
    }
  }
  
  /**
   * Maneja el evento de arrastrar archivos sobre la zona de drop
   */
  onDragOver(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = true;
  }
  
  /**
   * Maneja el evento de salir de la zona de drop
   */
  onDragLeave(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = false;
  }
  
  /**
   * Maneja el evento de soltar archivos en la zona de drop
   */
  onDrop(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = false;
    
    const files = event.dataTransfer?.files;
    if (files && files.length > 0) {
      this.processFiles(Array.from(files));
    }
  }
  
  /**
   * Abre el diálogo de selección de archivos
   */
  triggerFileInput(): void {
    this.fileInput.nativeElement.click();
  }
  
  /**
   * Procesa los archivos seleccionados, filtrando solo imágenes válidas
   */
  private processFiles(files: File[]): void {
    // Filtrar solo archivos de imagen y verificar tamaño
    const validImageFiles = files.filter(file => {
      if (!file.type.startsWith('image/')) {
        this.uploadError = `El archivo ${file.name} no es una imagen válida`;
        return false;
      }
      
      if (file.size > this.maxFileSize) {
        this.uploadError = `El archivo ${file.name} excede el tamaño máximo de 5MB`;
        return false;
      }
      
      return true;
    });
    
    if (validImageFiles.length === 0) return;
    
    this.uploadFiles(validImageFiles);
  }
  
  /**
   * Sube los archivos al servidor
   * @param files Archivos a subir
   * @param auctionId Opcional: ID de la subasta a la que asociar las imágenes
   */
  private uploadFiles(files: File[], auctionId?: string): void {
    this.isUploading = true;
    this.uploadError = null;
    
    // Si es solo un archivo, usar el método de carga individual
    if (files.length === 1) {
      this.imageService.uploadImage(files[0], auctionId).subscribe({
        next: (response) => {
          this.uploadedImages.push(response);
          this.isUploading = false;
        },
        error: (error) => {
          this.handleUploadError(error);
        }
      });
    } else {
      // Si son múltiples archivos, usar el método de carga múltiple
      this.imageService.uploadMultipleImages(files, auctionId).subscribe({
        next: (response) => {
          this.uploadedImages = [...this.uploadedImages, ...response];
          this.isUploading = false;
        },
        error: (error) => {
          this.handleUploadError(error);
        }
      });
    }
  }
  
  /**
   * Maneja errores durante la carga de archivos
   */
  private handleUploadError(error: any): void {
    this.isUploading = false;
    this.uploadError = error.error?.message || 'Error al cargar las imágenes. Por favor, inténtalo de nuevo.';
    console.error('Error al cargar imágenes:', error);
  }
  
  /**
   * Elimina una imagen de la lista de imágenes cargadas
   */
  removeImage(index: number): void {
    const imageToRemove = this.uploadedImages[index];
    
    // Eliminar del backend
    this.imageService.deleteImage(imageToRemove.id).subscribe({
      next: () => {
        // Eliminar de la lista local solo si se eliminó correctamente del backend
        this.uploadedImages.splice(index, 1);
        this.uploadedImages = [...this.uploadedImages]; // Forzar detección de cambios
      },
      error: (error) => {
        console.error('Error al eliminar imagen:', error);
        // Opcionalmente, mostrar un mensaje de error
      }
    });
  }
}
