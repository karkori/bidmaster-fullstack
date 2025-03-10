package dev.mostapha.bidmaster.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import dev.mostapha.bidmaster.application.port.in.ImageUseCase;
import dev.mostapha.bidmaster.application.port.out.ImageRepositoryPort;
import dev.mostapha.bidmaster.application.port.out.ImageStoragePort;
import dev.mostapha.bidmaster.application.port.out.SecurityScanPort;
import dev.mostapha.bidmaster.domain.model.image.Image;
import dev.mostapha.bidmaster.domain.model.image.Image.ImageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class ImageService implements ImageUseCase {

    private final ImageStoragePort imageStoragePort;
    private final SecurityScanPort securityScanPort;
    private final ImageRepositoryPort imageRepositoryPort;

    public ImageService(ImageStoragePort imageStoragePort, 
                        SecurityScanPort securityScanPort,
                        ImageRepositoryPort imageRepositoryPort) {
        this.imageStoragePort = imageStoragePort;
        this.securityScanPort = securityScanPort;
        this.imageRepositoryPort = imageRepositoryPort;
    }

    @Value("${app.images.bucket-name:bidmaster-images}")
    private String defaultBucketName;

    @Override
    public Mono<Image> storeImage(byte[] imageData, String filename, String contentType,
            UUID uploadedBy, ImageType type, UUID referenceId) {

        // Validación inicial
        if (imageData == null || imageData.length == 0) {
            return Mono.error(new IllegalArgumentException("Los datos de imagen no pueden estar vacíos"));
        }

        if (!contentType.startsWith("image/")) {
            return Mono.error(new IllegalArgumentException("El tipo de contenido debe ser una imagen"));
        }

        // Verificación de seguridad
        return securityScanPort.validateFileType(imageData, contentType)
                .flatMap(isValidType -> {
                    if (!isValidType) {
                        return Mono.error(new IllegalArgumentException(
                                "Tipo de archivo no válido o no coincide con el declarado"));
                    }
                    return securityScanPort.scanFile(imageData);
                })
                .flatMap(isSafe -> {
                    if (!isSafe) {
                        return Mono.error(new SecurityException("El archivo no ha pasado el escaneo de seguridad"));
                    }

                    // Almacenar imagen y miniatura
                    return Mono.zip(
                            imageStoragePort.storeImage(imageData, filename, contentType, defaultBucketName),
                            imageStoragePort.storeThumbnail(imageData, filename, contentType, defaultBucketName));
                })
                .flatMap(tuple -> {
                    String objectName = tuple.getT1();
                    String thumbnailObjectName = tuple.getT2();

                    // Crear entidad de dominio
                    Image image = Image.create(
                            extractFilename(filename),
                            filename,
                            contentType,
                            imageData.length,
                            defaultBucketName,
                            objectName,
                            thumbnailObjectName,
                            uploadedBy,
                            type,
                            referenceId);

                    // Persistir metadatos
                    return imageRepositoryPort.save(image);
                })
                .doOnSuccess(image -> log.info("Imagen procesada y almacenada exitosamente: id={}", image.getId()))
                .doOnError(e -> log.error("Error al procesar y almacenar la imagen", e));
    }

    @Override
    public Mono<Image> getImageById(UUID id) {
        return imageRepositoryPort.findById(id);
    }

    @Override
    public Flux<Image> getImagesByReference(UUID referenceId, ImageType type) {
        return imageRepositoryPort.findByReferenceIdAndType(referenceId, type);
    }

    @Override
    public Mono<Boolean> deleteImage(UUID id) {
        return imageRepositoryPort.findById(id)
                .flatMap(image -> Mono.zip(
                        imageStoragePort.deleteImage(image.getObjectName(), image.getBucketName()),
                        imageStoragePort.deleteImage(image.getThumbnailObjectName(), image.getBucketName()))
                        .flatMap(tuple -> {
                            boolean originalDeleted = tuple.getT1();
                            boolean thumbnailDeleted = tuple.getT2();

                            if (originalDeleted && thumbnailDeleted) {
                                return imageRepositoryPort.delete(id);
                            } else {
                                log.warn(
                                        "No se pudieron eliminar todos los objetos de almacenamiento para la imagen {}",
                                        id);
                                return Mono.just(false);
                            }
                        }))
                .defaultIfEmpty(false);
    }

    @Override
    public Mono<Long> deleteImages(List<UUID> ids) {
        return Flux.fromIterable(ids)
                .flatMap(this::deleteImage)
                .filter(deleted -> deleted)
                .count();
    }

    @Override
    public Mono<String> generatePresignedUrl(UUID id, Integer expirationTimeSeconds) {
        return imageRepositoryPort.findById(id)
                .flatMap(image -> imageStoragePort.getPresignedUrl(
                        image.getObjectName(),
                        image.getBucketName(),
                        expirationTimeSeconds));
    }

    @Override
    public Mono<String> generateThumbnailPresignedUrl(UUID id, Integer expirationTimeSeconds) {
        return imageRepositoryPort.findById(id)
                .flatMap(image -> imageStoragePort.getPresignedUrl(
                        image.getThumbnailObjectName(),
                        image.getBucketName(),
                        expirationTimeSeconds));
    }

    // Métodos auxiliares

    private String extractFilename(String originalFilename) {
        int lastSlashIndex = originalFilename.lastIndexOf('/');
        if (lastSlashIndex >= 0) {
            originalFilename = originalFilename.substring(lastSlashIndex + 1);
        }

        int lastBackslashIndex = originalFilename.lastIndexOf('\\');
        if (lastBackslashIndex >= 0) {
            originalFilename = originalFilename.substring(lastBackslashIndex + 1);
        }

        return originalFilename;
    }
}
