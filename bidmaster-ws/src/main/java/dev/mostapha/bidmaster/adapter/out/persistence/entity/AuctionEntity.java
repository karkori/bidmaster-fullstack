package dev.mostapha.bidmaster.adapter.out.persistence.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "auctions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuctionEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(unique = true, length = 150)
    private String slug;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(name = "initial_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal initialPrice;

    @Column(name = "current_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal currentPrice;

    @Column(name = "min_bid_increment", nullable = false, precision = 19, scale = 2)
    private BigDecimal minBidIncrement;

    @Column(name = "deposit_required", nullable = false, precision = 19, scale = 2)
    private BigDecimal depositRequired;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "pause_date")
    private LocalDateTime pauseDate;

    @Column(name = "pause_duration_seconds")
    private Long pauseDurationSeconds;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "total_bids", nullable = false)
    private int totalBids;

    @Column(name = "last_bid_date")
    private LocalDateTime lastBidDate;

    @Column(length = 50)
    private String condition;

    @Column(name = "shipping_options", columnDefinition = "TEXT")
    private String shippingOptions;

    @Column(name = "allow_pause", nullable = false)
    private boolean allowPause;

    @Column(name = "pauses_used", nullable = false)
    private int pausesUsed;

    @Column(name = "seller_id", nullable = false)
    private UUID sellerId;

    @Column(name = "winner_id")
    private UUID winnerId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;
}
