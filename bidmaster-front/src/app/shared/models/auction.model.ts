export interface Auction {
  id?: string;
  title: string;
  description: string;
  slug?: string;
  category: string;
  initialPrice: number;
  currentPrice?: number;
  minBidIncrement?: number;
  startDate?: Date;
  endDate: Date;
  status?: string;
  totalBids?: number;
  lastBidDate?: Date;
  condition: string;
  shippingOptions: string;
  allowPause?: boolean;
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
  id:           string;
  url:          string;
  description:  null;
  displayOrder: number;
  fileName:     string;
  contentType:  string;
  fileSize:     number;
  primary:      boolean;
}

// export interface AuctionImage {
//   id?: string;
//   auctionId?: string;
//   url: string;
//   description?: string;
//   isPrimary: boolean;
//   displayOrder?: number;
// }

export interface CreateAuctionDto {
  title: string;
  description: string;
  category: string;
  initialPrice: number;
  reservePrice?: number;
  endDate: string;
  minBidIncrement?: number;
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
