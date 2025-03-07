package dev.mostapha.bidmaster.infrastructure.persistence.repositories;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import dev.mostapha.bidmaster.infrastructure.persistence.entities.AuctionEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repositorio reactivo para la entidad de subasta.
 * Implementa operaciones CRUD básicas y consultas específicas.
 */
public interface InfraAuctionRepository extends ReactiveCrudRepository<AuctionEntity, UUID> {

    /**
     * Buscar subasta por slug
     */
    Mono<AuctionEntity> findBySlug(String slug);

    /**
     * Listar subastas por categoría
     */
    Flux<AuctionEntity> findByCategory(String category);

    /**
     * Listar subastas por estado
     */
    Flux<AuctionEntity> findByStatus(String status);

    /**
     * Listar subastas por vendedor
     */
    Flux<AuctionEntity> findBySellerId(UUID sellerId);

    /**
     * Buscar subastas activas que terminan antes de una fecha determinada
     */
    @Query("SELECT * FROM auctions WHERE status = 'ACTIVE' AND end_date < :endDate")
    Flux<AuctionEntity> findActiveAuctionsEndingBefore(LocalDateTime endDate);

    /**
     * Actualizar el estado de una subasta
     */
    @Modifying
    @Query("UPDATE auctions SET status = :status, updated_at = :updatedAt WHERE id = :id")
    Mono<Void> updateAuctionStatus(UUID id, String status, LocalDateTime updatedAt);
    
    /**
     * Insertar una nueva subasta (para poder controlar mejor la inserción)
     */
    @Modifying
    @Query("INSERT INTO auctions (id, title, description, slug, category, initial_price, current_price, min_bid_increment, start_date, end_date, status, total_bids, last_bid_date, condition, shipping_options, allow_pause, seller_id, winner_id, created_at, updated_at) VALUES (:#{#auction.id}, :#{#auction.title}, :#{#auction.description}, :#{#auction.slug}, :#{#auction.category}, :#{#auction.initialPrice}, :#{#auction.currentPrice}, :#{#auction.minBidIncrement}, :#{#auction.startDate}, :#{#auction.endDate}, :#{#auction.status}, :#{#auction.totalBids}, :#{#auction.lastBidDate}, :#{#auction.condition}, :#{#auction.shippingOptions}, :#{#auction.allowPause}, :#{#auction.sellerId}, :#{#auction.winnerId}, :#{#auction.createdAt}, :#{#auction.updatedAt})")
    Mono<Void> insertAuction(@Param("auction") AuctionEntity auction);

    /**
     * Actualizar una subasta existente
     */
    @Modifying
    @Query("UPDATE auctions SET title = :title, description = :description, slug = :slug, category = :category, initial_price = :initialPrice, current_price = :currentPrice, min_bid_increment = :minBidIncrement, start_date = :startDate, end_date = :endDate, status = :status, total_bids = :totalBids, last_bid_date = :lastBidDate, condition = :condition, shipping_options = :shippingOptions, allow_pause = :allowPause, winner_id = :winnerId, updated_at = :updatedAt WHERE id = :id")
    Mono<Void> updateAuction(AuctionEntity auction);

    /**
     * Actualizar precio actual y ganador tras una puja
     */
    @Modifying
    @Query("UPDATE auctions SET current_price = :price, winner_id = :winnerId, total_bids = total_bids + 1, last_bid_date = :bidDate, updated_at = :updatedAt WHERE id = :id")
    Mono<Void> updateAuctionBid(UUID id, UUID winnerId, Double price, LocalDateTime bidDate, LocalDateTime updatedAt);
}
