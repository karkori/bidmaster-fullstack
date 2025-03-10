package dev.mostapha.bidmaster.adapter.out.storage;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import dev.mostapha.bidmaster.application.port.out.ImageStoragePort;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.SetBucketPolicyArgs;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
@Slf4j
public class MinioStorageAdapter implements ImageStoragePort {

    private final MinioClient minioClient;

    public MinioStorageAdapter(MinioClient minioClient) {
        this.minioClient = minioClient;
    }
    
    @Value("${minio.endpoint}")
    private String minioEndpoint;
    
    @Value("${minio.public-endpoint:#{null}}")
    private String minioPublicEndpoint;
    
    // Tamaño máximo de miniatura (px)
    private static final int THUMBNAIL_SIZE = 200;
    
    @Override
    public Mono<String> storeImage(byte[] imageData, String filename, String contentType, String bucketName) {
        return Mono.fromCallable(() -> {
                // Asegurar que el bucket existe
                createBucketIfNotExists(bucketName);
                
                // Generar un nombre único para el objeto
                String objectName = generateUniqueObjectName(filename);
                
                // Subir la imagen a MinIO
                try (ByteArrayInputStream inputStream = new ByteArrayInputStream(imageData)) {
                    minioClient.putObject(
                            PutObjectArgs.builder()
                                    .bucket(bucketName)
                                    .object(objectName)
                                    .stream(inputStream, imageData.length, -1)
                                    .contentType(contentType)
                                    .build());
                    
                    log.info("Imagen almacenada con éxito: bucket={}, object={}", bucketName, objectName);
                    return objectName;
                }
            })
            .subscribeOn(Schedulers.boundedElastic())
            .onErrorResume(e -> {
                log.error("Error al almacenar la imagen en MinIO", e);
                return Mono.error(new RuntimeException("Error al almacenar la imagen", e));
            });
    }
    
    @Override
    public Mono<String> storeThumbnail(byte[] imageData, String filename, String contentType, String bucketName) {
        return Mono.fromCallable(() -> {
                // Asegurar que el bucket existe
                createBucketIfNotExists(bucketName);
                
                // Generar miniatura
                byte[] thumbnailData = generateThumbnail(imageData, getImageFormat(contentType));
                
                // Generar un nombre único para la miniatura
                String thumbnailName = "thumbnail_" + generateUniqueObjectName(filename);
                
                // Subir la miniatura a MinIO
                try (ByteArrayInputStream inputStream = new ByteArrayInputStream(thumbnailData)) {
                    minioClient.putObject(
                            PutObjectArgs.builder()
                                    .bucket(bucketName)
                                    .object(thumbnailName)
                                    .stream(inputStream, thumbnailData.length, -1)
                                    .contentType(contentType)
                                    .build());
                    
                    log.info("Miniatura almacenada con éxito: bucket={}, object={}", bucketName, thumbnailName);
                    return thumbnailName;
                }
            })
            .subscribeOn(Schedulers.boundedElastic())
            .onErrorResume(e -> {
                log.error("Error al generar o almacenar la miniatura en MinIO", e);
                return Mono.error(new RuntimeException("Error al procesar la miniatura", e));
            });
    }
    
