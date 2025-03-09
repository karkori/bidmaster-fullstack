package dev.mostapha.bidmaster.infrastructure.persistence.repositories;

import java.util.UUID;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import dev.mostapha.bidmaster.infrastructure.persistence.entities.AuctionImageEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repositorio reactivo para la entidad de imagen de subasta.
 * Implementa operaciones CRUD básicas y consultas específicas.
 */
public interface InfraAuctionImageRepository extends ReactiveCrudRepository<AuctionImageEntity, UUID> {
    
    /**
     * Buscar imágenes por ID de subasta
     */
    Flux<AuctionImageEntity> findByAuctionId(UUID auctionId);
    
    /**
     * Buscar la imagen principal de una subasta
     */
    Mono<AuctionImageEntity> findByAuctionIdAndIsPrimaryTrue(UUID auctionId);
    
    /**
     * Eliminar imágenes por ID de subasta
     */
    @Modifying
    @Query("DELETE FROM auction_images WHERE auction_id = :auctionId")
    Mono<Void> deleteByAuctionId(UUID auctionId);
    
    /**
     * Insertar una nueva imagen (para poder controlar mejor la inserción)
     */
    @Modifying
    @Query("INSERT INTO auction_images (id, auction_id, url, description, is_primary, display_order, created_at, updated_at) VALUES (:#{#image.id}, :#{#image.auctionId}, :#{#image.url}, :#{#image.description}, :#{#image.isPrimary}, :#{#image.displayOrder}, :#{#image.createdAt}, :#{#image.updatedAt})")
    Mono<AuctionImageEntity> insertAuctionImage(AuctionImageEntity image);
    
    /**
     * Actualizar una imagen existente
     */
    @Modifying
    @Query("UPDATE auction_images SET url = :url, description = :description, is_primary = :isPrimary, display_order = :displayOrder, updated_at = :updatedAt WHERE id = :id")
    Mono<Void> updateAuctionImage(AuctionImageEntity image);
}
