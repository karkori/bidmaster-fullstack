package dev.mostapha.bidmaster.adapter.in.web.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para representar una puja en el historial de subastas o del usuario.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BidResponseDTO {

    private UUID id;
    private UUID auctionId;
    private String auctionTitle;
    private UUID bidderId;
    private String bidderUsername;
    private BigDecimal amount;
    private BigDecimal depositBlocked;
    private String status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}
