package dev.mostapha.bidmaster.application.port.out;

import dev.mostapha.bidmaster.domain.model.auction.AuctionImage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Puerto de salida para las operaciones de persistencia de imágenes de subastas.
 */
public interface AuctionImageRepository {
    
    /**
     * Guarda una nueva imagen o actualiza una existente
     */
    Mono<AuctionImage> save(AuctionImage image);
    
    /**
     * Busca una imagen por su ID
     */
    Mono<AuctionImage> findById(UUID id);
    
    /**
     * Busca todas las imágenes de una subasta
     */
    Flux<AuctionImage> findByAuctionId(UUID auctionId);
    
    /**
     * Busca la imagen principal de una subasta
     */
    Mono<AuctionImage> findPrimaryByAuctionId(UUID auctionId);
    
    /**
     * Busca la primera imagen de una subasta marcada como principal
     */
    Mono<AuctionImage> findByAuctionIdAndIsPrimaryTrue(UUID auctionId);
    
    /**
     * Elimina una imagen
     */
    Mono<Void> deleteById(UUID id);
    
    /**
     * Elimina todas las imágenes de una subasta
     */
    Mono<Void> deleteByAuctionId(UUID auctionId);

    /**
     * Inserta una nueva imagen de subasta
     */
    Mono<AuctionImage> insertAuctionImage(AuctionImage image);
}
