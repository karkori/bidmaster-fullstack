package dev.mostapha.bidmaster.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import dev.mostapha.bidmaster.adapter.out.persistence.entity.BidEntity;
import dev.mostapha.bidmaster.adapter.out.persistence.repository.JpaBidRepository;
import dev.mostapha.bidmaster.application.port.out.BidRepository;
import dev.mostapha.bidmaster.domain.model.bid.Bid;
import dev.mostapha.bidmaster.domain.model.bid.BidStatus;

/**
 * Adaptador de salida JPA para el repositorio de pujas (Bids).
 */
@Component
public class BidRepositoryAdapter implements BidRepository {

    private final JpaBidRepository bidRepository;

    public BidRepositoryAdapter(JpaBidRepository bidRepository) {
        this.bidRepository = bidRepository;
    }

    @Override
    public Bid save(Bid bid) {
        BidEntity saved = bidRepository.save(mapToEntity(bid));
        return mapToDomain(saved);
    }

    @Override
    public Optional<Bid> findById(UUID id) {
        return bidRepository.findById(id).map(this::mapToDomain);
    }

    @Override
    public List<Bid> findByAuctionId(UUID auctionId) {
        return bidRepository.findByAuctionIdOrderByCreatedAtDesc(auctionId).stream()
                .map(this::mapToDomain)
                .toList();
    }

    @Override
    public List<Bid> findByBidderId(UUID bidderId) {
        return bidRepository.findByBidderIdOrderByCreatedAtDesc(bidderId).stream()
                .map(this::mapToDomain)
                .toList();
    }

    @Override
    public Optional<Bid> findWinningBidByAuctionId(UUID auctionId) {
        return bidRepository.findFirstByAuctionIdAndStatusOrderByAmountDesc(
                        auctionId, BidStatus.WINNING.name())
                .map(this::mapToDomain);
    }

    @Override
    public List<Bid> findByAuctionIdAndStatus(UUID auctionId, BidStatus status) {
        return bidRepository.findByAuctionIdAndStatus(auctionId, status.name()).stream()
                .map(this::mapToDomain)
                .toList();
    }

    private BidEntity mapToEntity(Bid bid) {
        return BidEntity.builder()
                .id(bid.getId())
                .auctionId(bid.getAuctionId())
                .bidderId(bid.getBidderId())
                .amount(bid.getAmount())
                .depositBlocked(bid.getDepositBlocked())
                .status(bid.getStatus().name())
                .createdAt(bid.getCreatedAt())
                .build();
    }

    private Bid mapToDomain(BidEntity entity) {
        return Bid.reconstitute(
                entity.getId(),
                entity.getAuctionId(),
                entity.getBidderId(),
                entity.getAmount(),
                entity.getDepositBlocked(),
                BidStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt());
    }
}
