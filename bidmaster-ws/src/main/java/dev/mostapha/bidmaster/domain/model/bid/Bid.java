package dev.mostapha.bidmaster.domain.model.bid;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad de dominio Puja (Bid).
 * Representa una oferta individual realizada por un comprador sobre una subasta.
 */
public class Bid {

    private final UUID id;
    private final UUID auctionId;
    private final UUID bidderId;
    private final BigDecimal amount;
    private final BigDecimal depositBlocked;
    private BidStatus status;
    private final LocalDateTime createdAt;

    private Bid(UUID id, UUID auctionId, UUID bidderId, BigDecimal amount, BigDecimal depositBlocked,
                BidStatus status, LocalDateTime createdAt) {
        this.id = id != null ? id : UUID.randomUUID();
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.amount = amount;
        this.depositBlocked = depositBlocked != null ? depositBlocked : BigDecimal.ZERO;
        this.status = status != null ? status : BidStatus.WINNING;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public static Bid create(UUID auctionId, UUID bidderId, BigDecimal amount, BigDecimal depositBlocked) {
        if (auctionId == null) {
            throw new IllegalArgumentException("El ID de la subasta es obligatorio");
        }
        if (bidderId == null) {
            throw new IllegalArgumentException("El ID del pujador es obligatorio");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto de la puja debe ser mayor que cero");
        }
        return new Bid(null, auctionId, bidderId, amount, depositBlocked, BidStatus.WINNING, LocalDateTime.now());
    }

    public static Bid reconstitute(UUID id, UUID auctionId, UUID bidderId, BigDecimal amount,
                                   BigDecimal depositBlocked, BidStatus status, LocalDateTime createdAt) {
        return new Bid(id, auctionId, bidderId, amount, depositBlocked, status, createdAt);
    }

    public void markAsOutbid() {
        this.status = BidStatus.OUTBID;
    }

    public void markAsWon() {
        this.status = BidStatus.WON;
    }

    public void cancel() {
        this.status = BidStatus.CANCELLED;
    }

    public UUID getId() {
        return id;
    }

    public UUID getAuctionId() {
        return auctionId;
    }

    public UUID getBidderId() {
        return bidderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getDepositBlocked() {
        return depositBlocked;
    }

    public BidStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
