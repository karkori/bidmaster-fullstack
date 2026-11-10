package dev.mostapha.bidmaster.application.port.out;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import dev.mostapha.bidmaster.domain.model.auction.Auction;
import dev.mostapha.bidmaster.domain.model.auction.AuctionCategory;
import dev.mostapha.bidmaster.domain.model.auction.AuctionStatus;

/**
 * Puerto de salida para la persistencia de subastas.
 */
public interface AuctionRepository {

    Auction save(Auction auction);

    Optional<Auction> findById(UUID id);

    Optional<Auction> findBySlug(String slug);

    List<Auction> findByFilters(Map<String, Object> filters);

    List<Auction> findBySellerId(UUID sellerId);

    List<Auction> findByCategory(AuctionCategory category);

    List<Auction> findByStatus(AuctionStatus status);

    List<Auction> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);

    List<Auction> findEndingSoon(int hours);

    List<Auction> findMostPopular(int limit);

    void deleteById(UUID id);
}
