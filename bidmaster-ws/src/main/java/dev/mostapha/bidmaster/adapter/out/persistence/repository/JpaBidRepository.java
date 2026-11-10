package dev.mostapha.bidmaster.adapter.out.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.mostapha.bidmaster.adapter.out.persistence.entity.BidEntity;

public interface JpaBidRepository extends JpaRepository<BidEntity, UUID> {

    List<BidEntity> findByAuctionIdOrderByCreatedAtDesc(UUID auctionId);

    List<BidEntity> findByBidderIdOrderByCreatedAtDesc(UUID bidderId);

    Optional<BidEntity> findFirstByAuctionIdAndStatusOrderByAmountDesc(UUID auctionId, String status);

    List<BidEntity> findByAuctionIdAndStatus(UUID auctionId, String status);
}
