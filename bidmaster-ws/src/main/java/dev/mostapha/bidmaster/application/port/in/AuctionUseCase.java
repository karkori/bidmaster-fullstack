package dev.mostapha.bidmaster.application.port.in;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import dev.mostapha.bidmaster.domain.model.auction.Auction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Puerto de entrada para las operaciones relacionadas con subastas.
 * Define los casos de uso disponibles para interactuar con las subastas.
 */
public interface AuctionUseCase {
    
    /**
     * Crea una nueva subasta en estado borrador
     */
    Mono<Auction> createAuction(Auction auction);
    
    /**
     * Busca una subasta por su ID
     */
    Mono<Auction> findAuctionById(UUID id);
    
    /**
     * Busca una subasta por su slug
     */
    Mono<Auction> findAuctionBySlug(String slug);
    
    /**
     * Busca subastas según filtros aplicados
     * @param filters Mapa de filtros (categoría, estado, rango de precios, etc.)
     */
    Flux<Auction> findAuctions(Map<String, Object> filters);
    
    /**
     * Busca subastas de un vendedor específico
     */
    Flux<Auction> findAuctionsBySellerId(UUID sellerId);
    
    /**
     * Actualiza una subasta existente
     * Solo permitido para subastas en estado DRAFT
     */
    Mono<Auction> updateAuction(UUID id, Auction auctionDetails);
    
    /**
     * Publica una subasta, cambiando su estado de DRAFT a ACTIVE
     */
    Mono<Auction> publishAuction(UUID id);
    
    /**
     * Pausa una subasta activa
     */
    Mono<Auction> pauseAuction(UUID id);
    
    /**
     * Reanuda una subasta pausada
     */
    Mono<Auction> resumeAuction(UUID id);
    
    /**
     * Cancela una subasta
     */
    Mono<Auction> cancelAuction(UUID id);
    
    /**
     * Finaliza una subasta antes de tiempo
     */
    Mono<Auction> finishAuction(UUID id);
    
    /**
     * Realiza una puja en una subasta
     */
    Mono<Auction> placeBid(UUID auctionId, UUID bidderId, BigDecimal amount);
    
    /**
     * Elimina una subasta (solo permitido para subastas en estado DRAFT)
     */
    Mono<Void> deleteAuction(UUID id);
}
