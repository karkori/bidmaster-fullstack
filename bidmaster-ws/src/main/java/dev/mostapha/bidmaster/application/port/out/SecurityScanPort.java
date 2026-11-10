package dev.mostapha.bidmaster.application.port.out;

/**
 * Puerto de salida para la validación e inspección de seguridad de archivos subidos.
 */
public interface SecurityScanPort {

    boolean validateFileType(byte[] fileData, String declaredContentType);

    boolean scanFile(byte[] fileData);
}
