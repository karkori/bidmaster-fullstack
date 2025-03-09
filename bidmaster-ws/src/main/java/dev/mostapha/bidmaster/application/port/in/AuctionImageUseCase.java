package dev.mostapha.bidmaster.application.port.in;

import dev.mostapha.bidmaster.domain.model.auction.AuctionImage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Puerto de entrada para las operaciones relacionadas con imágenes de subastas.
 * Define los casos de uso disponibles para interactuar con las imágenes.
 */
public interface AuctionImageUseCase {
    
    /**
     * Guarda una nueva imagen de subasta
     */
    Mono<AuctionImage> saveAuctionImage(AuctionImage image);
    
    /**
     * Busca todas las imágenes asociadas a una subasta
     */
    Flux<AuctionImage> findImagesByAuctionId(UUID auctionId);
    
    /**
     * Elimina una imagen de subasta
     */
    Mono<Void> deleteAuctionImage(UUID imageId);
    
    /**
     * Establece una imagen como principal para una subasta
     */
    Mono<AuctionImage> setAsPrimaryImage(UUID imageId, UUID auctionId);
}
