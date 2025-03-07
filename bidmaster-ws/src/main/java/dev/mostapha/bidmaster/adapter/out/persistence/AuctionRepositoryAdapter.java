package dev.mostapha.bidmaster.adapter.out.persistence;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import dev.mostapha.bidmaster.application.port.out.AuctionRepository;
import dev.mostapha.bidmaster.domain.model.auction.Auction;
import dev.mostapha.bidmaster.domain.model.auction.AuctionCategory;
import dev.mostapha.bidmaster.domain.model.auction.AuctionStatus;
import dev.mostapha.bidmaster.infrastructure.persistence.entities.AuctionEntity;
import dev.mostapha.bidmaster.infrastructure.persistence.repositories.InfraAuctionRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Adaptador de salida que implementa el puerto de repositorio de subastas.
 * Conecta el dominio con la infraestructura de persistencia.
 */
@Component
public class AuctionRepositoryAdapter implements AuctionRepository {

    private final InfraAuctionRepository auctionRepository;

    public AuctionRepositoryAdapter(InfraAuctionRepository auctionRepository) {
        this.auctionRepository = auctionRepository;
    }

    @Override
    public Mono<Auction> save(Auction auction) {
        return Mono.just(auction)
                .map(this::mapToEntity)
                .flatMap(entity -> {
                    // Verificamos si es una nueva subasta o una actualización
                    if (entity.getCreatedAt() != null && entity.getUpdatedAt() != null &&
                            Math.abs(entity.getCreatedAt().toEpochSecond(java.time.ZoneOffset.UTC) -
                                    entity.getUpdatedAt().toEpochSecond(java.time.ZoneOffset.UTC)) < 10) {
                        // Es una nueva subasta, usamos inserción explícita
                        return auctionRepository.insertAuction(entity)
                                .then(Mono.just(entity));
                    } else {
                        // Es una actualización, usamos el método updateAuction
                        return auctionRepository.updateAuction(entity)
                                .then(auctionRepository.findById(entity.getId()));
                    }
                })
                .map(this::mapToDomain);
    }

    @Override
    public Mono<Auction> findById(UUID id) {
        return auctionRepository.findById(id)
                .map(this::mapToDomain);
    }

    @Override
    public Mono<Auction> findBySlug(String slug) {
        return auctionRepository.findBySlug(slug)
                .map(this::mapToDomain);
    }

    @Override
    public Flux<Auction> findByFilters(Map<String, Object> filters) {
        Flux<AuctionEntity> result = Flux.empty();
        
        // Si no hay filtros, devolver todas las subastas
        if (filters == null || filters.isEmpty()) {
            result = auctionRepository.findAll();
        } else {
            // Aplicar filtros básicos
            if (filters.containsKey("category") && filters.get("category") != null) {
                String category = (String) filters.get("category");
                result = auctionRepository.findByCategory(category.toUpperCase());
            } else if (filters.containsKey("status") && filters.get("status") != null) {
                String status = (String) filters.get("status");
                result = auctionRepository.findByStatus(status.toUpperCase());
            } else if (filters.containsKey("sellerId") && filters.get("sellerId") != null) {
                UUID sellerId = (UUID) filters.get("sellerId");
                result = auctionRepository.findBySellerId(sellerId);
            } else {
                // Si no coincide con los filtros predefinidos, traer todos y filtrar en memoria
                result = auctionRepository.findAll();
            }
        }
        
        // Mapear a dominio y aplicar filtros adicionales en memoria si es necesario
        return result.map(this::mapToDomain)
                .filter(auction -> applyAdditionalFilters(auction, filters));
    }

    @Override
    public Flux<Auction> findBySellerId(UUID sellerId) {
        return auctionRepository.findBySellerId(sellerId)
                .map(this::mapToDomain);
    }

    @Override
    public Flux<Auction> findByCategory(AuctionCategory category) {
        return auctionRepository.findByCategory(category.name())
                .map(this::mapToDomain);
    }

    @Override
    public Flux<Auction> findByStatus(AuctionStatus status) {
        return auctionRepository.findByStatus(status.name())
                .map(this::mapToDomain);
    }

