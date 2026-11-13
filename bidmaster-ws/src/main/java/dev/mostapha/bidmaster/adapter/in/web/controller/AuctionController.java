package dev.mostapha.bidmaster.adapter.in.web.controller;

import java.security.Principal;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import dev.mostapha.bidmaster.adapter.in.web.dto.request.CreateAuctionRequestDTO;
import dev.mostapha.bidmaster.adapter.in.web.dto.request.PlaceBidRequestDTO;
import dev.mostapha.bidmaster.adapter.in.web.dto.response.AuctionImageDTO;
import dev.mostapha.bidmaster.adapter.in.web.dto.response.AuctionResponseDTO;
import dev.mostapha.bidmaster.adapter.in.web.dto.response.BidResponseDTO;
import dev.mostapha.bidmaster.adapter.out.event.AuctionEventBroadcaster;
import dev.mostapha.bidmaster.application.port.in.AuctionUseCase;
import dev.mostapha.bidmaster.application.port.in.UserUseCase;
import dev.mostapha.bidmaster.domain.model.auction.Auction;
import dev.mostapha.bidmaster.domain.model.auction.AuctionCategory;
import dev.mostapha.bidmaster.domain.model.bid.Bid;
import dev.mostapha.bidmaster.domain.model.user.User;
import jakarta.validation.Valid;

/**
 * Controlador REST para las operaciones relacionadas con subastas, pujas y streaming en tiempo real (SSE).
 */
@RestController
@RequestMapping("/api/auctions")
public class AuctionController {

    private final AuctionUseCase auctionUseCase;
    private final UserUseCase userUseCase;
    private final AuctionEventBroadcaster eventBroadcaster;

    public AuctionController(
            AuctionUseCase auctionUseCase,
            UserUseCase userUseCase,
            AuctionEventBroadcaster eventBroadcaster) {
        this.auctionUseCase = auctionUseCase;
        this.userUseCase = userUseCase;
        this.eventBroadcaster = eventBroadcaster;
    }

    /**
     * Stream SSE en tiempo real para recibir eventos de todas las subastas.
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamAllAuctions() {
        return eventBroadcaster.subscribeToAllAuctions();
    }

    /**
     * Stream SSE en tiempo real para recibir pujas y cambios de estado de una subasta específica.
     */
    @GetMapping(value = "/{id}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamAuction(@PathVariable UUID id) {
        return eventBroadcaster.subscribeToAuction(id);
    }

    /**
     * Crea una nueva subasta.
     */
    @PostMapping
    public ResponseEntity<AuctionResponseDTO> createAuction(
            @Valid @RequestBody CreateAuctionRequestDTO requestDTO,
            Principal principal) {
        User seller = resolveCurrentUser(principal, "vendedor");
        Auction auction = createAuctionFromDTO(requestDTO, seller.getId());
        Auction createdAuction = auctionUseCase.createAuction(auction);
        return ResponseEntity.status(HttpStatus.CREATED).body(enrichAndConvertToDTO(createdAuction));
    }

