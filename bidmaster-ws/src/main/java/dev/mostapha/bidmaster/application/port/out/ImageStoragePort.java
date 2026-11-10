package dev.mostapha.bidmaster.application.port.out;

/**
 * Puerto de salida para el almacenamiento de objetos binarios de imágenes (MinIO/S3).
 */
public interface ImageStoragePort {

    String storeImage(byte[] imageData, String filename, String contentType, String bucketName);

    String storeThumbnail(byte[] imageData, String filename, String contentType, String bucketName);

    byte[] getImage(String objectName, String bucketName);

    boolean deleteImage(String objectName, String bucketName);

    String getPublicUrl(String objectName, String bucketName);

    String getPresignedUrl(String objectName, String bucketName, Integer expirationTimeSeconds);
}
