package dev.mostapha.bidmaster.infrastructure.persistence.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad de subasta para la capa de infraestructura.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("auctions")
public class AuctionEntity {
    
    @Id
    private UUID id;
    
    private String title;
    private String description;
    private String slug;
    
    @Column("category")
    private String category;
    
    @Column("initial_price")
    private BigDecimal initialPrice;
    
    @Column("current_price")
    private BigDecimal currentPrice;
    
    @Column("min_bid_increment")
    private BigDecimal minBidIncrement;
    
    @Column("start_date")
    private LocalDateTime startDate;
    
    @Column("end_date")
    private LocalDateTime endDate;
    
    private String status;
    
    @Column("total_bids")
    private int totalBids;
    
    @Column("last_bid_date")
    private LocalDateTime lastBidDate;
    
    private String condition;
    
    @Column("shipping_options")
    private String shippingOptions;
    
    @Column("allow_pause")
    private boolean allowPause;
    
    @Column("seller_id")
    private UUID sellerId;
    
    @Column("winner_id")
    private UUID winnerId;
    
    @Column("created_at")
    private LocalDateTime createdAt;
    
    @Column("updated_at")
    private LocalDateTime updatedAt;
}
