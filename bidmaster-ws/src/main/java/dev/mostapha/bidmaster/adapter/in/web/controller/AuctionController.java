package dev.mostapha.bidmaster.adapter.in.web.controller;

import java.security.Principal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
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

import dev.mostapha.bidmaster.adapter.in.web.dto.AuctionResponseDTO;
import dev.mostapha.bidmaster.adapter.in.web.dto.CreateAuctionRequestDTO;
import dev.mostapha.bidmaster.adapter.in.web.dto.PlaceBidRequestDTO;
import dev.mostapha.bidmaster.application.port.in.AuctionUseCase;
import dev.mostapha.bidmaster.application.port.in.UserUseCase;
import dev.mostapha.bidmaster.domain.model.auction.Auction;
import dev.mostapha.bidmaster.domain.model.auction.AuctionCategory;
import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Controlador para las operaciones relacionadas con subastas.
 */
@RestController
@RequestMapping("/api/auctions")
public class AuctionController {

    private final AuctionUseCase auctionUseCase;
    private final UserUseCase userUseCase;

    public AuctionController(AuctionUseCase auctionUseCase, UserUseCase userUseCase) {
        this.auctionUseCase = auctionUseCase;
        this.userUseCase = userUseCase;
    }

    /**
     * Crea una nueva subasta
     */
    @PostMapping
    public Mono<ResponseEntity<AuctionResponseDTO>> createAuction(
            @Valid @RequestBody CreateAuctionRequestDTO requestDTO,
            Principal principal) {
        String username = "femtonet"; // TO-DO: remove this when JWT is implemented
        if (principal != null) {
            username = principal.getName();
        }
        // Obtener el usuario actual a partir del token JWT (principal)
        // Por ahora, usaremos su username para buscar el usuario
        return userUseCase.findUserByUsername(username)
                .flatMap(user -> {
                    // Convertir DTO a entidad de dominio
                    Auction auction = createAuctionFromDTO(requestDTO, user.getId());

                    // Persistir la subasta
                    return auctionUseCase.createAuction(auction)
                            .map(createdAuction -> {
                                // Convertir entidad a DTO de respuesta
                                AuctionResponseDTO responseDTO = convertToDTO(createdAuction);
                                // Añadir datos del vendedor
                                responseDTO.setSellerId(user.getId());
                                responseDTO.setSellerUsername(user.getUsername());
                                return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
                            });
                })
                // if user not found
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "El usuario no existe")));
    }

    /**
     * Obtiene una subasta por su ID
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<AuctionResponseDTO>> getAuctionById(@PathVariable String id) {
        return auctionUseCase.findAuctionById(UUID.fromString(id))
                .flatMap(auction -> {
                    // Obtener el usuario vendedor para incluir sus datos
                    return userUseCase.findUserById(auction.getSellerId())
                            .flatMap(seller -> {
                                AuctionResponseDTO dto = convertToDTO(auction);
                                dto.setSellerId(seller.getId());
                                dto.setSellerUsername(seller.getUsername());

                                // Si hay ganador, obtener sus datos
                                if (auction.getWinnerId() != null) {
                                    return userUseCase.findUserById(auction.getWinnerId())
                                            .map(winner -> {
                                                dto.setWinnerId(winner.getId());
                                                dto.setWinnerUsername(winner.getUsername());
                                                return ResponseEntity.ok(dto);
                                            })
                                            .defaultIfEmpty(ResponseEntity.ok(dto));
                                } else {
                                    return Mono.just(ResponseEntity.ok(dto));
                                }
                            });
                })
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    /**
     * Obtiene una subasta por su slug
     */
    @GetMapping("/slug/{slug}")
    public Mono<ResponseEntity<AuctionResponseDTO>> getAuctionBySlug(@PathVariable String slug) {
        return auctionUseCase.findAuctionBySlug(slug)
                .flatMap(auction -> {
                    // Similar al método getAuctionById
                    return userUseCase.findUserById(auction.getSellerId())
                            .flatMap(seller -> {
                                AuctionResponseDTO dto = convertToDTO(auction);
                                dto.setSellerId(seller.getId());
                                dto.setSellerUsername(seller.getUsername());

                                if (auction.getWinnerId() != null) {
                                    return userUseCase.findUserById(auction.getWinnerId())
                                            .map(winner -> {
                                                dto.setWinnerId(winner.getId());
                                                dto.setWinnerUsername(winner.getUsername());
                                                return ResponseEntity.ok(dto);
                                            })
                                            .defaultIfEmpty(ResponseEntity.ok(dto));
                                } else {
                                    return Mono.just(ResponseEntity.ok(dto));
                                }
                            });
                })
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    /**
     * Busca subastas con filtros
     */
    @GetMapping
    public Flux<AuctionResponseDTO> findAuctions(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String sellerId) {

        // Construir mapa de filtros
        Map<String, Object> filters = new HashMap<>();
        if (category != null) {
            filters.put("category", category);
        }
        if (status != null) {
            filters.put("status", status);
        }
        if (minPrice != null) {
            filters.put("minPrice", minPrice);
        }
        if (maxPrice != null) {
            filters.put("maxPrice", maxPrice);
        }
        if (sellerId != null) {
            filters.put("sellerId", UUID.fromString(sellerId));
        }

        return auctionUseCase.findAuctions(filters)
                .flatMap(auction -> {
                    // Obtener el usuario vendedor para cada subasta
                    return userUseCase.findUserById(auction.getSellerId())
                            .flatMap(seller -> {
                                AuctionResponseDTO dto = convertToDTO(auction);
                                dto.setSellerId(seller.getId());
                                dto.setSellerUsername(seller.getUsername());

                                if (auction.getWinnerId() != null) {
                                    return userUseCase.findUserById(auction.getWinnerId())
                                            .map(winner -> {
                                                dto.setWinnerId(winner.getId());
                                                dto.setWinnerUsername(winner.getUsername());
                                                return dto;
                                            })
                                            .defaultIfEmpty(dto);
                                } else {
                                    return Mono.just(dto);
                                }
                            });
                });
    }

    /**
     * Actualiza una subasta existente
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<AuctionResponseDTO>> updateAuction(
            @PathVariable String id,
            @Valid @RequestBody CreateAuctionRequestDTO requestDTO,
            Principal principal) {

        return userUseCase.findUserByUsername(principal.getName())
                .<ResponseEntity<AuctionResponseDTO>>flatMap(user -> {
                    // Verificar que el usuario es el propietario de la subasta
                    return auctionUseCase.findAuctionById(UUID.fromString(id))
                            .<ResponseEntity<AuctionResponseDTO>>flatMap(auction -> {
                                if (!auction.getSellerId().equals(user.getId())) {
                                    return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
                                }

                                // Actualizar los campos de la subasta
                                Auction updatedAuction = updateAuctionFromDTO(auction, requestDTO);

                                return auctionUseCase.updateAuction(UUID.fromString(id), updatedAuction)
                                        .map(result -> {
                                            AuctionResponseDTO dto = convertToDTO(result);
                                            dto.setSellerId(user.getId());
                                            dto.setSellerUsername(user.getUsername());
                                            return ResponseEntity.ok(dto);
                                        });
                            });
                })
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    /**
     * Publica una subasta (cambia estado de DRAFT a ACTIVE)
     */
    @PutMapping("/{id}/publish")
    public Mono<ResponseEntity<AuctionResponseDTO>> publishAuction(
            @PathVariable String id,
            Principal principal) {

        return userUseCase.findUserByUsername(principal.getName())
                .<ResponseEntity<AuctionResponseDTO>>flatMap(user -> {
                    // Verificar que el usuario es el propietario de la subasta
                    return auctionUseCase.findAuctionById(UUID.fromString(id))
                            .<ResponseEntity<AuctionResponseDTO>>flatMap(auction -> {
                                if (!auction.getSellerId().equals(user.getId())) {
                                    return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
                                }

                                return auctionUseCase.publishAuction(UUID.fromString(id))
                                        .<ResponseEntity<AuctionResponseDTO>>map(result -> {
                                            AuctionResponseDTO dto = convertToDTO(result);
                                            dto.setSellerId(user.getId());
                                            dto.setSellerUsername(user.getUsername());
                                            return ResponseEntity.ok(dto);
                                        })
                                        .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().build()));
                            });
                })
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    /**
     * Elimina una subasta (solo en estado DRAFT)
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteAuction(
            @PathVariable String id,
            Principal principal) {

        return userUseCase.findUserByUsername(principal.getName())
                .<ResponseEntity<Void>>flatMap(user -> {
                    // Verificar que el usuario es el propietario de la subasta
                    return auctionUseCase.findAuctionById(UUID.fromString(id))
                            .<ResponseEntity<Void>>flatMap(auction -> {
                                if (!auction.getSellerId().equals(user.getId())) {
                                    return Mono.just(ResponseEntity.<Void>status(HttpStatus.FORBIDDEN).build());
                                }

                                return auctionUseCase.deleteAuction(UUID.fromString(id))
                                        .then(Mono.just(ResponseEntity.noContent().<Void>build()));
                            });
                })
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    /**
     * Realiza una puja en una subasta
     */
    @PostMapping("/{id}/bids")
    public Mono<ResponseEntity<AuctionResponseDTO>> placeBid(
            @PathVariable String id,
            @Valid @RequestBody PlaceBidRequestDTO requestDTO,
            Principal principal) {

        return userUseCase.findUserByUsername(principal.getName())
                .<ResponseEntity<AuctionResponseDTO>>flatMap(user -> {
                    // Verificar que el usuario no es el propietario de la subasta
                    return auctionUseCase.findAuctionById(UUID.fromString(id))
                            .<ResponseEntity<AuctionResponseDTO>>flatMap(auction -> {
                                if (auction.getSellerId().equals(user.getId())) {
                                    return Mono.just(ResponseEntity
                                            .status(HttpStatus.FORBIDDEN)
                                            .body(null)); // El vendedor no puede pujar en su propia subasta
                                }

                                return auctionUseCase
                                        .placeBid(UUID.fromString(id), user.getId(), requestDTO.getAmount())
                                        .<ResponseEntity<AuctionResponseDTO>>flatMap(updatedAuction -> {
                                            AuctionResponseDTO dto = convertToDTO(updatedAuction);

                                            // Obtener datos del vendedor
                                            return userUseCase.findUserById(updatedAuction.getSellerId())
                                                    .<ResponseEntity<AuctionResponseDTO>>map(seller -> {
                                                        dto.setSellerId(seller.getId());
                                                        dto.setSellerUsername(seller.getUsername());

                                                        // Agregar datos del ganador actual
                                                        if (updatedAuction.getWinnerId() != null &&
                                                                updatedAuction.getWinnerId().equals(user.getId())) {
                                                            dto.setWinnerId(user.getId());
                                                            dto.setWinnerUsername(user.getUsername());
                                                        }

                                                        return ResponseEntity.ok(dto);
                                                    });
                                        })
                                        .onErrorResume(e -> {
                                            if (e instanceof IllegalStateException) {
                                                return Mono.just(ResponseEntity
                                                        .status(HttpStatus.BAD_REQUEST)
                                                        .body(null));
                                            }
                                            return Mono.just(ResponseEntity
                                                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                    .body(null));
                                        });
                            });
                })
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    // Métodos auxiliares para convertir entre entidades y DTOs

    /**
     * Crea una entidad Auction a partir del DTO de petición
     */
    private Auction createAuctionFromDTO(CreateAuctionRequestDTO dto, UUID sellerId) {
        // Convertir string de categoría a enum
        AuctionCategory category;
        try {
            category = AuctionCategory.valueOf(dto.getCategory().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Categoría no válida: " + dto.getCategory());
        }

        // Crear la subasta con los datos básicos
        Auction auction = Auction.create(
                dto.getTitle(),
                dto.getDescription(),
                category,
                dto.getInitialPrice(),
                dto.getEndDate(),
                sellerId);

        // Establecer campos opcionales
        if (dto.getMinBidIncrement() != null) {
            auction.setMinBidIncrement(dto.getMinBidIncrement());
        }
        auction.setCondition(dto.getCondition());
        auction.setShippingOptions(dto.getShippingOptions());
        auction.setAllowPause(dto.isAllowPause());

        return auction;
    }

    /**
     * Actualiza una entidad Auction existente con los datos del DTO
     */
    private Auction updateAuctionFromDTO(Auction auction, CreateAuctionRequestDTO dto) {
        if (dto.getTitle() != null) {
            auction.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            auction.setDescription(dto.getDescription());
        }
        if (dto.getCategory() != null) {
            try {
                AuctionCategory category = AuctionCategory.valueOf(dto.getCategory().toUpperCase());
                auction.setCategory(category);
            } catch (IllegalArgumentException e) {
                // Ignorar categoría inválida en actualización
            }
        }
        if (dto.getInitialPrice() != null) {
            auction.setInitialPrice(dto.getInitialPrice());
        }
        if (dto.getMinBidIncrement() != null) {
            auction.setMinBidIncrement(dto.getMinBidIncrement());
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

    /**
     * Convierte una entidad Auction a un DTO de respuesta
     */
    private AuctionResponseDTO convertToDTO(Auction auction) {
        AuctionResponseDTO dto = new AuctionResponseDTO();

        // Datos básicos
        dto.setId(auction.getId());
        dto.setTitle(auction.getTitle());
        dto.setDescription(auction.getDescription());
        dto.setSlug(auction.getSlug());

        // Gestionar posibles nulos
        if (auction.getCategory() != null) {
            dto.setCategory(auction.getCategory().getDisplayName());
        }

        dto.setInitialPrice(auction.getInitialPrice());
        dto.setCurrentPrice(auction.getCurrentPrice());
        dto.setMinBidIncrement(auction.getMinBidIncrement());
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

        // IDs
        dto.setSellerId(auction.getSellerId());
        dto.setWinnerId(auction.getWinnerId());

        // Calcular tiempo restante
        Duration timeLeft = auction.getTimeLeft();
        if (timeLeft != null) {
            dto.setTimeLeftSeconds(timeLeft.getSeconds());
        } else {
            dto.setTimeLeftSeconds(0);
        }

        // Por defecto, lista vacía de imágenes
        dto.setImages(new ArrayList<>());

        // Metadatos
        dto.setCreatedAt(auction.getCreatedAt());
        dto.setUpdatedAt(auction.getUpdatedAt());

        return dto;
    }
}
