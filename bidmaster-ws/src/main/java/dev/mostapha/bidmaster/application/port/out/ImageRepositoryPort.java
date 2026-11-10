package dev.mostapha.bidmaster.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import dev.mostapha.bidmaster.domain.model.image.Image;
import dev.mostapha.bidmaster.domain.model.image.Image.ImageType;

/**
 * Puerto de salida unificado para la persistencia de metadatos de imágenes.
 */
public interface ImageRepositoryPort {

    Image save(Image image);

    Optional<Image> findById(UUID id);

    List<Image> findByReferenceIdAndType(UUID referenceId, ImageType type);

    List<Image> findByUploadedBy(UUID uploadedBy);

    boolean delete(UUID id);

    long deleteAll(List<UUID> ids);

    void deleteByReferenceIdAndType(UUID referenceId, ImageType type);
}
