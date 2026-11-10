package dev.mostapha.bidmaster.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

import dev.mostapha.bidmaster.domain.model.auction.AuctionStatus;

/**
 * Evento de dominio emitido cuando una subasta cambia de estado.
 */
public record AuctionStatusChangedEvent(
        UUID auctionId,
        String auctionTitle,
        AuctionStatus newStatus,
        UUID sellerId,
        UUID winnerId,
        LocalDateTime occurredAt
) {
}
