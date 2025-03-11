package dev.mostapha.bidmaster.application.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import dev.mostapha.bidmaster.application.port.in.AuctionUseCase;
import dev.mostapha.bidmaster.application.port.out.AuctionImageRepository;
import dev.mostapha.bidmaster.application.port.out.AuctionRepository;
import dev.mostapha.bidmaster.domain.model.auction.Auction;
import dev.mostapha.bidmaster.domain.model.auction.AuctionStatus;
import dev.mostapha.bidmaster.domain.model.image.Image;
import dev.mostapha.bidmaster.infrastructure.persistence.repositories.ImageRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Implementación del caso de uso de subastas.
 * Contiene la lógica de negocio para la gestión de subastas.
 */
@Service
public class AuctionService implements AuctionUseCase {

    private final Logger log = LoggerFactory.getLogger(AuctionService.class);

    private final AuctionRepository auctionRepository;
    private final AuctionImageRepository auctionImageRepository;
    private final ImageRepository imageRepository;

    public AuctionService(AuctionRepository auctionRepository, 
                         AuctionImageRepository auctionImageRepository,
                         ImageRepository imageRepository) {
        this.auctionRepository = auctionRepository;
        this.auctionImageRepository = auctionImageRepository;
        this.imageRepository = imageRepository;
    }

    @Override
    public Mono<Auction> createAuction(Auction auction) {
        return auctionRepository.save(auction);
    }

    @Override
    public Mono<Auction> findAuctionById(UUID id) {
        return auctionRepository.findById(id)
                .flatMap(this::setAuctionImages)
                .switchIfEmpty(Mono.error(new RuntimeException("Subasta no encontrada con ID: " + id)));
    }

    @Override
    public Mono<Auction> findAuctionBySlug(String slug) {
        return auctionRepository.findBySlug(slug)
                .flatMap(this::setAuctionImages)
                .switchIfEmpty(Mono.error(new RuntimeException("Subasta no encontrada con slug: " + slug)));
    }

    @Override
    public Flux<Auction> findAuctions(Map<String, Object> filters) {
        return auctionRepository.findByFilters(filters)
                .flatMap(this::setAuctionImages);
    }

    @Override
    public Flux<Auction> findAuctionsBySellerId(UUID sellerId) {
        return auctionRepository.findBySellerId(sellerId)
                .flatMap(this::setAuctionImages);
    }

    @Override
    public Mono<Auction> updateAuction(UUID id, Auction auctionDetails) {
        return findAuctionById(id)
                .flatMap(existingAuction -> {
                    // Verificar que la subasta esté en estado borrador
                    if (existingAuction.getStatus() != AuctionStatus.DRAFT) {
                        return Mono.error(
                                new IllegalStateException("Solo se pueden actualizar subastas en estado borrador"));
                    }

                    // Actualizar los campos permitidos
                    // Nota: Aquí podríamos usar un método de actualización en la entidad
                    // para mantener la lógica de dominio encapsulada
                    if (auctionDetails.getTitle() != null) {
                        existingAuction.setTitle(auctionDetails.getTitle());
                    }
                    if (auctionDetails.getDescription() != null) {
                        existingAuction.setDescription(auctionDetails.getDescription());
                    }
                    if (auctionDetails.getCategory() != null) {
                        existingAuction.setCategory(auctionDetails.getCategory());
                    }
                    if (auctionDetails.getInitialPrice() != null) {
                        existingAuction.setInitialPrice(auctionDetails.getInitialPrice());
                    }
                    if (auctionDetails.getMinBidIncrement() != null) {
                        existingAuction.setMinBidIncrement(auctionDetails.getMinBidIncrement());
                    }
                    if (auctionDetails.getEndDate() != null) {
                        existingAuction.setEndDate(auctionDetails.getEndDate());
                    }
                    if (auctionDetails.getCondition() != null) {
                        existingAuction.setCondition(auctionDetails.getCondition());
                    }
                    if (auctionDetails.getShippingOptions() != null) {
                        existingAuction.setShippingOptions(auctionDetails.getShippingOptions());
                    }

                    return auctionRepository.save(existingAuction);
                });
    }

    @Override
    public Mono<Auction> publishAuction(UUID id) {
        return findAuctionById(id)
                .flatMap(auction -> {
                    try {
                        auction.publish();
                        return auctionRepository.save(auction);
                    } catch (IllegalStateException e) {
                        return Mono.error(e);
                    }
                });
    }

    @Override
    public Mono<Auction> pauseAuction(UUID id) {
        return findAuctionById(id)
                .flatMap(auction -> {
                    try {
                        auction.pause();
                        return auctionRepository.save(auction);
                    } catch (IllegalStateException e) {
                        return Mono.error(e);
                    }
                });
    }

