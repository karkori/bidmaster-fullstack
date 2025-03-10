package dev.mostapha.bidmaster.domain.model.image;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Entidad de dominio que representa una imagen en el sistema.
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
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
    private final String bucketName;
    private final String objectName;
    private final String thumbnailObjectName;
    private final LocalDateTime uploadDate;
    private final UUID uploadedBy;
    private final ImageType type;
    private final UUID referenceId;
    private final Long version;
    
    /**
     * Crea una nueva instancia de imagen.
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
        
        return new Image(
                UUID.randomUUID(),
                filename,
                originalFilename,
                contentType,
                size,
                bucketName,
                objectName,
                thumbnailObjectName,
                LocalDateTime.now(),
                uploadedBy,
                type,
                referenceId,
                0L);
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
            String bucketName,
            String objectName,
            String thumbnailObjectName,
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
                bucketName,
                objectName,
                thumbnailObjectName,
                uploadDate,
                uploadedBy,
                type,
                referenceId,
                version);
    }
}
