package dev.mostapha.bidmaster.adapter.out.storage;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

@Component
@Slf4j
public class MinioStorageAdapter implements ImageStoragePort {

    private final MinioClient minioClient;

    @Value("${minio.endpoint}")
    private String minioEndpoint;

    @Value("${minio.public-endpoint:#{null}}")
    private String minioPublicEndpoint;

    private static final int THUMBNAIL_SIZE = 200;

    private static final Map<String, String> FORMAT_MAPPING = Map.of(
            "image/jpeg", "jpg",
            "image/jpg", "jpg",
            "image/png", "png",
            "image/gif", "gif",
            "image/bmp", "bmp",
            "image/webp", "webp"
    );

    public MinioStorageAdapter(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @Override
    public String storeImage(byte[] imageData, String filename, String contentType, String bucketName) {
        try {
            createBucketIfNotExists(bucketName);
            String objectName = generateUniqueObjectName(filename);

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
        } catch (Exception e) {
            log.error("Error al almacenar la imagen en MinIO", e);
            throw new RuntimeException("Error al almacenar la imagen", e);
        }
    }

    @Override
    public String storeThumbnail(byte[] imageData, String filename, String contentType, String bucketName) {
        try {
            createBucketIfNotExists(bucketName);
            byte[] thumbnailData = generateThumbnail(imageData, getImageFormat(contentType));
            String thumbnailName = "thumbnail_" + generateUniqueObjectName(filename);

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
        } catch (Exception e) {
            log.error("Error al generar o almacenar la miniatura en MinIO", e);
            throw new RuntimeException("Error al procesar la miniatura", e);
        }
    }

    @Override
    public byte[] getImage(String objectName, String bucketName) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            ).transferTo(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("Error al recuperar la imagen de MinIO", e);
            throw new RuntimeException("Error al recuperar la imagen", e);
        }
    }

    @Override
    public boolean deleteImage(String objectName, String bucketName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());
            log.info("Imagen eliminada con éxito: bucket={}, object={}", bucketName, objectName);
            return true;
        } catch (Exception e) {
            log.error("Error al eliminar la imagen de MinIO", e);
            return false;
        }
    }

    @Override
    public String getPublicUrl(String objectName, String bucketName) {
        try {
            setupPublicBucketPolicy(bucketName);
            String endpoint = (minioPublicEndpoint != null && !minioPublicEndpoint.isBlank())
                    ? minioPublicEndpoint
                    : minioEndpoint;
            return String.format("%s/%s/%s", endpoint, bucketName, objectName);
        } catch (Exception e) {
            log.error("Error al generar URL pública", e);
            throw new RuntimeException("Error al generar URL pública", e);
        }
    }

    @Override
    public String getPresignedUrl(String objectName, String bucketName, Integer expirationTimeSeconds) {
        try {
            int expiry = expirationTimeSeconds != null ? expirationTimeSeconds : 3600;
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(expiry, TimeUnit.SECONDS)
                            .build());
        } catch (Exception e) {
            log.error("Error al generar URL prefirmada", e);
            throw new RuntimeException("Error al generar URL prefirmada", e);
        }
    }

    private void createBucketIfNotExists(String bucketName) throws Exception {
        boolean bucketExists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(bucketName).build());
        if (!bucketExists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            log.info("Bucket creado: {}", bucketName);
        }
    }

    private void setupPublicBucketPolicy(String bucketName) throws Exception {
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
        if (filename == null || filename.isBlank()) {
            filename = "image.jpg";
        }
        String extension = "";
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            extension = filename.substring(lastDotIndex);
            filename = filename.substring(0, lastDotIndex);
        }
        return String.format("%s_%d%s",
                filename.replaceAll("[^a-zA-Z0-9]", "_"),
                System.currentTimeMillis(),
                extension);
    }

    private String getImageFormat(String contentType) {
        if (contentType == null) {
            return "jpg";
        }
        return FORMAT_MAPPING.getOrDefault(contentType.toLowerCase(), "jpg");
    }

    private byte[] generateThumbnail(byte[] imageData, String format) throws Exception {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(imageData)) {
            BufferedImage originalImage = ImageIO.read(inputStream);
            if (originalImage == null) {
                return imageData;
            }

            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();

            int targetWidth;
            int targetHeight;
            if (originalWidth > originalHeight) {
                targetWidth = THUMBNAIL_SIZE;
                targetHeight = Math.max(1, (int) (originalHeight * ((double) THUMBNAIL_SIZE / originalWidth)));
            } else {
                targetHeight = THUMBNAIL_SIZE;
                targetWidth = Math.max(1, (int) (originalWidth * ((double) THUMBNAIL_SIZE / originalHeight)));
            }

            Image thumbnailImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
            BufferedImage bufferedThumbnail = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);

            Graphics2D graphics = bufferedThumbnail.createGraphics();
            graphics.drawImage(thumbnailImage, 0, 0, null);
            graphics.dispose();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedThumbnail, format, outputStream);
            return outputStream.toByteArray();
        }
    }
}
