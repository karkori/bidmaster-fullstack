package dev.mostapha.bidmaster.domain.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Evento de dominio emitido cuando se registra exitosamente una nueva puja.
 */
public record BidPlacedEvent(
        UUID bidId,
        UUID auctionId,
        String auctionTitle,
        UUID bidderId,
        String bidderUsername,
        UUID previousWinnerId,
        BigDecimal amount,
        int totalBids,
        LocalDateTime occurredAt
) {
}
