package dev.mostapha.bidmaster.adapter.in.web.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import dev.mostapha.bidmaster.adapter.in.web.dto.request.ReorderImagesRequestDTO;
import dev.mostapha.bidmaster.adapter.in.web.dto.response.ImageResponseDTO;
import jakarta.validation.Valid;
import dev.mostapha.bidmaster.application.port.in.AuctionUseCase;
import dev.mostapha.bidmaster.application.port.in.ImageUseCase;
import dev.mostapha.bidmaster.application.port.in.UserUseCase;
import dev.mostapha.bidmaster.application.port.out.ImageStoragePort;
import dev.mostapha.bidmaster.domain.model.auction.Auction;
import dev.mostapha.bidmaster.domain.model.image.Image;
import dev.mostapha.bidmaster.domain.model.image.Image.ImageType;
import dev.mostapha.bidmaster.domain.model.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@Slf4j
public class ImageController {

    private final ImageUseCase imageUseCase;
    private final ImageStoragePort imageStoragePort;
    private final AuctionUseCase auctionUseCase;
    private final UserUseCase userUseCase;

    private static final int DEFAULT_URL_EXPIRY = 3600;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageResponseDTO> uploadImage(
            @RequestPart("file") MultipartFile file,
            @RequestParam("type") String type,
            @RequestParam(value = "referenceId", required = false) UUID referenceId,
            @RequestParam(value = "userId", required = false) UUID userId,
            @RequestParam(value = "isPrimary", defaultValue = "false") boolean isPrimary,
            @RequestParam(value = "displayOrder", defaultValue = "0") int displayOrder,
            Principal principal) throws IOException {

        ImageType imageType;
        try {
            imageType = ImageType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tipo de imagen no válido: " + type);
        }

        UUID resolvedUserId = resolveUploaderId(principal, userId);

        if (imageType == ImageType.AUCTION && referenceId != null) {
            Auction auction = auctionUseCase.findAuctionById(referenceId)
                    .orElseThrow(() -> new NoSuchElementException("Subasta no encontrada con ID: " + referenceId));

            if (!auction.getSellerId().equals(resolvedUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        Image image = imageUseCase.storeImage(
                file.getBytes(),
                file.getOriginalFilename(),
                file.getContentType(),
                resolvedUserId,
                imageType,
                referenceId,
                isPrimary,
                displayOrder);

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponseDTO(image));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ImageResponseDTO> getImage(@PathVariable UUID id) {
        return imageUseCase.getImageById(id)
                .map(image -> ResponseEntity.ok(toResponseDTO(image)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/reference/{referenceId}")
    public List<ImageResponseDTO> getImagesByReference(
            @PathVariable UUID referenceId,
            @RequestParam("type") String type) {

        ImageType imageType = ImageType.valueOf(type.toUpperCase());
        return imageUseCase.getImagesByReference(referenceId, imageType).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @PatchMapping("/{id}/primary")
    public ResponseEntity<ImageResponseDTO> setPrimaryImage(
            @PathVariable UUID id,
            @RequestParam(value = "userId", required = false) UUID userId,
            Principal principal) {

        UUID resolvedUserId = resolveUploaderId(principal, userId);
        Image image = imageUseCase.getImageById(id)
                .orElseThrow(() -> new NoSuchElementException("Imagen no encontrada con ID: " + id));

        if (!isOwner(image, resolvedUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Image updated = imageUseCase.setPrimaryImage(id);
        return ResponseEntity.ok(toResponseDTO(updated));
    }

    @PatchMapping("/reference/{referenceId}/order")
    public ResponseEntity<List<ImageResponseDTO>> reorderImages(
            @PathVariable UUID referenceId,
            @RequestParam(value = "type", defaultValue = "AUCTION") String type,
            @RequestParam(value = "userId", required = false) UUID userId,
            @Valid @RequestBody ReorderImagesRequestDTO requestDTO,
            Principal principal) {

        ImageType imageType;
        try {
            imageType = ImageType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tipo de imagen no válido: " + type);
        }

        UUID resolvedUserId = resolveUploaderId(principal, userId);

        if (imageType == ImageType.AUCTION) {
            Auction auction = auctionUseCase.findAuctionById(referenceId)
                    .orElseThrow(() -> new NoSuchElementException("Subasta no encontrada con ID: " + referenceId));
            if (!auction.getSellerId().equals(resolvedUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } else {
            List<Image> existingImages = imageUseCase.getImagesByReference(referenceId, imageType);
            boolean hasForeignImage = existingImages.stream()
                    .anyMatch(img -> img.getUploadedBy() != null && !img.getUploadedBy().equals(resolvedUserId));
            if (hasForeignImage) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        List<ImageResponseDTO> reordered = imageUseCase.reorderImages(referenceId, imageType, requestDTO.getImageIds())
                .stream()
                .map(this::toResponseDTO)
                .toList();
        return ResponseEntity.ok(reordered);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImage(
            @PathVariable UUID id,
            @RequestParam(value = "userId", required = false) UUID userId,
            Principal principal) {

        if (principal != null || userId != null) {
            UUID resolvedUserId = resolveUploaderId(principal, userId);
            Image image = imageUseCase.getImageById(id).orElse(null);
            if (image == null) {
                return ResponseEntity.notFound().build();
            }
            if (!isOwner(image, resolvedUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        boolean deleted = imageUseCase.deleteImage(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @DeleteMapping
    public ResponseEntity<Long> deleteImages(@RequestParam("ids") List<UUID> ids) {
        return ResponseEntity.ok(imageUseCase.deleteImages(ids));
    }

    @GetMapping("/{id}/secure")
    public ResponseEntity<String> getSecureUrl(
            @PathVariable UUID id,
            @RequestParam(value = "expiry", required = false) Integer expirySeconds) {

        int expiry = expirySeconds != null ? expirySeconds : DEFAULT_URL_EXPIRY;
        return imageUseCase.generatePresignedUrl(id, expiry)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/thumbnail/secure")
    public ResponseEntity<String> getSecureThumbnailUrl(
            @PathVariable UUID id,
            @RequestParam(value = "expiry", required = false) Integer expirySeconds) {

        int expiry = expirySeconds != null ? expirySeconds : DEFAULT_URL_EXPIRY;
        return imageUseCase.generateThumbnailPresignedUrl(id, expiry)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/raw")
    public ResponseEntity<byte[]> getRawImage(@PathVariable UUID id) {
        return imageUseCase.getImageById(id)
                .filter(img -> img.getObjectName() != null && img.getBucketName() != null)
                .map(image -> {
                    byte[] data = imageStoragePort.getImage(image.getObjectName(), image.getBucketName());
                    return ResponseEntity.ok()
                            .contentType(MediaType.valueOf(image.getContentType()))
                            .body(data);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/thumbnail/raw")
    public ResponseEntity<byte[]> getRawThumbnail(@PathVariable UUID id) {
        return imageUseCase.getImageById(id)
                .filter(img -> img.getThumbnailObjectName() != null && img.getBucketName() != null)
                .map(image -> {
                    byte[] data = imageStoragePort.getImage(image.getThumbnailObjectName(), image.getBucketName());
                    return ResponseEntity.ok()
                            .contentType(MediaType.valueOf(image.getContentType()))
                            .body(data);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private boolean isOwner(Image image, UUID resolvedUserId) {
        if (image.getType() == ImageType.AUCTION && image.getReferenceId() != null) {
            return auctionUseCase.findAuctionById(image.getReferenceId())
                    .map(auction -> auction.getSellerId().equals(resolvedUserId))
                    .orElseGet(() -> image.getUploadedBy() != null && image.getUploadedBy().equals(resolvedUserId));
        }
        return image.getUploadedBy() != null && image.getUploadedBy().equals(resolvedUserId);
    }

    private UUID resolveUploaderId(Principal principal, UUID userId) {
        if (principal != null && principal.getName() != null) {
            User currentUser = userUseCase.findUserByUsername(principal.getName())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED, "Usuario autenticado no encontrado"));
            return currentUser.getId();
        }
        if (userId != null) {
            return userId;
        }
        throw new IllegalArgumentException("Se requiere un usuario autenticado o el parámetro userId");
    }

    private ImageResponseDTO toResponseDTO(Image image) {
        String imageUrl = image.getUrl();
        String thumbnailUrl = image.getThumbnailUrl();

        if (imageUrl == null && image.getObjectName() != null && image.getBucketName() != null) {
            imageUrl = imageStoragePort.getPublicUrl(image.getObjectName(), image.getBucketName());
        }
        if (thumbnailUrl == null && image.getThumbnailObjectName() != null && image.getBucketName() != null) {
            thumbnailUrl = imageStoragePort.getPublicUrl(image.getThumbnailObjectName(), image.getBucketName());
        }

        if (imageUrl == null || imageUrl.isBlank() || imageUrl.contains("/api/images/raw/")) {
            imageUrl = "/api/images/" + image.getId() + "/raw";
        }
        if (thumbnailUrl == null || thumbnailUrl.isBlank() || thumbnailUrl.contains("/api/images/raw/")) {
            thumbnailUrl = "/api/images/" + image.getId() + "/thumbnail/raw";
        }

        return ImageResponseDTO.fromDomain(image, imageUrl, thumbnailUrl);
    }
}
