package dev.mostapha.bidmaster.adapter.in.web.dto.request;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;

/**
 * DTO para solicitar la reordenación de imágenes asociadas a una referencia.
 */
public class ReorderImagesRequestDTO {

    @NotEmpty(message = "La lista de IDs de imágenes no puede estar vacía")
    private List<UUID> imageIds;

    public ReorderImagesRequestDTO() {
        // Constructor por defecto necesario para deserialización JSON
    }

    public ReorderImagesRequestDTO(List<UUID> imageIds) {
        this.imageIds = imageIds;
    }

    public List<UUID> getImageIds() {
        return imageIds;
    }

    public void setImageIds(List<UUID> imageIds) {
        this.imageIds = imageIds;
    }
}
