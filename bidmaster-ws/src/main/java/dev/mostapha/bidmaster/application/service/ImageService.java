package dev.mostapha.bidmaster.application.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.mostapha.bidmaster.application.port.in.ImageUseCase;
import dev.mostapha.bidmaster.application.port.out.ImageRepositoryPort;
import dev.mostapha.bidmaster.application.port.out.ImageStoragePort;
import dev.mostapha.bidmaster.application.port.out.SecurityScanPort;
import dev.mostapha.bidmaster.domain.model.image.Image;
import dev.mostapha.bidmaster.domain.model.image.Image.ImageType;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional(readOnly = true)
public class ImageService implements ImageUseCase {

    private final ImageStoragePort imageStoragePort;
    private final SecurityScanPort securityScanPort;
    private final ImageRepositoryPort imageRepositoryPort;

    @Value("${app.images.bucket-name:bidmaster-images}")
    private String defaultBucketName;

    public ImageService(
            ImageStoragePort imageStoragePort,
            SecurityScanPort securityScanPort,
            ImageRepositoryPort imageRepositoryPort) {
        this.imageStoragePort = imageStoragePort;
        this.securityScanPort = securityScanPort;
        this.imageRepositoryPort = imageRepositoryPort;
    }

    @Override
    @Transactional
    public Image storeImage(byte[] imageData, String filename, String contentType,
                            UUID uploadedBy, ImageType type, UUID referenceId) {

        if (imageData == null || imageData.length == 0) {
            throw new IllegalArgumentException("Los datos de imagen no pueden estar vacíos");
        }

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("El tipo de contenido debe ser una imagen");
        }

        if (!securityScanPort.validateFileType(imageData, contentType)) {
            throw new IllegalArgumentException("Tipo de archivo no válido o su firma binaria no coincide con el tipo declarado");
        }

        if (!securityScanPort.scanFile(imageData)) {
            throw new SecurityException("El archivo de imagen está corrupto o no ha pasado la validación de seguridad");
        }

        String objectName = imageStoragePort.storeImage(imageData, filename, contentType, defaultBucketName);
        String thumbnailObjectName = imageStoragePort.storeThumbnail(imageData, filename, contentType, defaultBucketName);

        String publicUrl = imageStoragePort.getPublicUrl(objectName, defaultBucketName);
        String thumbnailUrl = imageStoragePort.getPublicUrl(thumbnailObjectName, defaultBucketName);

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
        image.setUrls(publicUrl, thumbnailUrl);

        Image saved = imageRepositoryPort.save(image);
        log.info("Imagen procesada y almacenada exitosamente: id={}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public Image saveImageMetadata(Image image) {
        return imageRepositoryPort.save(image);
    }

    @Override
    public Optional<Image> getImageById(UUID id) {
        return imageRepositoryPort.findById(id);
    }

    @Override
    public List<Image> getImagesByReference(UUID referenceId, ImageType type) {
        return imageRepositoryPort.findByReferenceIdAndType(referenceId, type);
    }

    @Override
    @Transactional
    public boolean deleteImage(UUID id) {
        return imageRepositoryPort.findById(id)
                .map(image -> {
                    if (image.getObjectName() != null && image.getBucketName() != null) {
                        imageStoragePort.deleteImage(image.getObjectName(), image.getBucketName());
                    }
                    if (image.getThumbnailObjectName() != null && image.getBucketName() != null) {
                        imageStoragePort.deleteImage(image.getThumbnailObjectName(), image.getBucketName());
                    }
                    return imageRepositoryPort.delete(id);
                })
                .orElse(false);
    }

    @Override
    @Transactional
    public long deleteImages(List<UUID> ids) {
        return ids.stream()
                .filter(this::deleteImage)
                .count();
    }

    @Override
    public Optional<String> generatePresignedUrl(UUID id, Integer expirationTimeSeconds) {
        return imageRepositoryPort.findById(id)
                .map(image -> {
                    if (image.getObjectName() == null || image.getBucketName() == null) {
                        return image.getUrl();
                    }
                    return imageStoragePort.getPresignedUrl(
                            image.getObjectName(),
                            image.getBucketName(),
                            expirationTimeSeconds);
                });
    }

    @Override
    public Optional<String> generateThumbnailPresignedUrl(UUID id, Integer expirationTimeSeconds) {
        return imageRepositoryPort.findById(id)
                .map(image -> {
                    if (image.getThumbnailObjectName() == null || image.getBucketName() == null) {
                        return image.getThumbnailUrl() != null ? image.getThumbnailUrl() : image.getUrl();
                    }
                    return imageStoragePort.getPresignedUrl(
                            image.getThumbnailObjectName(),
                            image.getBucketName(),
                            expirationTimeSeconds);
                });
    }

    private String extractFilename(String originalFilename) {
        if (originalFilename == null) {
            return "image.jpg";
        }
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
