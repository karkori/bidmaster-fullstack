package dev.mostapha.bidmaster.application.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import dev.mostapha.bidmaster.application.port.in.AuctionUseCase;
import dev.mostapha.bidmaster.application.port.out.AuctionImageRepository;
import dev.mostapha.bidmaster.application.port.out.AuctionRepository;
import dev.mostapha.bidmaster.domain.model.auction.Auction;
import dev.mostapha.bidmaster.domain.model.auction.AuctionStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Implementación del caso de uso de subastas.
 * Contiene la lógica de negocio para la gestión de subastas.
 */
@Service
public class AuctionService implements AuctionUseCase {

    private final AuctionRepository auctionRepository;
    private final AuctionImageRepository auctionImageRepository;

    public AuctionService(AuctionRepository auctionRepository,
            AuctionImageRepository auctionImageRepository) {
        this.auctionRepository = auctionRepository;
        this.auctionImageRepository = auctionImageRepository;
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

    private Mono<Auction> setAuctionImages(Auction auction) {
        // Inicializamos una lista vacía para evitar NullPointerException
        auction.setImages(new ArrayList<>());
        
        // Cargar las imágenes de forma reactiva
        return auctionImageRepository.findByAuctionId(auction.getId())
            .collectList()
            .map(images -> {
                auction.setImages(images);
                return auction;
            })
            .defaultIfEmpty(auction);
    }
}
