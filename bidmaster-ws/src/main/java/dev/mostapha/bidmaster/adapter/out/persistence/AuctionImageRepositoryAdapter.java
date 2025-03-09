package dev.mostapha.bidmaster.adapter.out.persistence;

import java.util.UUID;

import org.springframework.stereotype.Component;

import dev.mostapha.bidmaster.application.port.out.AuctionImageRepository;
import dev.mostapha.bidmaster.domain.model.auction.AuctionImage;
import dev.mostapha.bidmaster.infrastructure.persistence.entities.AuctionImageEntity;
import dev.mostapha.bidmaster.infrastructure.persistence.repositories.InfraAuctionImageRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Adaptador de salida que implementa el puerto de repositorio de imágenes de subastas.
 * Conecta el dominio con la infraestructura de persistencia.
 */
@Component
public class AuctionImageRepositoryAdapter implements AuctionImageRepository {

    private final InfraAuctionImageRepository imageRepository;

    public AuctionImageRepositoryAdapter(InfraAuctionImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    @Override
    public Mono<AuctionImage> save(AuctionImage image) {
        return Mono.just(image)
                .map(this::mapToEntity)
                .flatMap(entity -> {
                    // Verificamos si es una nueva imagen o una actualización
                    if (entity.getCreatedAt() != null && entity.getUpdatedAt() != null &&
                            Math.abs(entity.getCreatedAt().toEpochSecond(java.time.ZoneOffset.UTC) -
                                    entity.getUpdatedAt().toEpochSecond(java.time.ZoneOffset.UTC)) < 10) {
                        // Es una nueva imagen, usamos inserción explícita
                        return imageRepository.insertAuctionImage(entity)
                                .then(Mono.just(entity));
                    } else {
                        // Es una actualización, usamos el método updateAuctionImage
                        return imageRepository.updateAuctionImage(entity)
                                .then(imageRepository.findById(entity.getId()));
                    }
                })
                .map(this::mapToDomain);
    }

    @Override
    public Mono<AuctionImage> findById(UUID id) {
        return imageRepository.findById(id)
                .map(this::mapToDomain);
    }

    @Override
    public Flux<AuctionImage> findByAuctionId(UUID auctionId) {
        return imageRepository.findByAuctionId(auctionId)
                .map(this::mapToDomain);
    }

    @Override
    public Mono<AuctionImage> findPrimaryByAuctionId(UUID auctionId) {
        return imageRepository.findByAuctionIdAndIsPrimaryTrue(auctionId)
                .map(this::mapToDomain);
    }
    
    @Override
    public Mono<AuctionImage> findByAuctionIdAndIsPrimaryTrue(UUID auctionId) {
        return imageRepository.findByAuctionIdAndIsPrimaryTrue(auctionId)
                .map(this::mapToDomain);
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        return imageRepository.deleteById(id);
    }

    @Override
    public Mono<Void> deleteByAuctionId(UUID auctionId) {
        return imageRepository.deleteByAuctionId(auctionId);
    }

    // Métodos de mapeo entre dominio e infraestructura

    /**
     * Convierte una entidad de dominio en una entidad de infraestructura
     */
    private AuctionImageEntity mapToEntity(AuctionImage image) {
        return AuctionImageEntity.builder()
                .id(image.getId())
                .auctionId(image.getAuctionId())
                .url(image.getUrl())
                .description(image.getDescription())
                .isPrimary(image.isPrimary())
                .displayOrder(image.getDisplayOrder())
                .createdAt(image.getCreatedAt())
                .updatedAt(image.getUpdatedAt())
                .build();
    }

    /**
     * Convierte una entidad de infraestructura en una entidad de dominio
     */
    private AuctionImage mapToDomain(AuctionImageEntity entity) {
        // Utilizamos el método de fábrica reconstitute para recrear la entidad desde persistencia
        return AuctionImage.reconstitute(
                entity.getId(),
                entity.getAuctionId(),
                entity.getUrl(),
                entity.getDescription(),
                entity.isPrimary(),
                entity.getDisplayOrder(),
                entity.getUrl().substring(entity.getUrl().lastIndexOf('/') + 1), // Extraemos el nombre del archivo de la URL
                detectContentType(entity.getUrl()), // Detectamos el tipo de contenido basado en la extensión
                0L, // Tamaño de archivo desconocido, ponemos 0 como valor por defecto
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
    
    /**
     * Detecta el tipo de contenido basado en la extensión del archivo
     */
    private String detectContentType(String url) {
        if (url == null) return "application/octet-stream";
        
        String lowercaseUrl = url.toLowerCase();
        if (lowercaseUrl.endsWith(".jpg") || lowercaseUrl.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowercaseUrl.endsWith(".png")) {
            return "image/png";
        } else if (lowercaseUrl.endsWith(".gif")) {
            return "image/gif";
        } else if (lowercaseUrl.endsWith(".webp")) {
            return "image/webp";
        } else if (lowercaseUrl.endsWith(".bmp")) {
            return "image/bmp";
        } else if (lowercaseUrl.endsWith(".svg")) {
            return "image/svg+xml";
        }
        
        return "image/jpeg"; // Tipo por defecto
    }
}
