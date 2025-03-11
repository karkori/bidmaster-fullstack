import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface UploadedImage {
  id: string;
  filename: string;
  contentType: string;
  size: number;
  url: string;
  uploadedBy: string;
  uploadedAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class ImageService {
  private apiUrl = `${environment.apiUrl}/images`;
  private http = inject(HttpClient);

  /**
   * Carga una imagen al servidor MinIO
   * @param file El archivo de imagen a cargar
   * @param referenceId ID opcional de la subasta a la que asociar la imagen
   * @returns Observable con los datos de la imagen cargada
   */
  uploadImage(file: File, referenceId?: string): Observable<UploadedImage> {
    const formData = new FormData();
    formData.append('file', file, file.name);
    
    let url = `${this.apiUrl}/upload`;
    if (referenceId) {
      url += `?referenceId=${referenceId}&type=AUCTION`;
    }
    
    return this.http.post<UploadedImage>(url, formData);
  }

  /**
   * Carga múltiples imágenes al servidor MinIO
   * @param files Lista de archivos a cargar
   * @param referenceId ID opcional de la subasta a la que asociar las imágenes
   * @returns Observable con los datos de las imágenes cargadas
   */
  uploadMultipleImages(files: File[], referenceId?: string): Observable<UploadedImage[]> {
    const formData = new FormData();
    
    files.forEach((file, index) => {
      formData.append(`files`, file, file.name);
    });
    
    let url = `${this.apiUrl}/upload-multiple`;
    if (referenceId) {
      url += `?referenceId=${referenceId}&type=AUCTION`;
    }
    
    return this.http.post<UploadedImage[]>(url, formData);
  }

  /**
   * Obtiene todas las imágenes del usuario actual
   * @returns Observable con la lista de imágenes
   */
  getUserImages(): Observable<UploadedImage[]> {
    return this.http.get<UploadedImage[]>(`${this.apiUrl}/user`);
  }

  /**
   * Elimina una imagen por su ID
   * @param imageId ID de la imagen a eliminar
   * @returns Observable con la respuesta de eliminación
   */
  deleteImage(imageId: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${imageId}`);
  }
}
