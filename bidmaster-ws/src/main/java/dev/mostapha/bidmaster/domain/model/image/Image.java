package dev.mostapha.bidmaster.domain.model.image;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Entidad de dominio unificada que representa una imagen en el sistema
 * (tanto almacenada en MinIO S3 como asociada por URL a una subasta, perfil o categoría).
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Image {

    public enum ImageType {
        AUCTION,
        PROFILE,
        CATEGORY,
        GENERAL
    }

    private final UUID id;
    private final String filename;
    private final String originalFilename;
    private final String contentType;
    private final long size;
    private String url;
    private String thumbnailUrl;
    private final String bucketName;
    private final String objectName;
    private final String thumbnailObjectName;
    private String description;
    private boolean isPrimary;
    private int displayOrder;
    private final LocalDateTime uploadDate;
    private final UUID uploadedBy;
    private final ImageType type;
    private final UUID referenceId;
    private final Long version;

    /**
     * Crea una nueva instancia de imagen almacenada en S3/MinIO.
     */
    public static Image create(
            String filename,
            String originalFilename,
            String contentType,
            long size,
            String bucketName,
            String objectName,
            String thumbnailObjectName,
            UUID uploadedBy,
            ImageType type,
            UUID referenceId) {

        return Image.builder()
                .id(UUID.randomUUID())
                .filename(filename)
                .originalFilename(originalFilename)
                .contentType(contentType)
                .size(size)
                .bucketName(bucketName)
                .objectName(objectName)
                .thumbnailObjectName(thumbnailObjectName)
                .isPrimary(false)
                .displayOrder(0)
                .uploadDate(LocalDateTime.now())
                .uploadedBy(uploadedBy)
                .type(type)
                .referenceId(referenceId)
                .version(0L)
                .build();
    }

    /**
     * Crea una imagen asociada a una subasta a partir de una URL directa.
     */
    public static Image createAuctionUrlImage(
            UUID auctionId,
            String url,
            String filename,
            String contentType,
            long size,
            boolean isPrimary,
            int displayOrder,
            String description) {

        if (auctionId == null) {
            throw new IllegalArgumentException("El ID de la subasta no puede ser nulo");
        }
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("La URL de la imagen no puede ser nula o vacía");
        }

        return Image.builder()
                .id(UUID.randomUUID())
                .filename(filename != null ? filename : "image.jpg")
                .originalFilename(filename != null ? filename : "image.jpg")
                .contentType(contentType != null ? contentType : "image/jpeg")
                .size(size)
                .url(url)
                .thumbnailUrl(url)
                .description(description)
                .isPrimary(isPrimary)
                .displayOrder(displayOrder)
                .uploadDate(LocalDateTime.now())
                .type(ImageType.AUCTION)
                .referenceId(auctionId)
                .version(0L)
                .build();
    }

    /**
     * Reconstruye una instancia de imagen desde la persistencia.
     */
    public static Image reconstitute(
            UUID id,
            String filename,
            String originalFilename,
            String contentType,
            long size,
            String url,
            String thumbnailUrl,
            String bucketName,
            String objectName,
            String thumbnailObjectName,
            String description,
            boolean isPrimary,
            int displayOrder,
            LocalDateTime uploadDate,
            UUID uploadedBy,
            ImageType type,
            UUID referenceId,
            Long version) {

        return new Image(
                id,
                filename,
                originalFilename,
                contentType,
                size,
                url,
                thumbnailUrl,
                bucketName,
                objectName,
                thumbnailObjectName,
                description,
                isPrimary,
                displayOrder,
                uploadDate,
                uploadedBy,
                type,
                referenceId,
                version != null ? version : 0L);
    }

    public void setAsPrimary() {
        this.isPrimary = true;
    }

    public void unsetPrimary() {
        this.isPrimary = false;
    }

    public void setDisplayOrder(int displayOrder) {
        if (displayOrder < 0) {
            throw new IllegalArgumentException("El orden de visualización no puede ser negativo");
        }
        this.displayOrder = displayOrder;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setUrls(String url, String thumbnailUrl) {
        this.url = url;
        this.thumbnailUrl = thumbnailUrl;
    }
}
