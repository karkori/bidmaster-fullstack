package dev.mostapha.bidmaster.application.port.out;

import reactor.core.publisher.Mono;

/**
 * Puerto de salida para el escaneo de seguridad de archivos.
 */
public interface SecurityScanPort {
    
    /**
     * Escanea los datos binarios en busca de malware y otros contenidos maliciosos.
     * 
     * @param fileData Datos binarios del archivo a escanear
     * @return true si el archivo es seguro, false si es peligroso
     */
    Mono<Boolean> scanFile(byte[] fileData);
    
    /**
     * Verifica si el tipo de archivo está permitido según su firma real
     * (más allá de la extensión).
     * 
     * @param fileData Datos binarios del archivo
     * @param declaredContentType Tipo de contenido declarado
     * @return true si el tipo de archivo es válido y permitido
     */
    Mono<Boolean> validateFileType(byte[] fileData, String declaredContentType);
}
