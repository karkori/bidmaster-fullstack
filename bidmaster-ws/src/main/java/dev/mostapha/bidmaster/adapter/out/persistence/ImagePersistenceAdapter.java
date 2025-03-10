package dev.mostapha.bidmaster.adapter.out.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import dev.mostapha.bidmaster.application.port.out.ImageRepositoryPort;
import dev.mostapha.bidmaster.domain.model.image.Image;
import dev.mostapha.bidmaster.domain.model.image.Image.ImageType;
import dev.mostapha.bidmaster.infrastructure.persistence.entities.ImageEntity;
import dev.mostapha.bidmaster.infrastructure.persistence.repositories.ImageRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ImagePersistenceAdapter implements ImageRepositoryPort {

    private final ImageRepository imageRepository;
    
    @Override
    public Mono<Image> save(Image image) {
        ImageEntity entity = toEntity(image);
        return imageRepository.save(entity)
                .map(this::toDomainModel);
    }
    
    @Override
    public Mono<Image> findById(UUID id) {
        return imageRepository.findById(id)
                .map(this::toDomainModel);
    }
    
    @Override
    public Flux<Image> findByReferenceIdAndType(UUID referenceId, ImageType type) {
        return imageRepository.findByReferenceIdAndType(referenceId, type.name())
                .map(this::toDomainModel);
    }
    
    @Override
    public Flux<Image> findByUploadedBy(UUID uploadedBy) {
        return imageRepository.findByUploadedBy(uploadedBy)
                .map(this::toDomainModel);
    }
    
    @Override
    public Mono<Boolean> delete(UUID id) {
        return imageRepository.existsById(id)
                .flatMap(exists -> {
                    if (exists) {
                        return imageRepository.deleteById(id)
                                .thenReturn(true);
                    }
                    return Mono.just(false);
                });
    }
    
    @Override
    public Mono<Long> deleteAll(List<UUID> ids) {
        return Flux.fromIterable(ids)
                .flatMap(id -> delete(id)
                        .filter(deleted -> deleted)
                        .map(deleted -> 1L))
                .reduce(0L, Long::sum);
    }
    
    private ImageEntity toEntity(Image image) {
        return ImageEntity.builder()
                .id(image.getId())
                .filename(image.getFilename())
                .originalFilename(image.getOriginalFilename())
                .contentType(image.getContentType())
                .size(image.getSize())
                .bucketName(image.getBucketName())
                .objectName(image.getObjectName())
                .thumbnailObjectName(image.getThumbnailObjectName())
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
                entity.getBucketName(),
                entity.getObjectName(),
                entity.getThumbnailObjectName(),
                entity.getUploadDate(),
                entity.getUploadedBy(),
                ImageType.valueOf(entity.getType()),
                entity.getReferenceId(),
                entity.getVersion()
        );
    }
}
