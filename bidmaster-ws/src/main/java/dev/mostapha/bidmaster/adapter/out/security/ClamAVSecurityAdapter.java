package dev.mostapha.bidmaster.adapter.out.security;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import dev.mostapha.bidmaster.application.port.out.SecurityScanPort;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.Connection;
import reactor.netty.tcp.TcpClient;

/**
 * Adaptador que implementa el escaneo de seguridad usando ClamAV.
 */
@Component
@Slf4j
public class ClamAVSecurityAdapter implements SecurityScanPort {

    @Value("${clamav.host:localhost}")
    private String clamavHost;

    @Value("${clamav.port:3310}")
    private int clamavPort;

    private static final String CLEAN_RESPONSE = "OK";
    private static final String STREAM_PREFIX = "stream: ";
    private static final int CHUNK_SIZE = 4096;
    private static final String INSTREAM_CMD = "zINSTREAM\0";

    private static final Set<String> ALLOWED_IMAGE_TYPES = new HashSet<>(
            Arrays.asList("image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp"));

    @Override
    public Mono<Boolean> scanFile(byte[] fileData) {
        if (fileData == null || fileData.length == 0) {
            log.warn("No se puede escanear un archivo vacío");
            return Mono.just(false);
        }

        return TcpClient.create()
                .host(clamavHost)
                .port(clamavPort)
                .connect()
                .flatMap(connection -> scanWithClamAV(connection, fileData))
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(e -> {
                    log.error("Error en el escaneo de seguridad", e);
                    return Mono.just(false);
                });
    }

    private Mono<Boolean> scanWithClamAV(Connection connection, byte[] fileData) {
        log.info("Iniciando escaneo de archivo con ClamAV");

        // Preparar el comando INSTREAM
        ByteBuf cmdBuf = Unpooled.wrappedBuffer(INSTREAM_CMD.getBytes(StandardCharsets.UTF_8));

        // Enviar comando e iniciar el escaneo
        return connection.outbound()
                .send(Mono.just(cmdBuf))
                .then(sendFileChunks(connection, fileData))
                .then()
                .then(Mono.defer(() -> connection.inbound().receive().aggregate().asString()))
                .map(response -> {
                    // Log completo de la respuesta para diagnóstico
                    log.debug("Respuesta completa de ClamAV: '{}'", response);
                    
                    // Eliminar posibles espacios, saltos de línea, etc.
                    String cleanedResponse = response.trim();
                    
                    // Verificar si contiene "OK" al final o "stream: OK" en cualquier lugar
                    boolean isClean = cleanedResponse.endsWith(CLEAN_RESPONSE) || 
                                     (cleanedResponse.contains(STREAM_PREFIX) && 
                                      cleanedResponse.contains(STREAM_PREFIX + CLEAN_RESPONSE));
                    
                    log.info("Resultado de escaneo: {}", isClean ? "Limpio" : "Infectado - " + cleanedResponse);
                    return isClean;
                })
                .doFinally(signalType -> {
                    try {
                        if (!connection.isDisposed()) {
                            connection.dispose();
                        }
                    } catch (Exception e) {
                        log.warn("Error al cerrar conexión con ClamAV", e);
                    }
                });
    }

    private Mono<Void> sendFileChunks(Connection connection, byte[] fileData) {
        // Dividir los datos en chunks
        return Flux.fromIterable(chunkifyData(fileData))
                .concatMap(chunk -> {
                    // Enviar primero el tamaño del chunk
                    ByteBuf sizeBuf = Unpooled.wrappedBuffer(
                            ByteBuffer.allocate(4).putInt(chunk.length).array());

                    // Luego enviar el contenido del chunk
                    ByteBuf dataBuf = Unpooled.wrappedBuffer(chunk);

                    // Encadenar las operaciones de forma reactiva
                    return connection.outbound().send(Mono.just(sizeBuf))
                            .then(connection.outbound().send(Mono.just(dataBuf)))
                            .then();
                })
                .then(Mono.defer(() -> {
                    // Enviar un chunk de tamaño cero al final para indicar que terminamos
                    ByteBuf endBuf = Unpooled.wrappedBuffer(new byte[] { 0, 0, 0, 0 });
                    return connection.outbound().send(Mono.just(endBuf)).then();
                }));
    }
    
    /**
     * Divide un array de bytes en chunks de tamaño fijo
     */
    private List<byte[]> chunkifyData(byte[] data) {
        List<byte[]> chunks = new ArrayList<>();
        int offset = 0;
        
        while (offset < data.length) {
            int chunkSize = Math.min(CHUNK_SIZE, data.length - offset);
            byte[] chunk = new byte[chunkSize];
            System.arraycopy(data, offset, chunk, 0, chunkSize);
            chunks.add(chunk);
            offset += chunkSize;
        }
        
        return chunks;
    }

    @Override
    public Mono<Boolean> validateFileType(byte[] fileData, String declaredContentType) {
        if (fileData == null || fileData.length == 0) {
            log.warn("No se puede validar un archivo vacío");
            return Mono.just(false);
        }

        return Mono.fromCallable(() -> {
            // Verificar si el tipo de contenido está permitido
            if (!ALLOWED_IMAGE_TYPES.contains(declaredContentType.toLowerCase())) {
                log.warn("Tipo de contenido no permitido: {}", declaredContentType);
                return false;
            }

            // Verificar la firma real del archivo
            String detectedType = detectFileType(fileData);
            if (detectedType == null) {
                log.warn("No se pudo detectar el tipo de archivo");
                return false;
            }

            boolean isValid = detectedType.equalsIgnoreCase(declaredContentType);
            if (!isValid) {
                log.warn("Tipo de archivo declarado ({}) no coincide con el detectado ({})",
                        declaredContentType, detectedType);
            }

            return isValid;
        })
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Detecta el tipo MIME basado en la firma del archivo.
     * Implementación básica para los tipos más comunes de imágenes.
     */
    private String detectFileType(byte[] data) {
        if (data.length < 12) {
            return null; // Muy pequeño para determinar
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
                data[8] == (byte) 0x57 && data[9] == (byte) 0x45 && data[10] == (byte) 0x42
                && data[11] == (byte) 0x50) {
            return "image/webp";
        }

        return null;
    }
}
