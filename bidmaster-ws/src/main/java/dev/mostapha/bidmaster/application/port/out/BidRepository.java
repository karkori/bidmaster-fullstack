package dev.mostapha.bidmaster.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import dev.mostapha.bidmaster.domain.model.bid.Bid;
import dev.mostapha.bidmaster.domain.model.bid.BidStatus;

/**
 * Puerto de salida para la persistencia de pujas (Bids).
 */
public interface BidRepository {

    Bid save(Bid bid);

    Optional<Bid> findById(UUID id);

    List<Bid> findByAuctionId(UUID auctionId);

    List<Bid> findByBidderId(UUID bidderId);

    Optional<Bid> findWinningBidByAuctionId(UUID auctionId);

    List<Bid> findByAuctionIdAndStatus(UUID auctionId, BidStatus status);
}