    @Override
    public Flux<Auction> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        // Como no tenemos una consulta específica para rango de precios,
        // traemos todas las subastas y filtramos en memoria
        return auctionRepository.findAll()
                .map(this::mapToDomain)
                .filter(auction -> 
                    auction.getCurrentPrice().compareTo(minPrice) >= 0 && 
                    auction.getCurrentPrice().compareTo(maxPrice) <= 0
                );
    }

    @Override
    public Flux<Auction> findEndingSoon(int hours) {
        LocalDateTime endBefore = LocalDateTime.now().plus(hours, ChronoUnit.HOURS);
        return auctionRepository.findActiveAuctionsEndingBefore(endBefore)
                .map(this::mapToDomain);
    }

    @Override
    public Flux<Auction> findMostPopular(int limit) {
        // Traemos todas las subastas activas, ordenamos por número de pujas
        // y limitamos al número especificado
        return auctionRepository.findByStatus(AuctionStatus.ACTIVE.name())
                .map(this::mapToDomain)
                .sort((a1, a2) -> Integer.compare(a2.getTotalBids(), a1.getTotalBids()))
                .take(limit);
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        return auctionRepository.deleteById(id);
    }

    // Métodos de mapeo entre dominio e infraestructura
    
    /**
     * Convierte una entidad de dominio en una entidad de infraestructura
     */
    private AuctionEntity mapToEntity(Auction auction) {
        return AuctionEntity.builder()
                .id(auction.getId())
                .title(auction.getTitle())
                .description(auction.getDescription())
                .slug(auction.getSlug())
                .category(auction.getCategory() != null ? auction.getCategory().name() : null)
                .initialPrice(auction.getInitialPrice())
                .currentPrice(auction.getCurrentPrice())
                .minBidIncrement(auction.getMinBidIncrement())
                .startDate(auction.getStartDate())
                .endDate(auction.getEndDate())
                .status(auction.getStatus() != null ? auction.getStatus().name() : null)
                .totalBids(auction.getTotalBids())
                .lastBidDate(auction.getLastBidDate())
                .condition(auction.getCondition())
                .shippingOptions(auction.getShippingOptions())
                .allowPause(auction.isAllowPause())
                .sellerId(auction.getSellerId())
                .winnerId(auction.getWinnerId())
                .createdAt(auction.getCreatedAt())
                .updatedAt(auction.getUpdatedAt())
                .build();
    }
    
    /**
     * Convierte una entidad de infraestructura en una entidad de dominio
     */
    private Auction mapToDomain(AuctionEntity entity) {
        // Convertir enums de String a sus valores correspondientes
        AuctionStatus status = null;
        if (entity.getStatus() != null) {
            try {
                status = AuctionStatus.valueOf(entity.getStatus());
            } catch (IllegalArgumentException e) {
                status = AuctionStatus.DRAFT; // Valor por defecto si hay error
            }
        }
        
        AuctionCategory category = null;
        if (entity.getCategory() != null) {
            try {
                category = AuctionCategory.valueOf(entity.getCategory());
            } catch (IllegalArgumentException e) {
                // Dejar como null si hay error
            }
        }
        
        // Usar el método de fábrica reconstitute para recrear la entidad de dominio desde la persistencia
        return Auction.reconstitute(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getSlug(),
                entity.getInitialPrice(),
                entity.getCurrentPrice(),
                entity.getMinBidIncrement(),
                BigDecimal.ZERO, // Valor por defecto para depositRequired que no está en la entidad
                entity.isAllowPause(),
                0, // Valor por defecto para pausesUsed que no está en la entidad
                entity.getStartDate(),
                entity.getEndDate(),
                null, // Valor por defecto para pauseDate que no está en la entidad
                null, // Valor por defecto para pauseDuration que no está en la entidad
                status,
                entity.getSellerId(),
                entity.getWinnerId(),
                entity.getTotalBids(),
                entity.getLastBidDate(),
                category,
                entity.getCondition(),
                entity.getShippingOptions(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                0L // Valor por defecto para version que no está en la entidad
        );
    }
    
    /**
     * Aplica filtros adicionales que no se pudieron aplicar a nivel de base de datos
     */
    private boolean applyAdditionalFilters(Auction auction, Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) {
            return true;
        }
        
        // Filtro por precio mínimo
        if (filters.containsKey("minPrice") && filters.get("minPrice") != null) {
            Double minPrice = (Double) filters.get("minPrice");
            if (auction.getCurrentPrice().doubleValue() < minPrice) {
                return false;
            }
        }
        
        // Filtro por precio máximo
        if (filters.containsKey("maxPrice") && filters.get("maxPrice") != null) {
            Double maxPrice = (Double) filters.get("maxPrice");
            if (auction.getCurrentPrice().doubleValue() > maxPrice) {
                return false;
            }
        }
        
        return true;
    }
}
