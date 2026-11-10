package dev.mostapha.bidmaster.adapter.out.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.mostapha.bidmaster.adapter.out.persistence.entity.ImageEntity;

public interface JpaImageRepository extends JpaRepository<ImageEntity, UUID> {

    List<ImageEntity> findByReferenceIdAndTypeOrderByDisplayOrderAsc(UUID referenceId, String type);

    List<ImageEntity> findByUploadedByOrderByUploadDateDesc(UUID uploadedBy);

    void deleteByReferenceIdAndType(UUID referenceId, String type);
}
