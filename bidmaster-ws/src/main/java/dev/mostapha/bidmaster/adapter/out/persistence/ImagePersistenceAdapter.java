package dev.mostapha.bidmaster.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import dev.mostapha.bidmaster.adapter.out.persistence.entity.ImageEntity;
import dev.mostapha.bidmaster.adapter.out.persistence.repository.JpaImageRepository;
import dev.mostapha.bidmaster.application.port.out.ImageRepositoryPort;
import dev.mostapha.bidmaster.domain.model.image.Image;
import dev.mostapha.bidmaster.domain.model.image.Image.ImageType;
import lombok.RequiredArgsConstructor;

/**
 * Adaptador de salida JPA unificado para la persistencia de imágenes.
 */
@Component
@RequiredArgsConstructor
public class ImagePersistenceAdapter implements ImageRepositoryPort {

    private final JpaImageRepository imageRepository;

    @Override
    public Image save(Image image) {
        ImageEntity entity = toEntity(image);
        if (!imageRepository.existsById(entity.getId())) {
            entity.setVersion(null);
        }
        return toDomainModel(imageRepository.save(entity));
    }

    @Override
    public Optional<Image> findById(UUID id) {
        return imageRepository.findById(id).map(this::toDomainModel);
    }

    @Override
    public List<Image> findByReferenceIdAndType(UUID referenceId, ImageType type) {
        return imageRepository.findByReferenceIdAndTypeOrderByDisplayOrderAsc(referenceId, type.name())
                .stream()
                .map(this::toDomainModel)
                .toList();
    }

    @Override
    public List<Image> findByUploadedBy(UUID uploadedBy) {
        return imageRepository.findByUploadedByOrderByUploadDateDesc(uploadedBy)
                .stream()
                .map(this::toDomainModel)
                .toList();
    }

    @Override
    public boolean delete(UUID id) {
        if (imageRepository.existsById(id)) {
            imageRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public long deleteAll(List<UUID> ids) {
        return ids.stream().filter(this::delete).count();
    }

    @Override
    public void deleteByReferenceIdAndType(UUID referenceId, ImageType type) {
        imageRepository.deleteByReferenceIdAndType(referenceId, type.name());
    }

    private ImageEntity toEntity(Image image) {
        return ImageEntity.builder()
                .id(image.getId())
                .filename(image.getFilename())
                .originalFilename(image.getOriginalFilename())
                .contentType(image.getContentType())
                .size(image.getSize())
                .url(image.getUrl())
                .thumbnailUrl(image.getThumbnailUrl())
                .bucketName(image.getBucketName())
                .objectName(image.getObjectName())
                .thumbnailObjectName(image.getThumbnailObjectName())
                .description(image.getDescription())
                .isPrimary(image.isPrimary())
                .displayOrder(image.getDisplayOrder())
                .uploadDate(image.getUploadDate())
                .uploadedBy(image.getUploadedBy())
                .type(image.getType().name())
                .referenceId(image.getReferenceId())
                .version(image.getVersion())
                .build();
    }

    private Image toDomainModel(ImageEntity entity) {
        return Image.reconstitute(
                entity.getId(),
                entity.getFilename(),
                entity.getOriginalFilename(),
                entity.getContentType(),
                entity.getSize(),
                entity.getUrl(),
                entity.getThumbnailUrl(),
                entity.getBucketName(),
                entity.getObjectName(),
                entity.getThumbnailObjectName(),
                entity.getDescription(),
                entity.isPrimary(),
                entity.getDisplayOrder(),
                entity.getUploadDate(),
                entity.getUploadedBy(),
                ImageType.valueOf(entity.getType()),
                entity.getReferenceId(),
                entity.getVersion());
    }
}
