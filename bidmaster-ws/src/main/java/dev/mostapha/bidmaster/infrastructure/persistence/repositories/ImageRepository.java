package dev.mostapha.bidmaster.infrastructure.persistence.repositories;

import java.util.UUID;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import dev.mostapha.bidmaster.infrastructure.persistence.entities.ImageEntity;
import reactor.core.publisher.Flux;

@Repository
public interface ImageRepository extends R2dbcRepository<ImageEntity, UUID> {
    
    Flux<ImageEntity> findByReferenceIdAndType(UUID referenceId, String type);
    
    Flux<ImageEntity> findByUploadedBy(UUID uploadedBy);
}
