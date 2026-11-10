package dev.mostapha.bidmaster.adapter.in.web.controller;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import dev.mostapha.bidmaster.adapter.in.web.dto.response.ImageResponseDTO;
import dev.mostapha.bidmaster.application.port.in.ImageUseCase;
import dev.mostapha.bidmaster.application.port.out.ImageStoragePort;
import dev.mostapha.bidmaster.domain.model.image.Image;
import dev.mostapha.bidmaster.domain.model.image.Image.ImageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@Slf4j
public class ImageController {

    private final ImageUseCase imageUseCase;
    private final ImageStoragePort imageStoragePort;

    private static final int DEFAULT_URL_EXPIRY = 3600;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageResponseDTO> uploadImage(
            @RequestPart("file") MultipartFile file,
            @RequestParam("type") String type,
            @RequestParam(value = "referenceId", required = false) UUID referenceId,
            @RequestParam("userId") UUID userId) throws IOException {

        ImageType imageType;
        try {
            imageType = ImageType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        Image image = imageUseCase.storeImage(
                file.getBytes(),
                file.getOriginalFilename(),
                file.getContentType(),
                userId,
                imageType,
                referenceId);

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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImage(@PathVariable UUID id) {
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

    private ImageResponseDTO toResponseDTO(Image image) {
        String imageUrl = image.getUrl();
        String thumbnailUrl = image.getThumbnailUrl();

        if (imageUrl == null && image.getObjectName() != null && image.getBucketName() != null) {
            imageUrl = imageStoragePort.getPublicUrl(image.getObjectName(), image.getBucketName());
        }
        if (thumbnailUrl == null && image.getThumbnailObjectName() != null && image.getBucketName() != null) {
            thumbnailUrl = imageStoragePort.getPublicUrl(image.getThumbnailObjectName(), image.getBucketName());
        }

        return ImageResponseDTO.fromDomain(image, imageUrl, thumbnailUrl);
    }
}
