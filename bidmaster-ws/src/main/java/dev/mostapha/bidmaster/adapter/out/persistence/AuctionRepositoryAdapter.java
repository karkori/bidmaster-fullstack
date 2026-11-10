package dev.mostapha.bidmaster.adapter.out.persistence;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import dev.mostapha.bidmaster.adapter.out.persistence.entity.AuctionEntity;
import dev.mostapha.bidmaster.adapter.out.persistence.repository.JpaAuctionRepository;
import dev.mostapha.bidmaster.application.port.out.AuctionRepository;
import dev.mostapha.bidmaster.domain.model.auction.Auction;
import dev.mostapha.bidmaster.domain.model.auction.AuctionCategory;
import dev.mostapha.bidmaster.domain.model.auction.AuctionStatus;
import jakarta.persistence.criteria.Predicate;

/**
 * Adaptador de salida JPA para el repositorio de subastas.
 * Utiliza JPA Specifications para filtrado dinámico en base de datos (sin filtrado en memoria).
 */
@Component
public class AuctionRepositoryAdapter implements AuctionRepository {

    private final JpaAuctionRepository auctionRepository;

    public AuctionRepositoryAdapter(JpaAuctionRepository auctionRepository) {
        this.auctionRepository = auctionRepository;
    }

    @Override
    public Auction save(Auction auction) {
        AuctionEntity entity = mapToEntity(auction);
        if (!auctionRepository.existsById(entity.getId())) {
            entity.setVersion(null);
        }
        AuctionEntity saved = auctionRepository.save(entity);
        return mapToDomain(saved);
    }

    @Override
    public Optional<Auction> findById(UUID id) {
        return auctionRepository.findById(id).map(this::mapToDomain);
    }

    @Override
    public Optional<Auction> findBySlug(String slug) {
        return auctionRepository.findBySlug(slug).map(this::mapToDomain);
    }

    @Override
    public List<Auction> findByFilters(Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) {
            return auctionRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                    .stream()
                    .map(this::mapToDomain)
                    .toList();
        }

        Specification<AuctionEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filters.get("category") instanceof String category && !category.isBlank()) {
                predicates.add(cb.equal(cb.upper(root.get("category")), category.toUpperCase()));
            }
            if (filters.get("status") instanceof String status && !status.isBlank()) {
                predicates.add(cb.equal(cb.upper(root.get("status")), status.toUpperCase()));
            }
            if (filters.get("sellerId") instanceof UUID sellerId) {
                predicates.add(cb.equal(root.get("sellerId"), sellerId));
            }
            if (filters.get("minPrice") instanceof Number minPrice) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("currentPrice"), BigDecimal.valueOf(minPrice.doubleValue())));
            }
            if (filters.get("maxPrice") instanceof Number maxPrice) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("currentPrice"), BigDecimal.valueOf(maxPrice.doubleValue())));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return auctionRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(this::mapToDomain)
                .toList();
    }

    @Override
    public List<Auction> findBySellerId(UUID sellerId) {
        return auctionRepository.findBySellerId(sellerId).stream()
                .map(this::mapToDomain)
                .toList();
    }

    @Override
    public List<Auction> findByCategory(AuctionCategory category) {
        return auctionRepository.findByCategory(category.name()).stream()
                .map(this::mapToDomain)
                .toList();
    }

    @Override
    public List<Auction> findByStatus(AuctionStatus status) {
        return auctionRepository.findByStatus(status.name()).stream()
                .map(this::mapToDomain)
                .toList();
    }

    @Override
    public List<Auction> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return auctionRepository.findByCurrentPriceBetween(minPrice, maxPrice).stream()
                .map(this::mapToDomain)
                .toList();
    }

    @Override
    public List<Auction> findEndingSoon(int hours) {
        LocalDateTime endBefore = LocalDateTime.now().plus(hours, ChronoUnit.HOURS);
        return auctionRepository.findByStatusAndEndDateBefore(AuctionStatus.ACTIVE.name(), endBefore).stream()
                .map(this::mapToDomain)
                .toList();
    }

    @Override
    public List<Auction> findMostPopular(int limit) {
        return auctionRepository.findByStatusOrderByTotalBidsDesc(
                        AuctionStatus.ACTIVE.name(), PageRequest.of(0, limit))
                .stream()
                .map(this::mapToDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        auctionRepository.deleteById(id);
    }

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
                .depositRequired(auction.getDepositRequired() != null ? auction.getDepositRequired() : BigDecimal.ZERO)
                .startDate(auction.getStartDate())
                .endDate(auction.getEndDate())
                .pauseDate(auction.getPauseDate())
                .pauseDurationSeconds(auction.getPauseDuration() != null ? auction.getPauseDuration().getSeconds() : null)
                .status(auction.getStatus() != null ? auction.getStatus().name() : null)
                .totalBids(auction.getTotalBids())
                .lastBidDate(auction.getLastBidDate())
                .condition(auction.getCondition())
                .shippingOptions(auction.getShippingOptions())
                .allowPause(auction.isAllowPause())
                .pausesUsed(auction.getPausesUsed())
                .sellerId(auction.getSellerId())
                .winnerId(auction.getWinnerId())
                .createdAt(auction.getCreatedAt())
                .updatedAt(auction.getUpdatedAt())
                .version(auction.getVersion())
                .build();
    }

    private Auction mapToDomain(AuctionEntity entity) {
        AuctionStatus status = AuctionStatus.DRAFT;
        if (entity.getStatus() != null) {
            try {
                status = AuctionStatus.valueOf(entity.getStatus());
            } catch (IllegalArgumentException ignored) {
            }
        }

        AuctionCategory category = null;
        if (entity.getCategory() != null) {
            try {
                category = AuctionCategory.valueOf(entity.getCategory());
            } catch (IllegalArgumentException ignored) {
            }
        }

        Duration pauseDuration = entity.getPauseDurationSeconds() != null
                ? Duration.ofSeconds(entity.getPauseDurationSeconds())
                : null;

        return Auction.reconstitute(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getSlug(),
                entity.getInitialPrice(),
                entity.getCurrentPrice(),
                entity.getMinBidIncrement(),
                entity.getDepositRequired() != null ? entity.getDepositRequired() : BigDecimal.ZERO,
                entity.isAllowPause(),
                entity.getPausesUsed(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getPauseDate(),
                pauseDuration,
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
                entity.getVersion() != null ? entity.getVersion() : 0L);
    }
}
