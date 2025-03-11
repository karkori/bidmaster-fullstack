export interface ImageModel {
  id: string;
  filename: string;
  contentType: string;
  size: number;
  url: string;
  uploadedBy: string;
  uploadedAt: string;
}

export interface ImageUploadResponse {
  id: string;
  filename: string;
  contentType: string;
  size: number;
  url: string;
}
