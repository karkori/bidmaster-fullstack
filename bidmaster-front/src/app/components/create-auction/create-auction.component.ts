import { isPlatformBrowser } from '@angular/common';
import { Component, OnDestroy, OnInit, PLATFORM_ID, inject, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuctionService } from '@core/services/auction.service';
import { AuthService } from '@core/services/auth.service';
import { ImageService } from '@core/services/image.service';
import LoadingSpinnerComponent from '@shared/components/loading-spinner/loading-spinner.component';
import { FormControlDirective, FormLabelDirective } from '@shared/directives';
import { Auction, AuctionImage, CreateAuctionDto, ImageResponse } from '@shared/models/auction.model';
import { createAuctionSchema } from '@shared/validators/auction.schemas';
import { Observable, concatMap, from, lastValueFrom, of, toArray } from 'rxjs';

export interface PendingImageItem {
  id: string;
  file: File;
  previewUrl: string;
  isPrimary: boolean;
}

export interface ExistingImageItem {
  id: string;
  resolvedUrl: string;
  fileName: string;
  primary: boolean;
  displayOrder: number;
}

const CATEGORY_NORMALIZATION_MAP: Record<string, string> = {
  electronics: 'ELECTRONICS',
  electronica: 'ELECTRONICS',
  fashion: 'FASHION',
  moda: 'FASHION',
  home: 'HOME',
  'hogar y jardin': 'HOME',
  collectibles: 'COLLECTIBLES',
  coleccionables: 'COLLECTIBLES',
  art: 'ART',
  arte: 'ART',
  sports: 'SPORTS',
  deportes: 'SPORTS',
  toys: 'TOYS',
  juguetes: 'TOYS',
  vehicles: 'VEHICLES',
  vehiculos: 'VEHICLES',
  others: 'OTHERS',
  otros: 'OTHERS'
};

@Component({
  selector: 'app-create-auction',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    FormControlDirective,
    FormLabelDirective,
    LoadingSpinnerComponent
  ],
  templateUrl: './create-auction.component.html'
})
export default class CreateAuctionComponent implements OnInit, OnDestroy {
  auctionForm: FormGroup;
  isSubmitting = false;
  submitMode: 'draft' | 'publish' | null = null;
  errorMessage: string | null = null;
  imageError: string | null = null;
  isDragging = false;

  isEditMode = signal<boolean>(false);
  editAuctionId = signal<string | null>(null);
  loadingAuction = signal<boolean>(false);
  editBlockedReason = signal<string | null>(null);

  existingImages = signal<ExistingImageItem[]>([]);
  deletedExistingImageIds = signal<string[]>([]);
  pendingImages = signal<PendingImageItem[]>([]);
  createdDraftAuction = signal<Auction | null>(null);

  private originalExistingOrder: string[] = [];
  private originalPrimaryExistingId: string | null = null;

