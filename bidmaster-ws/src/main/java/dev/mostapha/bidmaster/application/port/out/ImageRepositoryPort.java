package dev.mostapha.bidmaster.application.port.out;

import java.util.List;
import java.util.UUID;

import dev.mostapha.bidmaster.domain.model.image.Image;
import dev.mostapha.bidmaster.domain.model.image.Image.ImageType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Puerto de salida para la persistencia de metadatos de imágenes.
 */
public interface ImageRepositoryPort {
    
    /**
     * Guarda los metadatos de una imagen.
     * 
     * @param image La entidad Image a guardar
     * @return La entidad Image guardada
     */
    Mono<Image> save(Image image);
    
    /**
     * Encuentra una imagen por su ID.
     * 
     * @param id ID de la imagen
     * @return La entidad Image si existe
     */
    Mono<Image> findById(UUID id);
    
    /**
     * Busca imágenes por su referencia y tipo.
     * 
     * @param referenceId ID de la entidad de referencia
     * @param type Tipo de imagen
     * @return Lista de imágenes asociadas
     */
    Flux<Image> findByReferenceIdAndType(UUID referenceId, ImageType type);
    
    /**
     * Busca imágenes por su propietario.
     * 
     * @param uploadedBy ID del usuario que subió las imágenes
     * @return Lista de imágenes del usuario
     */
    Flux<Image> findByUploadedBy(UUID uploadedBy);
    
    /**
     * Elimina una imagen por su ID.
     * 
     * @param id ID de la imagen
     * @return true si se eliminó correctamente
     */
    Mono<Boolean> delete(UUID id);
    
    /**
     * Elimina múltiples imágenes por sus IDs.
     * 
     * @param ids Lista de IDs de imágenes
     * @return Número de imágenes eliminadas
     */
    Mono<Long> deleteAll(List<UUID> ids);
}
