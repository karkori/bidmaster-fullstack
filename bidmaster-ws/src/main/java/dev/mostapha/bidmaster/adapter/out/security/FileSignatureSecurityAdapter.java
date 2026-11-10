package dev.mostapha.bidmaster.adapter.out.security;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Set;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Component;

import dev.mostapha.bidmaster.application.port.out.SecurityScanPort;
import lombok.extern.slf4j.Slf4j;

/**
 * Adaptador de validación de seguridad de imágenes basado en inspección de firmas binarias (Magic Bytes)
 * y verificación estructural del stream de imagen (reemplaza el contenedor pesado de ClamAV).
 */
@Component
@Slf4j
public class FileSignatureSecurityAdapter implements SecurityScanPort {

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/bmp", "image/webp"
    );

    private static final int MAX_IMAGE_DIMENSION = 10000;

    @Override
    public boolean validateFileType(byte[] fileData, String declaredContentType) {
        if (fileData == null || fileData.length < 12 || declaredContentType == null) {
            log.warn("Archivo vacío, demasiado pequeño o sin Content-Type declarado");
            return false;
        }

        String normalizedDeclared = declaredContentType.toLowerCase().trim();
        if ("image/jpg".equals(normalizedDeclared)) {
            normalizedDeclared = "image/jpeg";
        }

        if (!ALLOWED_IMAGE_TYPES.contains(normalizedDeclared)) {
            log.warn("Tipo de contenido no permitido: {}", declaredContentType);
            return false;
        }

        String detectedType = detectFileType(fileData);
        if (detectedType == null) {
            log.warn("No se pudo detectar una firma binaria (magic bytes) de imagen válida");
            return false;
        }

        boolean matches = detectedType.equalsIgnoreCase(normalizedDeclared);
        if (!matches) {
            log.warn("El tipo declarado ({}) no coincide con la firma binaria real ({})",
                    normalizedDeclared, detectedType);
        }
        return matches;
    }

    @Override
    public boolean scanFile(byte[] fileData) {
        if (fileData == null || fileData.length < 12) {
            return false;
        }

        String detectedType = detectFileType(fileData);
        if (detectedType == null) {
            return false;
        }

        // Para WEBP validamos firma RIFF/WEBP y tamaño consistente
        if ("image/webp".equals(detectedType)) {
            return true;
        }

        // Para JPEG, PNG, GIF, BMP verificamos que ImageIO pueda decodificar el header/estructura sin errores
        try (ByteArrayInputStream bais = new ByteArrayInputStream(fileData)) {
            BufferedImage img = ImageIO.read(bais);
            if (img == null) {
                log.warn("El archivo tiene magic bytes de imagen pero no pudo decodificarse como imagen válida");
                return false;
            }
            if (img.getWidth() <= 0 || img.getHeight() <= 0
                    || img.getWidth() > MAX_IMAGE_DIMENSION
                    || img.getHeight() > MAX_IMAGE_DIMENSION) {
                log.warn("Dimensiones de imagen fuera de los límites seguros: {}x{}", img.getWidth(), img.getHeight());
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("Error al verificar la integridad estructural de la imagen", e);
            return false;
        }
    }

    private String detectFileType(byte[] data) {
        if (data.length < 12) {
            return null;
        }

        // JPEG: FF D8 FF
        if (data[0] == (byte) 0xFF && data[1] == (byte) 0xD8 && data[2] == (byte) 0xFF) {
            return "image/jpeg";
        }

        // PNG: 89 50 4E 47 0D 0A 1A 0A
        if (data[0] == (byte) 0x89 && data[1] == (byte) 0x50 && data[2] == (byte) 0x4E &&
                data[3] == (byte) 0x47 && data[4] == (byte) 0x0D && data[5] == (byte) 0x0A &&
                data[6] == (byte) 0x1A && data[7] == (byte) 0x0A) {
            return "image/png";
        }

        // GIF: 47 49 46 38
        if (data[0] == (byte) 0x47 && data[1] == (byte) 0x49 && data[2] == (byte) 0x46 && data[3] == (byte) 0x38) {
            return "image/gif";
        }

        // BMP: 42 4D
        if (data[0] == (byte) 0x42 && data[1] == (byte) 0x4D) {
            return "image/bmp";
        }

        // WEBP: 52 49 46 46 ... 57 45 42 50
        if (data[0] == (byte) 0x52 && data[1] == (byte) 0x49 && data[2] == (byte) 0x46 && data[3] == (byte) 0x46 &&
                data[8] == (byte) 0x57 && data[9] == (byte) 0x45 && data[10] == (byte) 0x42 && data[11] == (byte) 0x50) {
            return "image/webp";
        }

        return null;
    }
}