    @Override
    public Mono<byte[]> getImage(String objectName, String bucketName) {
        return Mono.fromCallable(() -> {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                
                minioClient.getObject(
                        GetObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectName)
                                .build()
                ).transferTo(outputStream);
                
                return outputStream.toByteArray();
            })
            .subscribeOn(Schedulers.boundedElastic())
            .onErrorResume(e -> {
                log.error("Error al recuperar la imagen de MinIO", e);
                return Mono.error(new RuntimeException("Error al recuperar la imagen", e));
            });
    }
    
    @Override
    public Mono<Boolean> deleteImage(String objectName, String bucketName) {
        return Mono.fromCallable(() -> {
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectName)
                                .build());
                
                log.info("Imagen eliminada con éxito: bucket={}, object={}", bucketName, objectName);
                return true;
            })
            .subscribeOn(Schedulers.boundedElastic())
            .onErrorResume(e -> {
                log.error("Error al eliminar la imagen de MinIO", e);
                return Mono.just(false);
            });
    }
    
    @Override
    public Mono<String> getPublicUrl(String objectName, String bucketName) {
        return Mono.fromCallable(() -> {
                // Asegurar que el bucket tiene una política pública
                setupPublicBucketPolicy(bucketName);
                
                // Endpoint público o el predeterminado
                String endpoint = minioPublicEndpoint != null ? minioPublicEndpoint : minioEndpoint;
                
                return String.format("%s/%s/%s", endpoint, bucketName, objectName);
            })
            .subscribeOn(Schedulers.boundedElastic())
            .onErrorResume(e -> {
                log.error("Error al generar URL pública", e);
                return Mono.error(new RuntimeException("Error al generar URL pública", e));
            });
    }
    
    @Override
    public Mono<String> getPresignedUrl(String objectName, String bucketName, Integer expirationTimeSeconds) {
        return Mono.fromCallable(() -> {
                int expiry = expirationTimeSeconds != null ? expirationTimeSeconds : 3600;
                
                String url = minioClient.getPresignedObjectUrl(
                        GetPresignedObjectUrlArgs.builder()
                                .method(Method.GET)
                                .bucket(bucketName)
                                .object(objectName)
                                .expiry(expiry, TimeUnit.SECONDS)
                                .build());
                
                log.info("URL prefirmada generada: {}", url);
                return url;
            })
            .subscribeOn(Schedulers.boundedElastic())
            .onErrorResume(e -> {
                log.error("Error al generar URL prefirmada", e);
                return Mono.error(new RuntimeException("Error al generar URL prefirmada", e));
            });
    }
    
    // Métodos auxiliares
    
    private void createBucketIfNotExists(String bucketName) throws Exception {
        boolean bucketExists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(bucketName).build());
        
        if (!bucketExists) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder().bucket(bucketName).build());
            log.info("Bucket creado: {}", bucketName);
        }
    }
    
    private void setupPublicBucketPolicy(String bucketName) throws Exception {
        // Política para acceso público de lectura
        String policy = String.format(
                "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:GetObject\"],\"Resource\":[\"arn:aws:s3:::%s/*\"]}]}",
                bucketName);
        
        minioClient.setBucketPolicy(
                SetBucketPolicyArgs.builder()
                        .bucket(bucketName)
                        .config(policy)
                        .build());
    }
    
    private String generateUniqueObjectName(String filename) {
        String extension = "";
        int lastDotIndex = filename.lastIndexOf('.');
        
        if (lastDotIndex > 0) {
            extension = filename.substring(lastDotIndex);
            filename = filename.substring(0, lastDotIndex);
        }
        
        // Generar nombre único basado en timestamp y nombre original
        return String.format("%s_%d%s", 
                filename.replaceAll("[^a-zA-Z0-9]", "_"),
                System.currentTimeMillis(),
                extension);
    }
    
    private String getImageFormat(String contentType) {
        Map<String, String> formatMapping = new HashMap<>();
        formatMapping.put("image/jpeg", "jpg");
        formatMapping.put("image/jpg", "jpg");
        formatMapping.put("image/png", "png");
        formatMapping.put("image/gif", "gif");
        formatMapping.put("image/bmp", "bmp");
        formatMapping.put("image/webp", "webp");
        
        return formatMapping.getOrDefault(contentType.toLowerCase(), "jpg");
    }
    
    private byte[] generateThumbnail(byte[] imageData, String format) throws Exception {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(imageData)) {
            // Leer la imagen original
            BufferedImage originalImage = ImageIO.read(inputStream);
            
            // Calcular dimensiones manteniendo relación de aspecto
            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();
            
            int targetWidth, targetHeight;
            
            if (originalWidth > originalHeight) {
                targetWidth = THUMBNAIL_SIZE;
                targetHeight = (int) (originalHeight * ((double) THUMBNAIL_SIZE / originalWidth));
            } else {
                targetHeight = THUMBNAIL_SIZE;
                targetWidth = (int) (originalWidth * ((double) THUMBNAIL_SIZE / originalHeight));
            }
            
            // Crear la miniatura
            Image thumbnailImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
            BufferedImage bufferedThumbnail = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
            
            Graphics2D graphics = bufferedThumbnail.createGraphics();
            graphics.drawImage(thumbnailImage, 0, 0, null);
            graphics.dispose();
            
            // Convertir la miniatura a byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedThumbnail, format, outputStream);
            
            return outputStream.toByteArray();
        }
    }
}
