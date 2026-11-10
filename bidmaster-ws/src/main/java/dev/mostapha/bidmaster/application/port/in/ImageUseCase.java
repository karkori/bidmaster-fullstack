package dev.mostapha.bidmaster.application.port.in;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import dev.mostapha.bidmaster.domain.model.image.Image;
import dev.mostapha.bidmaster.domain.model.image.Image.ImageType;

/**
 * Puerto de entrada unificado para la gestión de imágenes.
 */
public interface ImageUseCase {

    Image storeImage(byte[] imageData, String filename, String contentType,
                     UUID uploadedBy, ImageType type, UUID referenceId);

    Image saveImageMetadata(Image image);

    Optional<Image> getImageById(UUID id);

    List<Image> getImagesByReference(UUID referenceId, ImageType type);

    boolean deleteImage(UUID id);

    long deleteImages(List<UUID> ids);

    Optional<String> generatePresignedUrl(UUID id, Integer expirationTimeSeconds);

    Optional<String> generateThumbnailPresignedUrl(UUID id, Integer expirationTimeSeconds);
}
