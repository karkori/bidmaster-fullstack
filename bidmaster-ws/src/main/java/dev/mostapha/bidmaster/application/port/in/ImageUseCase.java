package dev.mostapha.bidmaster.application.port.in;

import java.util.List;
import java.util.UUID;

import dev.mostapha.bidmaster.domain.model.image.Image;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Puerto de entrada para las operaciones relacionadas con imágenes.
 */
public interface ImageUseCase {
    
    /**
     * Almacena una imagen y realiza las validaciones de seguridad necesarias.
     * 
     * @param imageData Datos binarios de la imagen
     * @param filename Nombre original del archivo
     * @param contentType Tipo de contenido (MIME type)
     * @param uploadedBy ID del usuario que sube la imagen
     * @param type Tipo de imagen (AUCTION, PROFILE, etc.)
     * @param referenceId ID de referencia (opcional) a la entidad relacionada
     * @return La entidad Image creada
     */
    Mono<Image> storeImage(byte[] imageData, String filename, String contentType, 
                           UUID uploadedBy, Image.ImageType type, UUID referenceId);
    
    /**
     * Recupera una imagen por su ID.
     * 
     * @param id ID de la imagen
     * @return La entidad Image
     */
    Mono<Image> getImageById(UUID id);
    
    /**
     * Recupera todas las imágenes asociadas a una entidad.
     * 
     * @param referenceId ID de la entidad de referencia
     * @param type Tipo de imagen
     * @return Lista de imágenes
     */
    Flux<Image> getImagesByReference(UUID referenceId, Image.ImageType type);
    
    /**
     * Elimina una imagen por su ID.
     * 
     * @param id ID de la imagen a eliminar
     * @return true si se eliminó correctamente
     */
    Mono<Boolean> deleteImage(UUID id);
    
    /**
     * Elimina múltiples imágenes por sus IDs.
     * 
     * @param ids Lista de IDs de imágenes a eliminar
     * @return Número de imágenes eliminadas
     */
    Mono<Long> deleteImages(List<UUID> ids);
    
    /**
     * Genera una URL firmada temporal para acceder directamente a la imagen.
     * 
     * @param id ID de la imagen
     * @param expirationTimeSeconds Tiempo de validez de la URL en segundos
     * @return La URL firmada
     */
    Mono<String> generatePresignedUrl(UUID id, Integer expirationTimeSeconds);
    
    /**
     * Genera una URL firmada temporal para acceder directamente a la miniatura de la imagen.
     * 
     * @param id ID de la imagen
     * @param expirationTimeSeconds Tiempo de validez de la URL en segundos
     * @return La URL firmada para la miniatura
     */
    Mono<String> generateThumbnailPresignedUrl(UUID id, Integer expirationTimeSeconds);
}
