package dev.mostapha.bidmaster.application.port.in;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import dev.mostapha.bidmaster.domain.model.auction.Auction;
import dev.mostapha.bidmaster.domain.model.bid.Bid;

/**
 * Puerto de entrada (Use Case) para las operaciones de negocio sobre subastas y pujas.
 */
public interface AuctionUseCase {

    Auction createAuction(Auction auction);

    Optional<Auction> findAuctionById(UUID id);

    Optional<Auction> findAuctionBySlug(String slug);

    List<Auction> findAuctions(Map<String, Object> filters);

    List<Auction> findAuctionsBySellerId(UUID sellerId);

    Auction updateAuction(UUID id, Auction auctionDetails);

    Auction publishAuction(UUID id);

    Auction pauseAuction(UUID id);

    Auction resumeAuction(UUID id);

    Auction cancelAuction(UUID id);

    Auction finishAuction(UUID id);

    Auction placeBid(UUID auctionId, UUID bidderId, BigDecimal amount);

    List<Bid> findBidsByAuctionId(UUID auctionId);

    List<Bid> findBidsByBidderId(UUID bidderId);

    void deleteAuction(UUID id);
}
