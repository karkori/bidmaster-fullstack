package dev.mostapha.bidmaster.application.port.out;

import reactor.core.publisher.Mono;

/**
 * Puerto de salida para las operaciones de almacenamiento de imágenes.
 */
public interface ImageStoragePort {
    
    /**
     * Almacena una imagen en el servicio de almacenamiento.
     * 
     * @param imageData Datos binarios de la imagen
     * @param filename Nombre del archivo a usar para almacenar
     * @param contentType Tipo de contenido (MIME type)
     * @param bucketName Nombre del bucket donde almacenar
     * @return Nombre del objeto generado en el almacenamiento
     */
    Mono<String> storeImage(byte[] imageData, String filename, String contentType, String bucketName);
    
    /**
     * Genera una miniatura de la imagen y la almacena.
     * 
     * @param imageData Datos binarios de la imagen original
     * @param filename Nombre del archivo a usar para la miniatura
     * @param contentType Tipo de contenido (MIME type)
     * @param bucketName Nombre del bucket donde almacenar
     * @return Nombre del objeto generado para la miniatura
     */
    Mono<String> storeThumbnail(byte[] imageData, String filename, String contentType, String bucketName);
    
    /**
     * Recupera una imagen del almacenamiento.
     * 
     * @param objectName Nombre del objeto en el almacenamiento
     * @param bucketName Nombre del bucket
     * @return Datos binarios de la imagen
     */
    Mono<byte[]> getImage(String objectName, String bucketName);
    
    /**
     * Elimina una imagen del almacenamiento.
     * 
     * @param objectName Nombre del objeto en el almacenamiento
     * @param bucketName Nombre del bucket
     * @return true si se eliminó correctamente
     */
    Mono<Boolean> deleteImage(String objectName, String bucketName);
    
    /**
     * Genera una URL pública para acceder a la imagen.
     * 
     * @param objectName Nombre del objeto en el almacenamiento
     * @param bucketName Nombre del bucket
     * @return URL pública para acceder a la imagen
     */
    Mono<String> getPublicUrl(String objectName, String bucketName);
    
    /**
     * Genera una URL firmada temporal para acceder a la imagen.
     * Implementación futura para contenido sensible.
     * 
     * @param objectName Nombre del objeto en el almacenamiento
     * @param bucketName Nombre del bucket
     * @param expirationTimeSeconds Tiempo de validez de la URL en segundos
     * @return URL firmada temporal
     */
    Mono<String> getPresignedUrl(String objectName, String bucketName, Integer expirationTimeSeconds);
}
