package dev.mostapha.bidmaster.adapter.in.web.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import dev.mostapha.bidmaster.adapter.in.web.dto.response.ImageResponseDTO;
import dev.mostapha.bidmaster.application.port.in.ImageUseCase;
import dev.mostapha.bidmaster.application.port.out.ImageStoragePort;
import dev.mostapha.bidmaster.domain.model.image.Image;
import dev.mostapha.bidmaster.domain.model.image.Image.ImageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@Slf4j
public class ImageController {

    private final ImageUseCase imageUseCase;
    private final ImageStoragePort imageStoragePort;
    
    private static final int DEFAULT_URL_EXPIRY = 3600; // 1 hora
    
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<ImageResponseDTO>> uploadImage(
            @RequestPart("file") FilePart filePart,
            @RequestParam("type") String type,
            @RequestParam(value = "referenceId", required = false) UUID referenceId,
            @RequestParam("userId") UUID userId) {
        
        ImageType imageType;
        try {
            imageType = ImageType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Mono.just(ResponseEntity.badRequest().body(null));
        }
        
        return filePart.content()
                .reduce(new byte[0], (acc, dataBuffer) -> {
                    byte[] data = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(data);
                    byte[] newAcc = new byte[acc.length + data.length];
                    System.arraycopy(acc, 0, newAcc, 0, acc.length);
                    System.arraycopy(data, 0, newAcc, acc.length, data.length);
                    return newAcc;
                })
                .flatMap(bytes -> imageUseCase.storeImage(
                        bytes, 
                        filePart.filename(), 
                        filePart.headers().getContentType().toString(),
                        userId,
                        imageType,
                        referenceId))
                .flatMap(image -> Mono.zip(
                        Mono.just(image),
                        imageStoragePort.getPublicUrl(image.getObjectName(), image.getBucketName()),
                        imageStoragePort.getPublicUrl(image.getThumbnailObjectName(), image.getBucketName())
                ))
                .map(tuple -> {
                    Image image = tuple.getT1();
                    String imageUrl = tuple.getT2();
                    String thumbnailUrl = tuple.getT3();
                    
                    return ResponseEntity.status(HttpStatus.CREATED)
                            .body(ImageResponseDTO.fromDomain(image, imageUrl, thumbnailUrl));
                })
                .onErrorResume(e -> {
                    log.error("Error al procesar la imagen", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null));
                });
    }
    
    @GetMapping("/{id}")
    public Mono<ResponseEntity<ImageResponseDTO>> getImage(@PathVariable UUID id) {
        return imageUseCase.getImageById(id)
                .flatMap(image -> Mono.zip(
                        Mono.just(image),
                        imageStoragePort.getPublicUrl(image.getObjectName(), image.getBucketName()),
                        imageStoragePort.getPublicUrl(image.getThumbnailObjectName(), image.getBucketName())
                ))
                .map(tuple -> {
                    Image image = tuple.getT1();
                    String imageUrl = tuple.getT2();
                    String thumbnailUrl = tuple.getT3();
                    
                    return ResponseEntity.ok(ImageResponseDTO.fromDomain(image, imageUrl, thumbnailUrl));
                })
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }
    
    @GetMapping("/reference/{referenceId}")
    public Flux<ImageResponseDTO> getImagesByReference(
            @PathVariable UUID referenceId,
            @RequestParam("type") String type) {
        
        ImageType imageType;
        try {
            imageType = ImageType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Flux.error(new IllegalArgumentException("Tipo de imagen no válido"));
        }
        
        return imageUseCase.getImagesByReference(referenceId, imageType)
                .flatMap(image -> Mono.zip(
                        Mono.just(image),
                        imageStoragePort.getPublicUrl(image.getObjectName(), image.getBucketName()),
                        imageStoragePort.getPublicUrl(image.getThumbnailObjectName(), image.getBucketName())
                ))
                .map(tuple -> {
                    Image image = tuple.getT1();
                    String imageUrl = tuple.getT2();
                    String thumbnailUrl = tuple.getT3();
                    
                    return ImageResponseDTO.fromDomain(image, imageUrl, thumbnailUrl);
                });
    }
    
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteImage(@PathVariable UUID id) {
        return imageUseCase.deleteImage(id)
                .map(deleted -> {
                    if (deleted) {
                        return ResponseEntity.noContent().<Void>build();
                    } else {
                        return ResponseEntity.notFound().<Void>build();
                    }
                });
    }
    
    @DeleteMapping
    public Mono<ResponseEntity<Long>> deleteImages(@RequestParam("ids") List<UUID> ids) {
        return imageUseCase.deleteImages(ids)
                .map(count -> ResponseEntity.ok(count));
    }
    
    // Endpoint para obtener una URL temporal (para implementación futura)
    @GetMapping("/{id}/secure")
    public Mono<ResponseEntity<String>> getSecureUrl(
            @PathVariable UUID id,
            @RequestParam(value = "expiry", required = false) Integer expirySeconds) {
        
        int expiry = expirySeconds != null ? expirySeconds : DEFAULT_URL_EXPIRY;
        
        return imageUseCase.generatePresignedUrl(id, expiry)
                .map(url -> ResponseEntity.ok(url))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }
    
    // Endpoint para obtener una URL temporal para la miniatura (para implementación futura)
    @GetMapping("/{id}/thumbnail/secure")
    public Mono<ResponseEntity<String>> getSecureThumbnailUrl(
            @PathVariable UUID id,
            @RequestParam(value = "expiry", required = false) Integer expirySeconds) {
        
        int expiry = expirySeconds != null ? expirySeconds : DEFAULT_URL_EXPIRY;
        
        return imageUseCase.generateThumbnailPresignedUrl(id, expiry)
                .map(url -> ResponseEntity.ok(url))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }
    
    // Endpoints para imágenes públicas (implementación actual prioritaria)
    
    @GetMapping("/{id}/raw")
    public Mono<ResponseEntity<byte[]>> getRawImage(@PathVariable UUID id) {
        return imageUseCase.getImageById(id)
                .flatMap(image -> imageStoragePort.getImage(image.getObjectName(), image.getBucketName())
                        .map(data -> ResponseEntity.ok()
                                .contentType(MediaType.valueOf(image.getContentType()))
                                .body(data)))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }
    
    @GetMapping("/{id}/thumbnail/raw")
    public Mono<ResponseEntity<byte[]>> getRawThumbnail(@PathVariable UUID id) {
        return imageUseCase.getImageById(id)
                .flatMap(image -> imageStoragePort.getImage(image.getThumbnailObjectName(), image.getBucketName())
                        .map(data -> ResponseEntity.ok()
                                .contentType(MediaType.valueOf(image.getContentType()))
                                .body(data)))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }
}
