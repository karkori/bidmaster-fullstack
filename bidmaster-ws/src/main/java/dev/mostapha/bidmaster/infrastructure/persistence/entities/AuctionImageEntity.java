package dev.mostapha.bidmaster.infrastructure.persistence.entities;

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
 * Entidad de imagen de subasta para la capa de infraestructura.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("auction_images")
public class AuctionImageEntity {
    
    @Id
    private UUID id;
    
    @Column("auction_id")
    private UUID auctionId;
    
    private String url;
    
    private String description;
    
    @Column("is_primary")
    private boolean isPrimary;
    
    @Column("display_order")
    private int displayOrder;
    
    @Column("created_at")
    private LocalDateTime createdAt;
    
    @Column("updated_at")
    private LocalDateTime updatedAt;
}
