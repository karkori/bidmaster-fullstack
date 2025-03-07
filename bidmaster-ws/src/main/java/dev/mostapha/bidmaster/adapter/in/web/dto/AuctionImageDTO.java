package dev.mostapha.bidmaster.adapter.in.web.dto;

import java.util.UUID;

/**
 * DTO para la información de una imagen de subasta
 */
public class AuctionImageDTO {
    
    private UUID id;
    private String url;
    private String description;
    private boolean isPrimary;
    private int displayOrder;
    private String fileName;
    private String contentType;
    private long fileSize;
    
    // Constructores
    
    public AuctionImageDTO() {
        // Constructor por defecto necesario para la serialización
    }
    
    // Getters y Setters
    
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setPrimary(boolean primary) {
        isPrimary = primary;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
}