    /**
     * Obtiene una subasta por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AuctionResponseDTO> getAuctionById(@PathVariable UUID id) {
        return auctionUseCase.findAuctionById(id)
                .map(auction -> ResponseEntity.ok(enrichAndConvertToDTO(auction)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Obtiene una subasta por su slug.
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<AuctionResponseDTO> getAuctionBySlug(@PathVariable String slug) {
        return auctionUseCase.findAuctionBySlug(slug)
                .map(auction -> ResponseEntity.ok(enrichAndConvertToDTO(auction)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Busca subastas con filtros dinámicos.
     */
    @GetMapping
    public List<AuctionResponseDTO> findAuctions(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) UUID sellerId) {

        Map<String, Object> filters = new HashMap<>();
        if (category != null && !category.isBlank()) {
            filters.put("category", category);
        }
        if (status != null && !status.isBlank()) {
            filters.put("status", status);
        }
        if (minPrice != null) {
            filters.put("minPrice", minPrice);
        }
        if (maxPrice != null) {
            filters.put("maxPrice", maxPrice);
        }
        if (sellerId != null) {
            filters.put("sellerId", sellerId);
        }

        return auctionUseCase.findAuctions(filters).stream()
                .map(this::enrichAndConvertToDTO)
                .toList();
    }

    /**
     * Obtiene subastas filtradas por categoría directamente desde el path.
     */
    @GetMapping("/category/{category}")
    public List<AuctionResponseDTO> getAuctionsByCategory(@PathVariable String category) {
        return findAuctions(category, null, null, null, null);
    }

    /**
     * Actualiza una subasta existente (solo en estado DRAFT).
     */
    @PutMapping("/{id}")
    public ResponseEntity<AuctionResponseDTO> updateAuction(
            @PathVariable UUID id,
            @Valid @RequestBody CreateAuctionRequestDTO requestDTO,
            Principal principal) {

        User user = resolveCurrentUser(principal, "vendedor");
        Auction auction = auctionUseCase.findAuctionById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subasta no encontrada"));

        if (!auction.getSellerId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Auction updatedAuction = updateAuctionFromDTO(auction, requestDTO);
        Auction result = auctionUseCase.updateAuction(id, updatedAuction);
        return ResponseEntity.ok(enrichAndConvertToDTO(result));
    }

    /**
     * Publica una subasta (cambia de DRAFT a ACTIVE). Soporta tanto PUT como POST.
     */
    @PutMapping("/{id}/publish")
    public ResponseEntity<AuctionResponseDTO> publishAuctionPut(@PathVariable UUID id, Principal principal) {
        return doPublishAuction(id, principal);
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<AuctionResponseDTO> publishAuctionPost(@PathVariable UUID id, Principal principal) {
        return doPublishAuction(id, principal);
    }

    private ResponseEntity<AuctionResponseDTO> doPublishAuction(UUID id, Principal principal) {
        User user = resolveCurrentUser(principal, "vendedor");
        Auction auction = auctionUseCase.findAuctionById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subasta no encontrada"));

        if (!auction.getSellerId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Auction result = auctionUseCase.publishAuction(id);
        return ResponseEntity.ok(enrichAndConvertToDTO(result));
    }

    /**
     * Pausa una subasta activa (solo el vendedor).
     */
    @PostMapping("/{id}/pause")
    public ResponseEntity<AuctionResponseDTO> pauseAuction(@PathVariable UUID id, Principal principal) {
        User user = resolveCurrentUser(principal, "vendedor");
        Auction auction = auctionUseCase.findAuctionById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subasta no encontrada"));

        if (!auction.getSellerId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Auction result = auctionUseCase.pauseAuction(id);
        return ResponseEntity.ok(enrichAndConvertToDTO(result));
    }

    /**
     * Reanuda una subasta pausada (solo el vendedor).
     */
    @PostMapping("/{id}/resume")
    public ResponseEntity<AuctionResponseDTO> resumeAuction(@PathVariable UUID id, Principal principal) {
        User user = resolveCurrentUser(principal, "vendedor");
        Auction auction = auctionUseCase.findAuctionById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subasta no encontrada"));

        if (!auction.getSellerId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Auction result = auctionUseCase.resumeAuction(id);
        return ResponseEntity.ok(enrichAndConvertToDTO(result));
    }

    /**
     * Cancela una subasta (solo el vendedor).
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<AuctionResponseDTO> cancelAuction(@PathVariable UUID id, Principal principal) {
        User user = resolveCurrentUser(principal, "vendedor");
        Auction auction = auctionUseCase.findAuctionById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subasta no encontrada"));

        if (!auction.getSellerId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Auction result = auctionUseCase.cancelAuction(id);
        return ResponseEntity.ok(enrichAndConvertToDTO(result));
    }

    /**
     * Finaliza una subasta (cambia estado a FINISHED).
     */
    @PostMapping("/{id}/finish")
    public ResponseEntity<AuctionResponseDTO> finishAuction(@PathVariable UUID id, Principal principal) {
        Auction result = auctionUseCase.finishAuction(id);
        return ResponseEntity.ok(enrichAndConvertToDTO(result));
    }

    /**
     * Elimina una subasta (solo en estado DRAFT).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuction(@PathVariable UUID id, Principal principal) {
        User user = resolveCurrentUser(principal, "vendedor");
        Auction auction = auctionUseCase.findAuctionById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subasta no encontrada"));

        if (!auction.getSellerId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        auctionUseCase.deleteAuction(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Realiza una puja transaccional en una subasta.
     */
    @PostMapping("/{id}/bids")
    public ResponseEntity<AuctionResponseDTO> placeBid(
            @PathVariable UUID id,
            @Valid @RequestBody PlaceBidRequestDTO requestDTO,
            Principal principal) {

        User bidder = resolveCurrentUser(principal, "comprador");
        Auction auction = auctionUseCase.findAuctionById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subasta no encontrada"));

        if (auction.getSellerId().equals(bidder.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Auction updatedAuction = auctionUseCase.placeBid(id, bidder.getId(), requestDTO.getAmount());
        return ResponseEntity.ok(enrichAndConvertToDTO(updatedAuction));
    }

    /**
     * Obtiene el historial de pujas de una subasta.
     */
    @GetMapping("/{id}/bids")
    public List<BidResponseDTO> getAuctionBids(@PathVariable UUID id) {
        String title = auctionUseCase.findAuctionById(id).map(Auction::getTitle).orElse(null);
        return auctionUseCase.findBidsByAuctionId(id).stream()
                .map(bid -> convertBidToDTO(bid, title))
                .toList();
    }

    /**
     * Obtiene el historial de pujas realizadas por un usuario específico.
     */
    @GetMapping("/bids/user/{userId}")
    public List<BidResponseDTO> getUserBids(@PathVariable UUID userId) {
        return auctionUseCase.findBidsByBidderId(userId).stream()
                .map(bid -> {
                    String title = auctionUseCase.findAuctionById(bid.getAuctionId())
                            .map(Auction::getTitle)
                            .orElse("Subasta");
                    return convertBidToDTO(bid, title);
                })
                .toList();
    }

    private User resolveCurrentUser(Principal principal, String fallbackUsername) {
        String username = (principal != null && principal.getName() != null)
                ? principal.getName()
                : fallbackUsername;

        return userUseCase.findUserByUsername(username)
                .or(() -> userUseCase.findAllUsers().stream().findFirst())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "El usuario " + username + " no existe"));
    }

    private BidResponseDTO convertBidToDTO(Bid bid, String auctionTitle) {
        String bidderUsername = userUseCase.findUserById(bid.getBidderId())
                .map(User::getUsername)
                .orElse(null);

        return BidResponseDTO.builder()
                .id(bid.getId())
                .auctionId(bid.getAuctionId())
                .auctionTitle(auctionTitle)
                .bidderId(bid.getBidderId())
                .bidderUsername(bidderUsername)
                .amount(bid.getAmount())
                .depositBlocked(bid.getDepositBlocked())
                .status(bid.getStatus().name())
                .createdAt(bid.getCreatedAt())
                .build();
    }

    private Auction createAuctionFromDTO(CreateAuctionRequestDTO dto, UUID sellerId) {
        AuctionCategory category = AuctionCategory.fromString(dto.getCategory());
        if (category == null) {
            throw new IllegalArgumentException("Categoría no válida: " + dto.getCategory());
        }

        Auction auction = Auction.create(
                dto.getTitle(),
                dto.getDescription(),
                category,
                dto.getInitialPrice(),
                dto.getEndDate(),
                sellerId);

        if (dto.getMinBidIncrement() != null) {
            auction.setMinBidIncrement(dto.getMinBidIncrement());
        }
        if (dto.getDepositRequired() != null) {
            auction.setDepositRequired(dto.getDepositRequired());
        }
        auction.setCondition(dto.getCondition());
        auction.setShippingOptions(dto.getShippingOptions());
        auction.setAllowPause(dto.isAllowPause());

        return auction;
    }

    private Auction updateAuctionFromDTO(Auction auction, CreateAuctionRequestDTO dto) {
        if (dto.getTitle() != null) {
            auction.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            auction.setDescription(dto.getDescription());
        }
        if (dto.getCategory() != null) {
            AuctionCategory category = AuctionCategory.fromString(dto.getCategory());
            if (category == null) {
                throw new IllegalArgumentException("Categoría no válida: " + dto.getCategory());
            }
            auction.setCategory(category);
        }
        if (dto.getInitialPrice() != null) {
            auction.setInitialPrice(dto.getInitialPrice());
        }
        if (dto.getMinBidIncrement() != null) {
            auction.setMinBidIncrement(dto.getMinBidIncrement());
        }
        if (dto.getDepositRequired() != null) {
            auction.setDepositRequired(dto.getDepositRequired());
        }
        if (dto.getEndDate() != null) {
            auction.setEndDate(dto.getEndDate());
        }
        if (dto.getCondition() != null) {
            auction.setCondition(dto.getCondition());
        }
        if (dto.getShippingOptions() != null) {
            auction.setShippingOptions(dto.getShippingOptions());
        }
        auction.setAllowPause(dto.isAllowPause());

        return auction;
    }

    private AuctionResponseDTO enrichAndConvertToDTO(Auction auction) {
        AuctionResponseDTO dto = new AuctionResponseDTO();

        dto.setId(auction.getId());
        dto.setTitle(auction.getTitle());
        dto.setDescription(auction.getDescription());
        dto.setSlug(auction.getSlug());

        if (auction.getCategory() != null) {
            dto.setCategory(auction.getCategory().getDisplayName());
        }

        dto.setInitialPrice(auction.getInitialPrice());
        dto.setCurrentPrice(auction.getCurrentPrice());
        dto.setMinBidIncrement(auction.getMinBidIncrement());
        dto.setDepositRequired(auction.getDepositRequired());
        dto.setStartDate(auction.getStartDate());
        dto.setEndDate(auction.getEndDate());

        if (auction.getStatus() != null) {
            dto.setStatus(auction.getStatus().name());
        }

        dto.setTotalBids(auction.getTotalBids());
        dto.setLastBidDate(auction.getLastBidDate());
        dto.setCondition(auction.getCondition());
        dto.setShippingOptions(auction.getShippingOptions());
        dto.setAllowPause(auction.isAllowPause());
        dto.setPausesUsed(auction.getPausesUsed());

        dto.setSellerId(auction.getSellerId());
        if (auction.getSellerId() != null) {
            userUseCase.findUserById(auction.getSellerId())
                    .ifPresent(seller -> dto.setSellerUsername(seller.getUsername()));
        }

        dto.setWinnerId(auction.getWinnerId());
        if (auction.getWinnerId() != null) {
            userUseCase.findUserById(auction.getWinnerId())
                    .ifPresent(winner -> dto.setWinnerUsername(winner.getUsername()));
        }

        Duration timeLeft = auction.getTimeLeft();
        dto.setTimeLeftSeconds(timeLeft != null ? Math.max(0, timeLeft.getSeconds()) : 0);

        if (auction.getImages() != null) {
            List<AuctionImageDTO> imageDTOs = auction.getImages().stream()
                    .map(image -> {
                        String url = image.getUrl();
                        if (url == null || url.isBlank() || url.contains("/api/images/raw/")) {
                            url = "/api/images/" + image.getId() + "/raw";
                        }
                        return new AuctionImageDTO(
                                image.getId(),
                                url,
                                image.getDescription(),
                                image.isPrimary(),
                                image.getDisplayOrder(),
                                image.getFilename(),
                                image.getContentType(),
                                image.getSize());
                    })
                    .toList();
            dto.setImages(imageDTOs);
        } else {
            dto.setImages(Collections.emptyList());
        }

        dto.setCreatedAt(auction.getCreatedAt());
        dto.setUpdatedAt(auction.getUpdatedAt());

        return dto;
    }
}
