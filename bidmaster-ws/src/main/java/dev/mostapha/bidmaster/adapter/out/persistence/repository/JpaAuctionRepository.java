package dev.mostapha.bidmaster.adapter.out.persistence.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import dev.mostapha.bidmaster.adapter.out.persistence.entity.AuctionEntity;

public interface JpaAuctionRepository extends JpaRepository<AuctionEntity, UUID>, JpaSpecificationExecutor<AuctionEntity> {

    Optional<AuctionEntity> findBySlug(String slug);

    List<AuctionEntity> findByCategory(String category);

    List<AuctionEntity> findByStatus(String status);

    List<AuctionEntity> findBySellerId(UUID sellerId);

    List<AuctionEntity> findByCurrentPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    List<AuctionEntity> findByStatusAndEndDateBefore(String status, LocalDateTime endDate);

    List<AuctionEntity> findByStatusOrderByTotalBidsDesc(String status, Pageable pageable);
}