    @Override
    public Mono<Auction> resumeAuction(UUID id) {
        return findAuctionById(id)
                .flatMap(auction -> {
                    try {
                        auction.resume();
                        return auctionRepository.save(auction);
                    } catch (IllegalStateException e) {
                        return Mono.error(e);
                    }
                });
    }

    @Override
    public Mono<Auction> cancelAuction(UUID id) {
        return findAuctionById(id)
                .flatMap(auction -> {
                    try {
                        auction.cancel();
                        return auctionRepository.save(auction);
                    } catch (IllegalStateException e) {
                        return Mono.error(e);
                    }
                });
    }

    @Override
    public Mono<Auction> finishAuction(UUID id) {
        return findAuctionById(id)
                .flatMap(auction -> {
                    try {
                        auction.finish();
                        return auctionRepository.save(auction);
                    } catch (IllegalStateException e) {
                        return Mono.error(e);
                    }
                });
    }

    @Override
    public Mono<Auction> placeBid(UUID auctionId, UUID bidderId, BigDecimal amount) {
        return findAuctionById(auctionId)
                .flatMap(auction -> {
                    boolean success = auction.placeBid(bidderId, amount);
                    if (!success) {
                        return Mono.error(new IllegalStateException("No se pudo realizar la puja"));
                    }
                    return auctionRepository.save(auction);
                });
    }

    @Override
    public Mono<Void> deleteAuction(UUID id) {
        return findAuctionById(id)
                .flatMap(auction -> {
                    if (auction.getStatus() != AuctionStatus.DRAFT) {
                        return Mono.error(
                                new IllegalStateException("Solo se pueden eliminar subastas en estado borrador"));
                    }
                    // Primero eliminamos las imágenes asociadas
                    return auctionImageRepository.deleteByAuctionId(id)
                            .then(auctionRepository.deleteById(id));
                });
    }

    @Override
    public Mono<Boolean> associateImageWithAuction(UUID imageId, UUID auctionId) {
        log.info("Asociando imagen {} con subasta {}", imageId, auctionId);
        
        // Verificar primero que la subasta existe
        return auctionRepository.findById(auctionId)
            .flatMap(auction -> {
                // Actualizar la referencia en la imagen
                return imageRepository.findById(imageId)
                    .flatMap(imageEntity -> {
                        imageEntity.setReferenceId(auctionId);
                        imageEntity.setType("AUCTION");
                        log.info("Actualizando imagen {} para asociarla con subasta {}", imageId, auctionId);
                        return imageRepository.save(imageEntity);
                    })
                    .thenReturn(true)
                    .onErrorResume(e -> {
                        log.error("Error al actualizar la imagen {}: {}", imageId, e.getMessage());
                        return Mono.just(false);
                    });
            })
            .defaultIfEmpty(false)
            .onErrorResume(e -> {
                log.error("No se pudo asociar la imagen: error al buscar la subasta {}: {}", auctionId, e.getMessage());
                return Mono.just(false);
            });
    }

    private Mono<Auction> setAuctionImages(Auction auction) {
        if (auction == null) {
            log.error("La subasta es nula en setAuctionImages");
            return Mono.empty();
        }
        
        log.info("Cargando imágenes para la subasta con ID: {}", auction.getId());
        
        // Inicializamos una lista vacía para evitar NullPointerException
        auction.setImages(new ArrayList<>());
        
        // Cargar las imágenes desde la tabla 'images' usando reference_id y type='AUCTION'
        return imageRepository.findByReferenceIdAndType(auction.getId(), Image.ImageType.AUCTION.name())
            .doOnNext(entity -> {
                log.info("Imagen encontrada para subasta {}: {}", auction.getId(), entity.getObjectName());
                log.info("Detalles de la imagen - ID: {}, URL: {}, Type: {}", 
                    entity.getId(), entity.getObjectName(), entity.getType());
            })
            .map(entity -> {
                // Convertir la entidad a un objeto de dominio AuctionImage
                return dev.mostapha.bidmaster.domain.model.auction.AuctionImage.create(
                    auction.getId(),
                    entity.getObjectName(), // URL como la imagen
                    entity.getFilename(),
                    entity.getContentType(),
                    entity.getSize()
                );
            })
            .collectList()
            .doOnNext(images -> {
                if (images.isEmpty()) {
                    log.warn("No se encontraron imágenes para la subasta {}", auction.getId());
                } else {
                    log.info("Se encontraron {} imágenes para la subasta {}", images.size(), auction.getId());
                    auction.setImages(images);
                    log.info("Imágenes asignadas a la subasta: {}", images.size());
                }
            })
            .thenReturn(auction);
    }
}
