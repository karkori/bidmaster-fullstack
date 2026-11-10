package dev.mostapha.bidmaster.application.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.mostapha.bidmaster.application.port.in.AuctionUseCase;
import dev.mostapha.bidmaster.application.port.out.AuctionRepository;
import dev.mostapha.bidmaster.application.port.out.BidRepository;
import dev.mostapha.bidmaster.application.port.out.DomainEventPublisherPort;
import dev.mostapha.bidmaster.application.port.out.ImageRepositoryPort;
import dev.mostapha.bidmaster.application.port.out.UserRepository;
import dev.mostapha.bidmaster.domain.event.AuctionStatusChangedEvent;
import dev.mostapha.bidmaster.domain.event.BidPlacedEvent;
import dev.mostapha.bidmaster.domain.model.auction.Auction;
import dev.mostapha.bidmaster.domain.model.auction.AuctionStatus;
import dev.mostapha.bidmaster.domain.model.bid.Bid;
import dev.mostapha.bidmaster.domain.model.bid.BidStatus;
import dev.mostapha.bidmaster.domain.model.image.Image;
import dev.mostapha.bidmaster.domain.model.image.Image.ImageType;
import dev.mostapha.bidmaster.domain.model.user.User;

/**
 * Implementación transaccional del caso de uso de subastas y pujas.
 */
@Service
@Transactional(readOnly = true)
public class AuctionService implements AuctionUseCase {

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final ImageRepositoryPort imageRepositoryPort;
    private final UserRepository userRepository;
    private final DomainEventPublisherPort eventPublisher;

