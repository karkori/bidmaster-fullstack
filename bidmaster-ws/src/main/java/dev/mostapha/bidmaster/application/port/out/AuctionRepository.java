package dev.mostapha.bidmaster.application.port.out;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import dev.mostapha.bidmaster.domain.model.auction.Auction;
import dev.mostapha.bidmaster.domain.model.auction.AuctionCategory;
import dev.mostapha.bidmaster.domain.model.auction.AuctionStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Puerto de salida para las operaciones de persistencia de subastas.
 */
public interface AuctionRepository {
    
    /**
     * Guarda una nueva subasta o actualiza una existente
     */
    Mono<Auction> save(Auction auction);
    
    /**
     * Busca una subasta por su ID
     */
    Mono<Auction> findById(UUID id);
    
    /**
     * Busca una subasta por su slug
     */
    Mono<Auction> findBySlug(String slug);
    
    /**
     * Busca subastas según filtros aplicados
     */
    Flux<Auction> findByFilters(Map<String, Object> filters);
    
    /**
     * Busca subastas por vendedor
     */
    Flux<Auction> findBySellerId(UUID sellerId);
    
    /**
     * Busca subastas por categoría
     */
    Flux<Auction> findByCategory(AuctionCategory category);
    
    /**
     * Busca subastas por estado
     */
    Flux<Auction> findByStatus(AuctionStatus status);
    
    /**
     * Busca subastas por rango de precios
     */
    Flux<Auction> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);
    
    /**
     * Busca subastas próximas a finalizar
     */
    Flux<Auction> findEndingSoon(int hours);
    
    /**
     * Busca subastas más populares (con más pujas)
     */
    Flux<Auction> findMostPopular(int limit);
    
    /**
     * Elimina una subasta
     */
    Mono<Void> deleteById(UUID id);
}