  private fb = inject(FormBuilder);
  private auctionService = inject(AuctionService);
  private authService = inject(AuthService);
  private imageService = inject(ImageService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private platformId = inject(PLATFORM_ID);
  private isBrowser = isPlatformBrowser(this.platformId);

  constructor() {
    const defaultEndDate = new Date();
    defaultEndDate.setDate(defaultEndDate.getDate() + 7);

    this.auctionForm = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(100)]],
      description: ['', [Validators.required, Validators.minLength(20), Validators.maxLength(1000)]],
      category: ['', [Validators.required]],
      initialPrice: [1.00, [Validators.required, Validators.min(0.01)]],
      minBidIncrement: [1.00, [Validators.min(0.01)]],
      depositRequired: [0, [Validators.min(0)]],
      endDate: [this.formatDateTimeLocal(defaultEndDate), [Validators.required]],
      condition: ['', [Validators.required]],
      shippingOptions: ['', [Validators.required]],
      allowPause: [false],
      terms: [false, [Validators.requiredTrue]]
    });
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode.set(true);
      this.editAuctionId.set(id);
      if (this.isBrowser) {
        this.loadAuctionForEdit(id);
      }
    }
  }

  loadAuctionForEdit(id: string): void {
    this.loadingAuction.set(true);
    this.editBlockedReason.set(null);
    this.errorMessage = null;

    this.auctionService.getAuctionById(id).subscribe({
      next: (auction) => {
        const currentUser = this.authService.currentUserValue;
        if (currentUser?.id && auction.sellerId && auction.sellerId !== currentUser.id) {
          this.editBlockedReason.set('No tienes permisos para editar esta subasta.');
          this.loadingAuction.set(false);
          return;
        }
        if (auction.status !== 'DRAFT') {
          this.editBlockedReason.set('Solo se pueden editar subastas que se encuentran en estado borrador.');
          this.loadingAuction.set(false);
          return;
        }

        this.createdDraftAuction.set(auction);
        this.auctionForm.patchValue({
          title: auction.title,
          description: auction.description,
          category: this.normalizeCategoryToEnum(auction.category),
          initialPrice: auction.initialPrice,
          minBidIncrement: auction.minBidIncrement ?? 1,
          depositRequired: auction.depositRequired ?? 0,
          endDate: this.formatDateTimeLocal(auction.endDate),
          condition: auction.condition,
          shippingOptions: auction.shippingOptions,
          allowPause: Boolean(auction.allowPause),
          terms: true
        });

        const sorted = [...(auction.images ?? [])]
          .sort((a: AuctionImage, b: AuctionImage) => (a.displayOrder ?? 0) - (b.displayOrder ?? 0))
          .map((img: AuctionImage, idx: number) => ({
            id: img.id,
            resolvedUrl: this.imageService.resolveImageUrl(img.url, img.id),
            fileName: img.fileName || `imagen-${idx + 1}`,
            primary: Boolean(img.primary),
            displayOrder: img.displayOrder ?? idx
          }));

        this.existingImages.set(sorted);
        this.deletedExistingImageIds.set([]);
        this.originalExistingOrder = sorted.map(img => img.id);
        this.originalPrimaryExistingId = sorted.find(img => img.primary)?.id ?? null;
        this.loadingAuction.set(false);
      },
      error: (err) => {
        const msg = this.extractBackendDetail(err) || 'No se pudo cargar el borrador de la subasta.';
        this.editBlockedReason.set(msg);
        this.loadingAuction.set(false);
      }
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.addFiles(Array.from(input.files));
      input.value = '';
    }
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = true;
  }

  onDragLeave(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = false;
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = false;

    const files = event.dataTransfer?.files;
    if (files && files.length > 0) {
      this.addFiles(Array.from(files));
    }
  }

  addFiles(files: File[]): void {
    this.imageError = null;
    const errors: string[] = [];
    const added: PendingImageItem[] = [];
    const current = this.pendingImages();
    let hasPrimary =
      this.existingImages().some(item => item.primary) ||
      current.some(item => item.isPrimary);

    for (const file of files) {
      const validationError = this.imageService.validateFile(file);
      if (validationError) {
        errors.push(validationError);
        continue;
      }

      const previewUrl = this.isBrowser && typeof URL !== 'undefined' && typeof URL.createObjectURL === 'function'
        ? URL.createObjectURL(file)
        : '/no-image.svg';

      const isPrimary = !hasPrimary;
      if (isPrimary) {
        hasPrimary = true;
      }

      added.push({
        id: `${Date.now()}-${Math.random().toString(36).slice(2, 9)}`,
        file,
        previewUrl,
        isPrimary
      });
    }

    if (added.length > 0) {
      this.pendingImages.set([...current, ...added]);
    }
    if (errors.length > 0) {
      this.imageError = errors.join(' ');
    }
  }

  setPrimaryImage(id: string): void {
    this.existingImages.update(items => items.map(item => ({ ...item, primary: false })));
    this.pendingImages.update(items =>
      items.map(item => ({
        ...item,
        isPrimary: item.id === id
      }))
    );
  }

  setExistingPrimaryImage(id: string): void {
    this.existingImages.update(items =>
      items.map(item => ({
        ...item,
        primary: item.id === id
      }))
    );
    this.pendingImages.update(items => items.map(item => ({ ...item, isPrimary: false })));
  }

  moveImage(index: number, direction: -1 | 1): void {
    const items = [...this.pendingImages()];
    const targetIndex = index + direction;
    if (targetIndex < 0 || targetIndex >= items.length) {
      return;
    }
    [items[index], items[targetIndex]] = [items[targetIndex], items[index]];
    this.pendingImages.set(items);
  }

  moveExistingImage(index: number, direction: -1 | 1): void {
    const items = [...this.existingImages()];
    const targetIndex = index + direction;
    if (targetIndex < 0 || targetIndex >= items.length) {
      return;
    }
    [items[index], items[targetIndex]] = [items[targetIndex], items[index]];
    this.existingImages.set(items.map((item, idx) => ({ ...item, displayOrder: idx })));
  }

  removeImage(id: string): void {
    const current = this.pendingImages();
    const removed = current.find(item => item.id === id);
    if (removed) {
      this.revokePreviewUrl(removed.previewUrl);
    }

    const remaining = current.filter(item => item.id !== id);
    const hasPrimaryElsewhere = this.existingImages().some(item => item.primary);
    if (!hasPrimaryElsewhere && remaining.length > 0 && !remaining.some(item => item.isPrimary)) {
      remaining[0] = { ...remaining[0], isPrimary: true };
    }
    this.pendingImages.set(remaining);
  }

  removeExistingImage(id: string): void {
    const current = this.existingImages();
    const target = current.find(item => item.id === id);
    if (!target) {
      return;
    }

    this.deletedExistingImageIds.update(ids => (ids.includes(id) ? ids : [...ids, id]));
    const remaining = current
      .filter(item => item.id !== id)
      .map((item, idx) => ({ ...item, displayOrder: idx }));

    const hasPrimary =
      remaining.some(item => item.primary) ||
      this.pendingImages().some(item => item.isPrimary);

    if (!hasPrimary) {
      if (remaining.length > 0) {
        remaining[0] = { ...remaining[0], primary: true };
      } else if (this.pendingImages().length > 0) {
        this.pendingImages.update(items =>
          items.map((item, idx) => ({ ...item, isPrimary: idx === 0 }))
        );
      }
    }

    this.existingImages.set(remaining);
  }

  onSubmit(): void {
    void this.submitAuction(true);
  }

  async submitAuction(publishImmediately: boolean): Promise<void> {
    this.errorMessage = null;

    if (this.editBlockedReason()) {
      return;
    }

    if (!this.validateWithZodAndMarkTouched()) {
      return;
    }

    this.isSubmitting = true;
    this.submitMode = publishImmediately ? 'publish' : 'draft';

    try {
      if (this.isEditMode() && this.editAuctionId()) {
        await this.submitEditMode(this.editAuctionId()!, publishImmediately);
      } else {
        await this.submitCreateMode(publishImmediately);
      }
    } catch (error: unknown) {
      this.isSubmitting = false;
      this.submitMode = null;
      this.errorMessage = this.extractErrorMessage(error, publishImmediately);
    }
  }

  getControlError(controlName: string): string | null {
    const control = this.auctionForm.get(controlName);
    if (!control || !control.errors || !control.touched) {
      return null;
    }
    if (typeof control.errors['zod'] === 'string') {
      return control.errors['zod'];
    }
    return null;
  }

  ngOnDestroy(): void {
    for (const item of this.pendingImages()) {
      this.revokePreviewUrl(item.previewUrl);
    }
  }

  private async submitCreateMode(publishImmediately: boolean): Promise<void> {
    const existingDraft = this.createdDraftAuction();
    const auction = existingDraft ?? await lastValueFrom(this.auctionService.createAuction(this.buildAuctionDto()));
    this.createdDraftAuction.set(auction);

    const imagesToUpload = [...this.pendingImages()];
    if (imagesToUpload.length > 0 && auction.id) {
      await lastValueFrom(this.uploadImagesSequentially(auction.id, imagesToUpload, 0));
    }

    let finalAuction = auction;
    if (publishImmediately && auction.id && auction.status !== 'ACTIVE') {
      finalAuction = await lastValueFrom(this.auctionService.publishAuction(auction.id));
    }

    this.isSubmitting = false;
    this.submitMode = null;
    await this.router.navigate(['/auctions', finalAuction.slug || finalAuction.id]);
  }

  private async submitEditMode(auctionId: string, publishImmediately: boolean): Promise<void> {
    let finalAuction = await lastValueFrom(
      this.auctionService.updateAuction(auctionId, this.buildAuctionDto())
    );
    this.createdDraftAuction.set(finalAuction);

    // 1. Eliminar imágenes existentes marcadas para borrado
    const toDelete = [...this.deletedExistingImageIds()];
    for (const imgId of toDelete) {
      await lastValueFrom(this.imageService.deleteImage(imgId));
      this.deletedExistingImageIds.update(ids => ids.filter(id => id !== imgId));
    }

    // 2. Subir nuevas imágenes pendientes a continuación de las existentes
    const remainingExisting = [...this.existingImages()];
    const imagesToUpload = [...this.pendingImages()];
    const uploadedResponses: ImageResponse[] = [];
    if (imagesToUpload.length > 0) {
      const results = await lastValueFrom(
        this.uploadImagesSequentially(auctionId, imagesToUpload, remainingExisting.length)
      );
      uploadedResponses.push(...results);
    }

    // 3. Reordenar imágenes existentes/subidas si cambió el orden o hubo borrados
    const allRemainingIds = [
      ...remainingExisting.map(img => img.id),
      ...uploadedResponses.map(img => img.id)
    ];
    const orderChanged =
      toDelete.length > 0 ||
      remainingExisting.length !== this.originalExistingOrder.length ||
      remainingExisting.some((img, idx) => img.id !== this.originalExistingOrder[idx]);

    if (allRemainingIds.length > 0 && orderChanged) {
      await lastValueFrom(this.imageService.reorderImages(auctionId, allRemainingIds, 'AUCTION'));
    }

    // 4. Si se marcó otra imagen existente como portada, aplicarlo por PATCH
    const targetPrimaryExisting = remainingExisting.find(img => img.primary);
    const uploadedPrimary = uploadedResponses.some(img => img.primary);
    if (
      targetPrimaryExisting &&
      !uploadedPrimary &&
      targetPrimaryExisting.id !== this.originalPrimaryExistingId
    ) {
      await lastValueFrom(this.imageService.setPrimaryImage(targetPrimaryExisting.id));
      this.originalPrimaryExistingId = targetPrimaryExisting.id;
    }

    this.originalExistingOrder = [...allRemainingIds];

    // 5. Publicar inmediatamente si se pulsó "Guardar y publicar"
    if (publishImmediately && finalAuction.status !== 'ACTIVE') {
      finalAuction = await lastValueFrom(this.auctionService.publishAuction(auctionId));
    }

    this.isSubmitting = false;
    this.submitMode = null;

    if (publishImmediately) {
      await this.router.navigate(['/auctions', finalAuction.slug || finalAuction.id]);
    } else {
      await this.router.navigate(['/dashboard/my-auctions']);
    }
  }

  private buildAuctionDto(): CreateAuctionDto {
    const val = this.auctionForm.value;
    const rawEndDate: string = val.endDate;
    const formattedEndDate = rawEndDate && !/\d{2}:\d{2}:\d{2}/.test(rawEndDate)
      ? `${rawEndDate}:00`
      : rawEndDate;

    return {
      title: String(val.title).trim(),
      description: String(val.description).trim(),
      category: String(val.category),
      initialPrice: Number(val.initialPrice),
      minBidIncrement: val.minBidIncrement ? Number(val.minBidIncrement) : undefined,
      depositRequired: val.depositRequired !== null && val.depositRequired !== undefined
        ? Number(val.depositRequired)
        : 0,
      endDate: formattedEndDate,
      condition: String(val.condition),
      shippingOptions: String(val.shippingOptions),
      allowPause: Boolean(val.allowPause)
    };
  }

  private uploadImagesSequentially(
    auctionId: string,
    items: PendingImageItem[],
    startOrder: number
  ): Observable<ImageResponse[]> {
    return from(items.map((item, idx) => ({ item, order: startOrder + idx }))).pipe(
      concatMap(({ item, order }) =>
        this.imageService.uploadImage(item.file, 'AUCTION', auctionId, {
          isPrimary: item.isPrimary,
          displayOrder: order
        }).pipe(
          concatMap(res => {
            this.removeUploadedFromQueue(item.id);
            return of(res);
          })
        )
      ),
      toArray()
    );
  }

  private removeUploadedFromQueue(id: string): void {
    const target = this.pendingImages().find(img => img.id === id);
    if (target) {
      this.revokePreviewUrl(target.previewUrl);
    }
    this.pendingImages.update(list => list.filter(img => img.id !== id));
  }

  private validateWithZodAndMarkTouched(): boolean {
    Object.keys(this.auctionForm.controls).forEach(key => {
      const control = this.auctionForm.get(key);
      control?.markAsTouched();
      if (control?.errors?.['zod']) {
        const { zod, ...rest } = control.errors;
        control.setErrors(Object.keys(rest).length > 0 ? rest : null);
      }
    });

    const result = createAuctionSchema.safeParse(this.auctionForm.value);
    if (!result.success) {
      for (const issue of result.error.issues) {
        const field = issue.path[0];
        if (typeof field === 'string') {
          const control = this.auctionForm.get(field);
          if (control) {
            control.setErrors({ ...(control.errors ?? {}), zod: issue.message });
          }
        }
      }
      return false;
    }

    return this.auctionForm.valid;
  }

  private normalizeCategoryToEnum(rawCategory?: string): string {
    if (!rawCategory) {
      return '';
    }
    const normalized = rawCategory
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .trim()
      .toLowerCase();
    return CATEGORY_NORMALIZATION_MAP[normalized] ?? rawCategory.toUpperCase();
  }

  private formatDateTimeLocal(dateValue: Date | string): string {
    const date = dateValue instanceof Date ? dateValue : new Date(dateValue);
    if (Number.isNaN(date.getTime())) {
      return '';
    }
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day}T${hours}:${minutes}`;
  }

  private extractBackendDetail(error: unknown): string | undefined {
    const errObj = error as { error?: { detail?: string; message?: string }; message?: string };
    return errObj?.error?.detail || errObj?.error?.message || errObj?.message;
  }

  private extractErrorMessage(error: unknown, publishImmediately: boolean): string {
    const backendMsg = this.extractBackendDetail(error);
    if (!this.isEditMode() && this.createdDraftAuction()) {
      return backendMsg
        ? `La subasta quedó guardada como borrador, pero falló el paso siguiente: ${backendMsg}`
        : `La subasta quedó guardada como borrador, pero ocurrió un error al subir imágenes${publishImmediately ? ' o publicar' : ''}. Puedes reintentar sin duplicarla.`;
    }
    if (this.isEditMode()) {
      return backendMsg || 'Ha ocurrido un error al actualizar el borrador. Por favor, inténtalo de nuevo.';
    }
    return backendMsg || 'Ha ocurrido un error al crear la subasta. Por favor, inténtalo de nuevo.';
  }

  private revokePreviewUrl(url: string): void {
    if (this.isBrowser && url.startsWith('blob:') && typeof URL !== 'undefined' && typeof URL.revokeObjectURL === 'function') {
      URL.revokeObjectURL(url);
    }
  }
}