    public AuctionService(
            AuctionRepository auctionRepository,
            BidRepository bidRepository,
            ImageRepositoryPort imageRepositoryPort,
            UserRepository userRepository,
            DomainEventPublisherPort eventPublisher) {
        this.auctionRepository = auctionRepository;
        this.bidRepository = bidRepository;
        this.imageRepositoryPort = imageRepositoryPort;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public Auction createAuction(Auction auction) {
        Auction saved = auctionRepository.save(auction);
        return attachImages(saved);
    }

    @Override
    public Optional<Auction> findAuctionById(UUID id) {
        return auctionRepository.findById(id)
                .map(this::attachImages);
    }

    @Override
    public Optional<Auction> findAuctionBySlug(String slug) {
        return auctionRepository.findBySlug(slug)
                .map(this::attachImages);
    }

    @Override
    public List<Auction> findAuctions(Map<String, Object> filters) {
        return auctionRepository.findByFilters(filters).stream()
                .map(this::attachImages)
                .toList();
    }

    @Override
    public List<Auction> findAuctionsBySellerId(UUID sellerId) {
        return auctionRepository.findBySellerId(sellerId).stream()
                .map(this::attachImages)
                .toList();
    }

    @Override
    @Transactional
    public Auction updateAuction(UUID id, Auction auctionDetails) {
        Auction existingAuction = getAuctionOrThrow(id);

        if (existingAuction.getStatus() != AuctionStatus.DRAFT) {
            throw new IllegalStateException("Solo se pueden actualizar subastas en estado borrador");
        }

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

        return attachImages(auctionRepository.save(existingAuction));
    }

    @Override
    @Transactional
    public Auction publishAuction(UUID id) {
        Auction auction = getAuctionOrThrow(id);
        auction.publish();
        Auction saved = attachImages(auctionRepository.save(auction));
        publishStatusEvent(saved);
        return saved;
    }

    @Override
    @Transactional
    public Auction pauseAuction(UUID id) {
        Auction auction = getAuctionOrThrow(id);
        auction.pause();
        Auction saved = attachImages(auctionRepository.save(auction));
        publishStatusEvent(saved);
        return saved;
    }

    @Override
    @Transactional
    public Auction resumeAuction(UUID id) {
        Auction auction = getAuctionOrThrow(id);
        auction.resume();
        Auction saved = attachImages(auctionRepository.save(auction));
        publishStatusEvent(saved);
        return saved;
    }

    @Override
    @Transactional
    public Auction cancelAuction(UUID id) {
        Auction auction = getAuctionOrThrow(id);
        auction.cancel();

        // Si había una puja ganadora activa, cancelar y liberar su depósito bloqueado
        bidRepository.findWinningBidByAuctionId(id).ifPresent(winningBid -> {
            winningBid.cancel();
            bidRepository.save(winningBid);
            if (winningBid.getDepositBlocked().compareTo(BigDecimal.ZERO) > 0) {
                userRepository.findById(winningBid.getBidderId()).ifPresent(bidder -> {
                    bidder.unblockBalance(winningBid.getDepositBlocked());
                    userRepository.save(bidder);
                });
            }
        });

        Auction saved = attachImages(auctionRepository.save(auction));
        publishStatusEvent(saved);
        return saved;
    }

    @Override
    @Transactional
    public Auction finishAuction(UUID id) {
        Auction auction = getAuctionOrThrow(id);
        auction.finish();

        bidRepository.findWinningBidByAuctionId(id).ifPresent(winningBid -> {
            winningBid.markAsWon();
            bidRepository.save(winningBid);
        });

        Auction saved = attachImages(auctionRepository.save(auction));
        publishStatusEvent(saved);
        return saved;
    }

    @Override
    @Transactional
    public Auction placeBid(UUID auctionId, UUID bidderId, BigDecimal amount) {
        Auction auction = getAuctionOrThrow(auctionId);
        User bidder = userRepository.findById(bidderId)
                .orElseThrow(() -> new NoSuchElementException("Usuario pujador no encontrado"));

        if (!bidder.canBid()) {
            throw new IllegalStateException("El usuario no tiene una cuenta activa habilitada para pujar");
        }

        UUID previousWinnerId = auction.getWinnerId();

        // Validar reglas de dominio de la puja en la subasta
        if (!auction.canBid(bidderId, amount)) {
            throw new IllegalStateException(
                    "No se pudo realizar la puja. Verifique que la subasta esté activa y que el monto supere el mínimo requerido ("
                            + auction.calculateMinNextBid() + ")");
        }

        // Gestionar depósito requerido si aplica
        BigDecimal depositToBlock = auction.getDepositRequired() != null
                ? auction.getDepositRequired()
                : BigDecimal.ZERO;

        // Liberar puja líder anterior y desbloquear su depósito si existía
        bidRepository.findWinningBidByAuctionId(auctionId).ifPresent(previousBid -> {
            previousBid.markAsOutbid();
            bidRepository.save(previousBid);

            if (previousBid.getDepositBlocked().compareTo(BigDecimal.ZERO) > 0) {
                if (previousBid.getBidderId().equals(bidderId)) {
                    bidder.unblockBalance(previousBid.getDepositBlocked());
                } else {
                    userRepository.findById(previousBid.getBidderId()).ifPresent(prevUser -> {
                        prevUser.unblockBalance(previousBid.getDepositBlocked());
                        userRepository.save(prevUser);
                    });
                }
            }
        });

        if (depositToBlock.compareTo(BigDecimal.ZERO) > 0) {
            if (!bidder.blockBalance(depositToBlock)) {
                throw new IllegalStateException("Saldo insuficiente para cubrir el depósito de garantía requerido");
            }
            userRepository.save(bidder);
        }

        // Aplicar la puja en el agregado de dominio Auction
        boolean placed = auction.placeBid(bidderId, amount);
        if (!placed) {
            throw new IllegalStateException("No se pudo registrar la puja en la subasta");
        }

        Auction savedAuction = attachImages(auctionRepository.save(auction));

        // Persistir registro histórico de la puja
        Bid bid = Bid.create(auctionId, bidderId, amount, depositToBlock);
        Bid savedBid = bidRepository.save(bid);

        // Publicar evento de dominio (se emite tras el commit de la transacción)
        eventPublisher.publishBidPlaced(new BidPlacedEvent(
                savedBid.getId(),
                savedAuction.getId(),
                savedAuction.getTitle(),
                bidder.getId(),
                bidder.getUsername(),
                previousWinnerId,
                amount,
                savedAuction.getTotalBids(),
                LocalDateTime.now()));

        return savedAuction;
    }

    @Override
    public List<Bid> findBidsByAuctionId(UUID auctionId) {
        return bidRepository.findByAuctionId(auctionId);
    }

    @Override
    public List<Bid> findBidsByBidderId(UUID bidderId) {
        return bidRepository.findByBidderId(bidderId);
    }

    @Override
    @Transactional
    public void deleteAuction(UUID id) {
        Auction auction = getAuctionOrThrow(id);
        if (auction.getStatus() != AuctionStatus.DRAFT) {
            throw new IllegalStateException("Solo se pueden eliminar subastas en estado borrador");
        }
        imageRepositoryPort.deleteByReferenceIdAndType(id, ImageType.AUCTION);
        auctionRepository.deleteById(id);
    }

    private Auction getAuctionOrThrow(UUID id) {
        return auctionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Subasta no encontrada con ID: " + id));
    }

    private Auction attachImages(Auction auction) {
        List<Image> images = imageRepositoryPort.findByReferenceIdAndType(auction.getId(), ImageType.AUCTION);
        auction.setImages(images);
        return auction;
    }

    private void publishStatusEvent(Auction auction) {
        eventPublisher.publishAuctionStatusChanged(new AuctionStatusChangedEvent(
                auction.getId(),
                auction.getTitle(),
                auction.getStatus(),
                auction.getSellerId(),
                auction.getWinnerId(),
                LocalDateTime.now()));
    }
}
