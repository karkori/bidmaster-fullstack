export interface Auction {
  id?: string;
  title: string;
  description: string;
  slug?: string;
  category: string;
  initialPrice: number;
  currentPrice?: number;
  minBidIncrement?: number;
  depositRequired?: number;
  startDate?: Date;
  endDate: Date;
  status?: string;
  totalBids?: number;
  lastBidDate?: Date;
  condition: string;
  shippingOptions: string;
  allowPause?: boolean;
  pausesUsed?: number;
  sellerId?: string;
  sellerUsername?: string;
  winnerId?: string;
  winnerUsername?: string;
  timeLeftSeconds?: number;
  images?: AuctionImage[];
  createdAt?: Date;
  updatedAt?: Date;
}

export interface AuctionImage {
  id: string;
  url: string;
  description: string | null;
  displayOrder: number;
  fileName: string;
  contentType: string;
  fileSize: number;
  primary: boolean;
}

export type ImageType = 'AUCTION' | 'PROFILE' | 'CATEGORY' | 'GENERAL';

export interface ImageUploadOptions {
  isPrimary?: boolean;
  displayOrder?: number;
  userId?: string;
}

export interface ReorderImagesRequest {
  imageIds: string[];
}

export interface ImageResponse {
  id: string;
  filename: string;
  originalFilename: string;
  contentType: string;
  size: number;
  imageUrl: string;
  thumbnailUrl?: string;
  uploadDate: string;
  uploadedBy: string;
  type: ImageType;
  referenceId?: string;
  primary: boolean;
  displayOrder: number;
}

export interface CreateAuctionDto {
  title: string;
  description: string;
  category: string;
  initialPrice: number;
  endDate: string;
  minBidIncrement?: number;
  depositRequired?: number;
  condition: string;
  shippingOptions: string;
  allowPause?: boolean;
}

export interface AuctionFilters {
  category?: string;
  status?: string;
  minPrice?: number;
  maxPrice?: number;
  sellerId?: string;
}
