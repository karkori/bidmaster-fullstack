package dev.mostapha.bidmaster.application.service;

import dev.mostapha.bidmaster.application.port.in.AuctionImageUseCase;
import dev.mostapha.bidmaster.application.port.out.AuctionImageRepository;
import dev.mostapha.bidmaster.domain.model.auction.AuctionImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Implementación del caso de uso para manejar imágenes de subastas
 */
@Service
public class AuctionImageService implements AuctionImageUseCase {
    
    private static final Logger log = LoggerFactory.getLogger(AuctionImageService.class);
    private final AuctionImageRepository auctionImageRepository;
    
    public AuctionImageService(AuctionImageRepository auctionImageRepository) {
        this.auctionImageRepository = auctionImageRepository;
    }
    
    @Override
    public Mono<AuctionImage> saveAuctionImage(AuctionImage image) {
        log.info("Guardando imagen para la subasta ID: {}", image.getAuctionId());
        return auctionImageRepository.insertAuctionImage(image);
    }
    
    @Override
    public Flux<AuctionImage> findImagesByAuctionId(UUID auctionId) {
        log.info("Buscando imágenes para la subasta ID: {}", auctionId);
        return auctionImageRepository.findByAuctionId(auctionId);
    }
    
    @Override
    public Mono<Void> deleteAuctionImage(UUID imageId) {
        log.info("Eliminando imagen ID: {}", imageId);
        return auctionImageRepository.deleteById(imageId);
    }
    
    @Override
    public Mono<AuctionImage> setAsPrimaryImage(UUID imageId, UUID auctionId) {
        log.info("Estableciendo imagen ID: {} como principal para la subasta ID: {}", imageId, auctionId);
        
        // Primero desmarcar cualquier imagen principal existente
        return auctionImageRepository.findByAuctionIdAndIsPrimaryTrue(auctionId)
            .flatMap(primaryImage -> {
                primaryImage.unsetPrimary();
                return auctionImageRepository.save(primaryImage);
            })
            .then(auctionImageRepository.findById(imageId))
            .flatMap(image -> {
                image.setAsPrimary();
                return auctionImageRepository.save(image);
            });
    }
}
