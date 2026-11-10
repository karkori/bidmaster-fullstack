package dev.mostapha.bidmaster.config;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import dev.mostapha.bidmaster.application.port.out.ImageStoragePort;

@Configuration
@Profile("test")
public class TestStorageConfig {

    @Bean
    @Primary
    public ImageStoragePort inMemoryImageStoragePort() {
        return new InMemoryImageStoragePort();
    }

    public static class InMemoryImageStoragePort implements ImageStoragePort {
        private final Map<String, byte[]> storage = new ConcurrentHashMap<>();

        @Override
        public String storeImage(byte[] imageData, String filename, String contentType, String bucketName) {
            String objectName = UUID.randomUUID().toString() + "_" + filename;
            storage.put(bucketKey(bucketName, objectName), imageData);
            return objectName;
        }

        @Override
        public String storeThumbnail(byte[] imageData, String filename, String contentType, String bucketName) {
            String objectName = "thumbnail_" + UUID.randomUUID().toString() + "_" + filename;
            storage.put(bucketKey(bucketName, objectName), imageData);
            return objectName;
        }

        @Override
        public byte[] getImage(String objectName, String bucketName) {
            byte[] data = storage.get(bucketKey(bucketName, objectName));
            if (data == null) {
                throw new NoSuchElementException("Object not found in test storage: " + objectName);
            }
            return data;
        }

        @Override
        public boolean deleteImage(String objectName, String bucketName) {
            return storage.remove(bucketKey(bucketName, objectName)) != null;
        }

        @Override
        public String getPublicUrl(String objectName, String bucketName) {
            return "http://localhost:8080/api/images/raw/" + objectName;
        }

        @Override
        public String getPresignedUrl(String objectName, String bucketName, Integer expirationTimeSeconds) {
            return "http://localhost:8080/api/images/raw/" + objectName;
        }

        private String bucketKey(String bucketName, String objectName) {
            return bucketName + "/" + objectName;
        }
    }
}
