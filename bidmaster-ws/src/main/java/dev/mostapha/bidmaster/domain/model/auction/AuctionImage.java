package dev.mostapha.bidmaster.domain.model.auction;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad de dominio que representa una imagen asociada a una subasta.
 */
public class AuctionImage {
    
    // Identificación
    private final UUID id;
    private final UUID auctionId;
    private String url;
    private String description;
    private boolean isPrimary;
    private int displayOrder;
    
    // Metadatos de la imagen
    private String fileName;
    private String contentType;
    private long fileSize;
    
    // Campos de auditoría
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Constructor privado para crear una nueva imagen de subasta
     */
    private AuctionImage(UUID id, UUID auctionId, String url, String fileName, String contentType, long fileSize) {
        this.id = id != null ? id : UUID.randomUUID();
        this.auctionId = auctionId;
        this.url = url;
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.isPrimary = false;
        this.displayOrder = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }
    
    /**
     * Factory method para crear una nueva imagen de subasta
     */
    public static AuctionImage create(UUID auctionId, String url, String fileName, String contentType, long fileSize) {
        if (auctionId == null) {
            throw new IllegalArgumentException("El ID de la subasta no puede ser nulo");
        }
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("La URL de la imagen no puede ser nula o vacía");
        }
        
        return new AuctionImage(null, auctionId, url, fileName, contentType, fileSize);
    }
    
    /**
     * Factory method para reconstruir una imagen existente desde la persistencia
     */
    public static AuctionImage reconstitute(UUID id, UUID auctionId, String url, String description, 
                                           boolean isPrimary, int displayOrder, String fileName, 
                                           String contentType, long fileSize, 
                                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        AuctionImage image = new AuctionImage(id, auctionId, url, fileName, contentType, fileSize);
        image.description = description;
        image.isPrimary = isPrimary;
        image.displayOrder = displayOrder;
        return image;
    }
    
    // Métodos para establecer/cambiar propiedades
    
    public void setAsPrimary() {
        this.isPrimary = true;
    }
    
    public void unsetPrimary() {
        this.isPrimary = false;
    }
    
    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void setDisplayOrder(int displayOrder) {
        if (displayOrder < 0) {
            throw new IllegalArgumentException("El orden de visualización no puede ser negativo");
        }
        this.displayOrder = displayOrder;
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters
    
    public UUID getId() {
        return id;
    }
    
    public UUID getAuctionId() {
        return auctionId;
    }
    
    public String getUrl() {
        return url;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isPrimary() {
        return isPrimary;
    }
    
    public int getDisplayOrder() {
        return displayOrder;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public String getContentType() {
        return contentType;
    }
    
    public long getFileSize() {
        return fileSize;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
